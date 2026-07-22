package dev.ccpocket.daemon.codex

import java.io.ByteArrayOutputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Stdio JSON-lines ↔ Codex managed app-server WebSocket bridge.
 *
 * Codex 0.145.0's `app-server proxy` writes raw JSON to the Unix socket even though the managed
 * server accepts WebSocket upgrades at `/rpc`, so the server closes the connection immediately.
 * This tiny JVM child performs the missing RFC 6455 handshake/framing and otherwise leaves the
 * app-server JSON untouched. It deliberately lives in its own process so the existing AgentIo
 * lifecycle, stderr isolation and restart behavior remain unchanged.
 */
object CodexManagedProxy {
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 1) { "usage: CodexManagedProxy <unix-socket>" }
        SocketChannel.open(StandardProtocolFamily.UNIX).use { channel ->
            channel.connect(UnixDomainSocketAddress.of(args[0]))
            // A Channel-backed InputStream performs a socket read for every single-byte read.
            // WebSocket headers and frame prefixes are byte-oriented, so buffering is essential
            // here (without it a valid local 101 response can take seconds to consume).
            val input = BufferedInputStream(ChannelInput(channel))
            val output = BufferedOutputStream(ChannelOutput(channel))
            handshake(input, output)

            val writer = Thread({ forwardStdin(output) }, "codex-managed-proxy-writer").apply {
                isDaemon = true
                start()
            }
            forwardWebSocket(input, output, System.out)
            writer.interrupt()
        }
    }

    private fun handshake(input: InputStream, output: OutputStream) {
        val keyBytes = ByteArray(16).also(RANDOM::nextBytes)
        val key = Base64.getEncoder().encodeToString(keyBytes)
        val request = buildString {
            append("GET /rpc HTTP/1.1\r\n")
            append("Host: localhost\r\n")
            append("Connection: Upgrade\r\n")
            append("Upgrade: websocket\r\n")
            append("Sec-WebSocket-Version: 13\r\n")
            append("Sec-WebSocket-Key: $key\r\n\r\n")
        }
        output.write(request.toByteArray(StandardCharsets.US_ASCII)); output.flush()

        val header = ByteArrayOutputStream()
        var matched = 0
        val end = byteArrayOf(13, 10, 13, 10)
        while (header.size() < MAX_HANDSHAKE_BYTES) {
            val b = input.read()
            if (b < 0) throw EOFException("managed app-server closed during WebSocket handshake")
            header.write(b)
            matched = if (b.toByte() == end[matched]) matched + 1 else if (b == 13) 1 else 0
            if (matched == end.size) break
        }
        val response = header.toString(StandardCharsets.US_ASCII)
        require(response.startsWith("HTTP/1.1 101 ")) { "managed app-server rejected WebSocket upgrade: ${response.lineSequence().firstOrNull()}" }
        val expected = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-1").digest((key + WS_GUID).toByteArray(StandardCharsets.US_ASCII)),
        )
        val actual = response.lineSequence().firstOrNull { it.startsWith("sec-websocket-accept:", ignoreCase = true) }
            ?.substringAfter(':')?.trim()
        require(actual == expected) { "managed app-server returned an invalid WebSocket accept" }
    }

    private fun forwardStdin(output: OutputStream) {
        System.`in`.bufferedReader().forEachLine { line ->
            synchronized(output) { writeFrame(output, OPCODE_TEXT, line.toByteArray(StandardCharsets.UTF_8)) }
        }
        synchronized(output) { runCatching { writeFrame(output, OPCODE_CLOSE, byteArrayOf()) } }
    }

    private fun forwardWebSocket(input: InputStream, output: OutputStream, stdout: OutputStream) {
        var fragmentedOpcode = 0
        var fragments: ByteArrayOutputStream? = null
        while (true) {
            val first = input.read(); if (first < 0) return
            val second = input.read(); if (second < 0) throw EOFException("truncated WebSocket frame")
            val fin = first and 0x80 != 0
            val opcode = first and 0x0f
            val masked = second and 0x80 != 0
            var length = (second and 0x7f).toLong()
            if (length == 126L) length = readUnsigned(input, 2)
            if (length == 127L) length = readUnsigned(input, 8)
            require(length <= MAX_FRAME_BYTES) { "managed app-server WebSocket frame too large: $length" }
            val mask = if (masked) input.readExact(4) else null
            val payload = input.readExact(length.toInt())
            mask?.let { key -> payload.indices.forEach { payload[it] = (payload[it].toInt() xor key[it % 4].toInt()).toByte() } }

            when (opcode) {
                OPCODE_TEXT -> if (fin) emitJson(stdout, payload) else {
                    fragmentedOpcode = opcode; fragments = ByteArrayOutputStream().apply { write(payload) }
                }
                OPCODE_CONTINUATION -> {
                    val sink = requireNotNull(fragments) { "unexpected WebSocket continuation" }
                    sink.write(payload)
                    if (fin) {
                        if (fragmentedOpcode == OPCODE_TEXT) emitJson(stdout, sink.toByteArray())
                        fragments = null; fragmentedOpcode = 0
                    }
                }
                OPCODE_PING -> synchronized(output) { writeFrame(output, OPCODE_PONG, payload) }
                OPCODE_PONG -> Unit
                OPCODE_CLOSE -> return
                else -> Unit
            }
        }
    }

    private fun emitJson(stdout: OutputStream, payload: ByteArray) {
        stdout.write(payload)
        if (payload.lastOrNull() != '\n'.code.toByte()) stdout.write('\n'.code)
        stdout.flush()
    }

    /** Client→server RFC 6455 frames MUST be masked. */
    private fun writeFrame(output: OutputStream, opcode: Int, payload: ByteArray) {
        output.write(0x80 or opcode)
        when {
            payload.size < 126 -> output.write(0x80 or payload.size)
            payload.size <= 0xffff -> {
                output.write(0x80 or 126); output.write(payload.size ushr 8); output.write(payload.size)
            }
            else -> {
                output.write(0x80 or 127)
                for (shift in 56 downTo 0 step 8) output.write(payload.size.toLong().ushr(shift).toInt())
            }
        }
        val mask = ByteArray(4).also(RANDOM::nextBytes)
        output.write(mask)
        payload.indices.forEach { output.write(payload[it].toInt() xor mask[it % 4].toInt()) }
        output.flush()
    }

    private fun readUnsigned(input: InputStream, count: Int): Long {
        var value = 0L
        repeat(count) { value = (value shl 8) or (input.read().takeIf { it >= 0 } ?: throw EOFException()).toLong() }
        return value
    }

    private fun InputStream.readExact(count: Int): ByteArray = ByteArray(count).also { bytes ->
        var offset = 0
        while (offset < count) {
            val read = read(bytes, offset, count - offset)
            if (read < 0) throw EOFException("truncated WebSocket payload")
            offset += read
        }
    }

    // Channels.newInputStream/newOutputStream serialize blocking operations on the channel's
    // blockingLock. A WebSocket must read and write concurrently, otherwise the read loop holds
    // that lock forever and stdin can never send its first JSON-RPC request.
    private class ChannelInput(private val channel: SocketChannel) : InputStream() {
        override fun read(): Int {
            val one = ByteBuffer.allocate(1)
            return if (channel.read(one) < 0) -1 else one.array()[0].toInt() and 0xff
        }

        override fun read(bytes: ByteArray, offset: Int, length: Int): Int =
            channel.read(ByteBuffer.wrap(bytes, offset, length))
    }

    private class ChannelOutput(private val channel: SocketChannel) : OutputStream() {
        override fun write(value: Int) = write(byteArrayOf(value.toByte()))

        override fun write(bytes: ByteArray, offset: Int, length: Int) {
            val buffer = ByteBuffer.wrap(bytes, offset, length)
            while (buffer.hasRemaining()) channel.write(buffer)
        }
    }

    private val RANDOM = SecureRandom()
    private const val WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
    private const val MAX_HANDSHAKE_BYTES = 16 * 1024
    private const val MAX_FRAME_BYTES = 64L * 1024 * 1024
    private const val OPCODE_CONTINUATION = 0x0
    private const val OPCODE_TEXT = 0x1
    private const val OPCODE_CLOSE = 0x8
    private const val OPCODE_PING = 0x9
    private const val OPCODE_PONG = 0xa
}
