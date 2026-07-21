package dev.ccpocket.protocol.update

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Duration

/**
 * The JVM half of the self-update plumbing shared by the daemon and the desktop app: read the latest GitHub
 * release (version + asset download URLs), fetch an asset, and verify it against the release's SHA256SUMS.
 * Both binaries publish under the same repo with one SHA256SUMS over every asset, so this reads identically
 * for either — the only thing that differs is which asset name each downloads (a daemon tarball vs a desktop
 * dmg/msi), which the caller decides. Kept dependency-light (java.net.http only) so pulling it into the
 * Compose Desktop client adds nothing the daemon didn't already carry.
 */
object ReleaseClient {
    const val DEFAULT_REPO = "ac54u-mobile/cc-pocket"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val http: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(15))
        .build()

    /** A release: its version (no `v` / `daemon-v` prefix) and every asset's name → browser_download_url. */
    data class Release(val version: String, val assetUrls: Map<String, String>)

    /**
     * Newest **daemon** release. Prefers tags `daemon-vX.Y.Z` so App-only tags (`app-v…`) never hijack
     * self-update / install. Falls back to a plain `vX.Y.Z` (or GitHub "latest") that actually ships a
     * `cc-pocket-daemon-*` asset.
     */
    fun latest(repo: String = DEFAULT_REPO): Release? = try {
        val objs = fetchJsonArray("https://api.github.com/repos/$repo/releases?per_page=30")
            .orEmpty()
            .mapNotNull { it as? JsonObject }
            .filter { obj ->
                (obj["draft"] as? JsonPrimitive)?.booleanOrNull != true &&
                    !(obj["tag_name"] as? JsonPrimitive)?.contentOrNull.orEmpty().let {
                        it.startsWith("app-v") || it.startsWith("app/")
                    } &&
                    (obj["assets"] as? JsonArray).orEmpty().any { el ->
                        ((el as? JsonObject)?.get("name") as? JsonPrimitive)?.contentOrNull
                            ?.startsWith("cc-pocket-daemon-") == true
                    }
            }
        // Prefer daemon-v* so App releases never become the self-update target
        val preferred = objs.firstOrNull { obj ->
            (obj["tag_name"] as? JsonPrimitive)?.contentOrNull.orEmpty().let {
                it.startsWith("daemon-v") || it.startsWith("daemon/")
            }
        } ?: objs.firstOrNull()
        parseRelease(preferred) ?: run {
            val obj = fetchJsonObject("https://api.github.com/repos/$repo/releases/latest")
            parseRelease(obj)?.takeIf { it.assetUrls.keys.any { n -> n.startsWith("cc-pocket-daemon-") } }
        }
    } catch (_: Exception) {
        null
    }

    private fun fetchJsonArray(url: String): JsonArray? {
        val res = http.send(get(url), HttpResponse.BodyHandlers.ofString())
        if (res.statusCode() != 200) return null
        return json.parseToJsonElement(res.body()) as? JsonArray
    }

    private fun fetchJsonObject(url: String): JsonObject? {
        val res = http.send(get(url), HttpResponse.BodyHandlers.ofString())
        if (res.statusCode() != 200) return null
        return json.parseToJsonElement(res.body()) as? JsonObject
    }

    private fun get(url: String): HttpRequest =
        HttpRequest.newBuilder(URI(url))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "cc-pocket")
            .timeout(Duration.ofSeconds(20))
            .build()

    private fun parseRelease(obj: JsonObject?): Release? {
        if (obj == null) return null
        val tag = (obj["tag_name"] as? JsonPrimitive)?.contentOrNull ?: return null
        val assets = (obj["assets"] as? JsonArray).orEmpty().mapNotNull { el ->
            val a = el as? JsonObject ?: return@mapNotNull null
            val name = (a["name"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
            val url = (a["browser_download_url"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
            name to url
        }.toMap()
        val version = when {
            tag.startsWith("daemon-v") -> tag.removePrefix("daemon-v")
            tag.startsWith("daemon/") -> tag.removePrefix("daemon/")
            else -> tag.removePrefix("v")
        }
        return Release(version, assets)
    }

    /** Download [url] to [dest] (10-minute ceiling for a large artifact). Throws on a non-2xx status. */
    fun download(url: String, dest: Path) {
        val req = HttpRequest.newBuilder(URI(url)).header("User-Agent", "cc-pocket")
            .timeout(Duration.ofMinutes(10)).build()
        val res = http.send(req, HttpResponse.BodyHandlers.ofFile(dest))
        check(res.statusCode() in 200..299) { "download failed (HTTP ${res.statusCode()}): $url" }
    }

    fun sha256(file: Path): String {
        val md = MessageDigest.getInstance("SHA-256")
        java.nio.file.Files.newInputStream(file).use { s ->
            val buf = ByteArray(1 shl 16)
            while (true) {
                val n = s.read(buf); if (n < 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Verify [file] (published as [asset]) against the release's SHA256SUMS asset. A missing sums entry (old
     * releases, or an asset the manifest doesn't list) warns via [onSkip] and passes — a PRESENT mismatch is
     * fatal (corrupted download or tampered artifact). Returns true when the checksum was actually verified,
     * false when verification was skipped.
     */
    fun verifyAgainstSums(release: Release, asset: String, file: Path, onSkip: (String) -> Unit = {}): Boolean {
        val sumsUrl = release.assetUrls["SHA256SUMS"] ?: run {
            onSkip("release has no SHA256SUMS — skipping checksum verification"); return false
        }
        val req = HttpRequest.newBuilder(URI(sumsUrl)).header("User-Agent", "cc-pocket")
            .timeout(Duration.ofSeconds(30)).build()
        val body = http.send(req, HttpResponse.BodyHandlers.ofString()).takeIf { it.statusCode() == 200 }?.body()
            ?: run { onSkip("could not fetch SHA256SUMS — skipping checksum verification"); return false }
        val expected = ReleaseVersions.parseSums(body)[asset] ?: run {
            onSkip("SHA256SUMS has no entry for $asset — skipping checksum verification"); return false
        }
        val actual = sha256(file)
        check(actual.equals(expected, ignoreCase = true)) {
            "checksum mismatch for $asset\n  expected $expected\n  actual   $actual\n(corrupted download or tampered artifact — aborting)"
        }
        return true
    }
}
