package dev.ccpocket.app.ui

import org.jetbrains.compose.resources.StringResource
import dev.ccpocket.app.resources.Res
import dev.ccpocket.app.resources.settings_bridges
import dev.ccpocket.app.resources.settings_hub_advanced
import dev.ccpocket.app.resources.settings_hub_agent
import dev.ccpocket.app.resources.settings_hub_appearance
import dev.ccpocket.app.resources.settings_hub_notifications
import dev.ccpocket.app.resources.settings_tab_about
import dev.ccpocket.app.resources.settings_tab_account
import dev.ccpocket.app.resources.settings_tab_computers
import dev.ccpocket.app.resources.settings_tab_schedules
import dev.ccpocket.app.resources.settings_tab_shares
import dev.ccpocket.app.resources.settings_tab_shortcuts
import dev.ccpocket.app.resources.settings_tab_workspace
import dev.ccpocket.app.resources.security_section

/**
 * Shared settings information architecture — phone Hub destinations and desktop left-rail tabs
 * use the same titles / search keywords so the two surfaces stay one product.
 *
 * Platform-only sections are still listed here (with [platforms]) so search indexes stay complete.
 */
enum class SettingsPlatform { MOBILE, DESKTOP }

enum class SettingsSection(
    val title: StringResource,
    val keywords: String,
    val platforms: Set<SettingsPlatform>,
) {
    AGENT(
        Res.string.settings_hub_agent,
        "mode model effort agent filter claude codex opencode 模式 模型 推理",
        setOf(SettingsPlatform.MOBILE, SettingsPlatform.DESKTOP),
    ),
    APPEARANCE(
        Res.string.settings_hub_appearance,
        "theme dark light font size appearance 主题 字号 外观",
        setOf(SettingsPlatform.MOBILE, SettingsPlatform.DESKTOP),
    ),
    NOTIFICATIONS(
        Res.string.settings_hub_notifications,
        "notify voice whisper dictation phone 通知 语音",
        setOf(SettingsPlatform.MOBILE, SettingsPlatform.DESKTOP),
    ),
    SECURITY(
        Res.string.security_section,
        "face id touch lock biometric security 安全 锁屏",
        setOf(SettingsPlatform.MOBILE),
    ),
    WORKSPACE(
        Res.string.settings_tab_workspace,
        "terminal menu bar embedded external 终端 菜单栏",
        setOf(SettingsPlatform.DESKTOP),
    ),
    ADVANCED(
        Res.string.settings_hub_advanced,
        "context window tokens per-model advanced 上下文 窗口",
        setOf(SettingsPlatform.MOBILE, SettingsPlatform.DESKTOP),
    ),
    ACCOUNT(
        Res.string.settings_tab_account,
        "oauth preset api key account 账号 预设",
        setOf(SettingsPlatform.DESKTOP),
    ),
    COMPUTERS(
        Res.string.settings_tab_computers,
        "pair daemon computer host 电脑 配对",
        setOf(SettingsPlatform.DESKTOP),
    ),
    SCHEDULES(
        Res.string.settings_tab_schedules,
        "schedule cron timer 定时 任务",
        setOf(SettingsPlatform.MOBILE, SettingsPlatform.DESKTOP),
    ),
    SHARES(
        Res.string.settings_tab_shares,
        "share folder invite 共享 文件夹",
        setOf(SettingsPlatform.MOBILE, SettingsPlatform.DESKTOP),
    ),
    BRIDGES(
        Res.string.settings_bridges,
        "bridge bot feishu telegram 桥接 机器人",
        setOf(SettingsPlatform.MOBILE, SettingsPlatform.DESKTOP),
    ),
    SHORTCUTS(
        Res.string.settings_tab_shortcuts,
        "keyboard shortcut hotkey 快捷键",
        setOf(SettingsPlatform.DESKTOP),
    ),
    ABOUT(
        Res.string.settings_tab_about,
        "about version license mit update 关于 版本",
        setOf(SettingsPlatform.MOBILE, SettingsPlatform.DESKTOP),
    ),
    ;

    fun matches(query: String, resolvedTitle: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return true
        return resolvedTitle.lowercase().contains(q) || keywords.contains(q)
    }

    companion object {
        fun forPlatform(p: SettingsPlatform): List<SettingsSection> =
            entries.filter { p in it.platforms }
    }
}
