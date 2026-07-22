package dev.ccpocket.daemon.codex

import kotlin.test.Test
import kotlin.test.assertEquals

class CodexLauncherTest {
    @Test
    fun private_transport_uses_direct_app_server() {
        assertEquals(listOf("app-server"), CodexLauncher.buildArgs(shared = false))
    }

    @Test
    fun shared_transport_uses_managed_server_proxy() {
        assertEquals(listOf("app-server", "proxy"), CodexLauncher.buildArgs(shared = true))
    }
}
