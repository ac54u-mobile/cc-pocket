package dev.ccpocket.daemon.codex

import dev.ccpocket.daemon.agent.AgentSpec
import dev.ccpocket.daemon.agent.ExecutableResolver
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Resolves the real `codex` binary and builds the `codex app-server` launch command. Mirrors
 * [dev.ccpocket.daemon.claude.ClaudeLauncher]: never goes through a shell (a PATH shim could corrupt the
 * JSON-RPC stream), prefers native binaries over `#!` script shims, and probes well-known install dirs
 * because login services / GUI launchers often start with a sanitized PATH.
 */
object CodexLauncher {
    private val isWindows: Boolean = System.getProperty("os.name").lowercase().contains("win")
    private val envBin: String? = System.getenv("CC_POCKET_CODEX_BIN")
    private val sharedOverride: String? = System.getenv("CC_POCKET_CODEX_SHARED")
    private val sharedReady = AtomicReference<Boolean?>(null)

    private val exeNames: List<String> =
        if (isWindows) listOf("codex.exe", "codex.cmd", "codex.bat", "codex") else listOf("codex")

    private val fallbackDirs: List<String> = buildList {
        val home = System.getProperty("user.home")
        add(home + File.separator + ".local" + File.separator + "bin")
        // npm/volta/bun/deno global bins where `@openai/codex` commonly lands
        add(home + File.separator + ".npm-global" + File.separator + "bin")
        add(home + File.separator + ".volta" + File.separator + "bin")
        add(home + File.separator + ".bun" + File.separator + "bin")
        add(home + File.separator + ".deno" + File.separator + "bin")
        if (!isWindows) {
            add("/opt/homebrew/bin"); add("/usr/local/bin"); add("/usr/bin")
        }
    }

    fun resolveExecutable(explicit: String? = null): Path =
        ExecutableResolver.resolve(explicit, envBin, exeNames, fallbackDirs, "codex executable not found. Install the Codex CLI, or set CC_POCKET_CODEX_BIN / pass --codex-bin.")

    /** argv for the JSON-RPC transport. A managed app-server is the only transport that can fan the same
     * thread's notifications out to Codex IDE and cc-pocket simultaneously. Older CLIs (and an explicit
     * CC_POCKET_CODEX_SHARED=0) retain the private stdio server. */
    fun buildArgs(shared: Boolean = false): List<String> =
        if (shared) listOf("app-server", "proxy") else listOf("app-server")

    fun processBuilder(exe: Path, spec: AgentSpec): ProcessBuilder {
        val exeStr = exe.toString()
        val needsShell = isWindows && exeStr.lowercase().let { it.endsWith(".cmd") || it.endsWith(".bat") }
        val shared = ensureSharedServer(exe, needsShell)
        val argv = if (shared) managedProxyCommand() else buildList {
            if (needsShell) { add(System.getenv("ComSpec") ?: "cmd.exe"); add("/c") }
            add(exeStr); addAll(buildArgs(shared = false))
        }
        return ProcessBuilder(argv).apply {
            directory(spec.workdir.toFile())
            redirectErrorStream(false) // keep stderr off the stdout JSON-RPC stream
        }
    }

    /** Start Codex's official managed app-server once, then let this conversation attach through its proxy.
     * The capability was added after cc-pocket's original app-server integration, so probing is deliberately
     * runtime + fail-closed. Starting is idempotent according to the CLI contract. */
    private fun ensureSharedServer(exe: Path, needsShell: Boolean): Boolean {
        if (sharedOverride?.lowercase() in setOf("0", "false", "off", "no")) return false
        sharedReady.get()?.let { return it }
        val command = buildList {
            if (needsShell) { add(System.getenv("ComSpec") ?: "cmd.exe"); add("/c") }
            add(exe.toString())
            addAll(listOf("app-server", "daemon", "start"))
        }
        val started = runCatching {
            val p = ProcessBuilder(command).redirectErrorStream(true).start()
            val exited = p.waitFor(SHARED_START_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!exited) p.destroyForcibly()
            exited && p.exitValue() == 0
        }.getOrDefault(false)
        // `daemon start` only proves that the supervisor is alive. Codex 0.145.0 on Linux can leave
        // `app-server proxy` connected but unable to forward even `initialize`, which otherwise makes
        // every phone turn spin forever. Verify the actual byte path before selecting it; fail closed
        // to the proven direct stdio transport when the proxy is absent, incompatible, or wedged.
        val ready = started && !isWindows && probeSharedTransport()
        sharedReady.compareAndSet(null, ready)
        return sharedReady.get() == true
    }

    private fun probeSharedTransport(): Boolean = runCatching {
        val p = ProcessBuilder(managedProxyCommand()).redirectErrorStream(false).start()
        try {
            p.outputStream.bufferedWriter().apply {
                write(PROBE_INITIALIZE)
                newLine()
                flush()
            }
            val response = CompletableFuture.supplyAsync { p.inputStream.bufferedReader().readLine() }
                .get(SHARED_PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            response?.contains("\"id\":$PROBE_ID") == true && response.contains("\"result\"")
        } finally {
            p.destroyForcibly()
            p.waitFor(1, TimeUnit.SECONDS)
        }
    }.getOrDefault(false)

    private fun managedProxyCommand(): List<String> = listOf(
        Path.of(System.getProperty("java.home"), "bin", if (isWindows) "java.exe" else "java").toString(),
        "-cp",
        System.getProperty("java.class.path"),
        CodexManagedProxy::class.java.name,
        System.getenv("CC_POCKET_CODEX_SOCKET")
            ?: Path.of(System.getProperty("user.home"), ".codex", "app-server-control", "app-server-control.sock").toString(),
    )

    private const val SHARED_START_TIMEOUT_SECONDS = 5L
    private const val SHARED_PROBE_TIMEOUT_SECONDS = 4L
    private const val PROBE_ID = 991_337
    private const val PROBE_INITIALIZE =
        "{\"id\":$PROBE_ID,\"method\":\"initialize\",\"params\":{\"clientInfo\":{\"name\":\"cc-pocket-probe\",\"version\":\"1\"},\"capabilities\":{\"experimentalApi\":false}}}"
}
