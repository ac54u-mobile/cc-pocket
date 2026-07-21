package dev.ccpocket.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ccpocket.app.APP_VERSION
import dev.ccpocket.app.data.PocketRepository
import dev.ccpocket.app.lock.AppLockController
import dev.ccpocket.app.lock.AutoLockDelay
import dev.ccpocket.app.pairing.displayName
import dev.ccpocket.app.resources.*
import dev.ccpocket.app.theme.ThemeMode
import dev.ccpocket.app.theme.Tok
import dev.ccpocket.app.ui.share.JoinFolderScreen
import dev.ccpocket.app.ui.share.SharedFoldersScreen
import dev.ccpocket.app.voice.NativeDictation
import dev.ccpocket.protocol.DEFAULT_CONTEXT_WINDOW
import dev.ccpocket.protocol.LARGE_CONTEXT_WINDOW
import org.jetbrains.compose.resources.stringResource

private val EFFORT_DEFAULT_OPTS: List<String?> = listOf(null) + EFFORT_OPTIONS
private val MODEL_DEFAULT_OPTS: List<String?> = listOf(null) + CLAUDE_MODEL_OPTIONS.map { it.second }
private val CONTEXT_WINDOW_OPTS: List<Long?> = listOf(null, DEFAULT_CONTEXT_WINDOW, LARGE_CONTEXT_WINDOW)
private val FONT_SCALE_STEPS: List<Float> = listOf(0.85f, 1.0f, 1.15f, 1.3f, 1.4f)

/** Settings detail destinations. Hub is the indexed landing page with search. */
private enum class SettingsDest {
    HUB, USAGE, SCHEDULES, SHARES, JOIN, BRIDGES,
    AGENT, APPEARANCE, NOTIFICATIONS, SECURITY, ADVANCED, ABOUT,
}

private data class HubEntry(
    val dest: SettingsDest,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val keywords: String,
)

/**
 * Settings: hub → category panes (searchable). Full-screen overlays (Usage / Schedules / Shares…)
 * keep their existing screens. [onBack] leaves Settings entirely.
 */
@Composable
fun SettingsScreen(repo: PocketRepository, onBack: () -> Unit) {
    var dest by remember { mutableStateOf(SettingsDest.HUB) }
    val goHub = { dest = SettingsDest.HUB }
    when (dest) {
        SettingsDest.USAGE -> { UsageScreen(repo, onBack = goHub); return }
        SettingsDest.SCHEDULES -> { ScheduleScreen(repo, onBack = goHub); return }
        SettingsDest.SHARES -> { SharedFoldersScreen(repo, onBack = goHub); return }
        SettingsDest.JOIN -> {
            JoinFolderScreen(repo, onBack = goHub, onJoined = { dest = SettingsDest.HUB; onBack() })
            return
        }
        SettingsDest.BRIDGES -> {
            dev.ccpocket.app.ui.bridge.BridgesScreen(repo, onBack = goHub)
            return
        }
        else -> Unit
    }

    dev.ccpocket.app.SystemBackHandler(enabled = true) {
        if (dest == SettingsDest.HUB) onBack() else dest = SettingsDest.HUB
    }

    when (dest) {
        SettingsDest.HUB -> SettingsHub(repo, onBack = onBack, onOpen = { dest = it })
        SettingsDest.AGENT -> SettingsPaneScaffold(stringResource(Res.string.settings_hub_agent), goHub) {
            AgentDefaultsPane(repo)
        }
        SettingsDest.APPEARANCE -> SettingsPaneScaffold(stringResource(Res.string.settings_hub_appearance), goHub) {
            AppearancePane(repo)
        }
        SettingsDest.NOTIFICATIONS -> SettingsPaneScaffold(stringResource(Res.string.settings_hub_notifications), goHub) {
            NotificationsPane(repo)
        }
        SettingsDest.SECURITY -> SettingsPaneScaffold(stringResource(Res.string.security_section), goHub) {
            SecurityGroup(repo.appLock)
        }
        SettingsDest.ADVANCED -> SettingsPaneScaffold(stringResource(Res.string.settings_hub_advanced), goHub) {
            AdvancedPane(repo)
        }
        SettingsDest.ABOUT -> SettingsPaneScaffold(stringResource(Res.string.about_section), goHub) {
            AboutPane(repo, onExit = { onBack(); repo.disconnect() })
        }
        else -> Unit
    }
}

@Composable
private fun SettingsPaneScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(Tok.base)) {
        SettingsTopBar(title, onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 28.dp),
        ) { content() }
    }
}

@Composable
private fun SettingsHub(repo: PocketRepository, onBack: () -> Unit, onOpen: (SettingsDest) -> Unit) {
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()

    val entries = listOf(
        HubEntry(
            SettingsDest.USAGE, stringResource(Res.string.settings_usage),
            stringResource(Res.string.usage_title), Icons.Rounded.Insights, Tok.info,
            "usage tokens cost chart 用量",
        ),
        HubEntry(
            SettingsDest.SCHEDULES, stringResource(Res.string.schedule_tasks_title),
            stringResource(Res.string.settings_hub_activity), Icons.Outlined.Schedule, Tok.warn,
            "schedule cron timer 定时 任务",
        ),
        HubEntry(
            SettingsDest.SHARES, stringResource(Res.string.settings_shared_folders),
            stringResource(Res.string.settings_hub_collaboration), Icons.Rounded.Share, Tok.accent,
            "share folder 共享 文件夹",
        ),
        HubEntry(
            SettingsDest.JOIN, stringResource(Res.string.join_title),
            stringResource(Res.string.settings_hub_collaboration), Icons.Rounded.PersonAdd, Tok.codex,
            "join invite code 加入",
        ),
        HubEntry(
            SettingsDest.BRIDGES, stringResource(Res.string.settings_bridges),
            stringResource(Res.string.settings_hub_collaboration), Icons.Outlined.SmartToy, Tok.opencode,
            "bridge bot feishu telegram 桥接 机器人",
        ),
        HubEntry(
            SettingsDest.AGENT, stringResource(Res.string.settings_hub_agent),
            stringResource(Res.string.settings_hub_agent_sub), Icons.Outlined.Tune, Tok.accent,
            "mode model effort agent filter claude codex opencode 模式 模型 推理",
        ),
        HubEntry(
            SettingsDest.APPEARANCE, stringResource(Res.string.settings_hub_appearance),
            stringResource(Res.string.settings_hub_appearance_sub), Icons.Outlined.DarkMode, Tok.info,
            "theme dark light font size appearance 主题 字号 外观",
        ),
        HubEntry(
            SettingsDest.NOTIFICATIONS, stringResource(Res.string.settings_hub_notifications),
            stringResource(Res.string.settings_hub_notifications_sub), Icons.Outlined.Notifications, Tok.warn,
            "notify voice whisper dictation 通知 语音",
        ),
        HubEntry(
            SettingsDest.SECURITY, stringResource(Res.string.security_section),
            stringResource(Res.string.settings_hub_security_sub), Icons.Outlined.Lock, Tok.ok,
            "face id touch lock biometric security 安全 锁屏",
        ),
        HubEntry(
            SettingsDest.ADVANCED, stringResource(Res.string.settings_hub_advanced),
            stringResource(Res.string.settings_hub_advanced_sub), Icons.Outlined.Shield, Tok.muted,
            "context window tokens per-model advanced 上下文 窗口",
        ),
        HubEntry(
            SettingsDest.ABOUT, stringResource(Res.string.about_section),
            stringResource(Res.string.settings_hub_about_sub), Icons.Outlined.Info, Tok.tx2,
            "about version license mit exit disconnect 关于 版本",
        ),
    )
    val filtered = if (q.isEmpty()) entries else entries.filter { e ->
        e.title.lowercase().contains(q) || e.subtitle.lowercase().contains(q) || e.keywords.contains(q)
    }

    Column(Modifier.fillMaxSize().background(Tok.base)) {
        SettingsTopBar(stringResource(Res.string.settings_title), onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 28.dp),
        ) {
            SettingsSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = stringResource(Res.string.settings_search_placeholder),
            )
            Spacer(Modifier.height(12.dp))
            SettingsStatusStrip(
                title = stringResource(Res.string.settings_connected),
                subtitle = repo.paired.value?.displayName()
                    ?: stringResource(Res.string.settings_direct_lan),
                actionLabel = stringResource(Res.string.settings_switch_computer),
                onAction = { onBack(); repo.disconnect() },
            )
            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Text(
                    stringResource(Res.string.settings_search_empty),
                    color = Tok.muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 28.dp).align(Alignment.CenterHorizontally),
                )
            } else if (q.isNotEmpty()) {
                SettingsSectionLabel(stringResource(Res.string.settings_search_placeholder))
                SettingsCard {
                    filtered.forEachIndexed { i, e ->
                        if (i > 0) SettingsDivider()
                        SettingsNavRow(
                            title = e.title,
                            subtitle = e.subtitle,
                            icon = e.icon,
                            iconTint = e.iconTint,
                            onClick = { onOpen(e.dest) },
                        )
                    }
                }
            } else {
                SettingsSectionLabel(stringResource(Res.string.settings_hub_activity))
                SettingsCard {
                    listOf(SettingsDest.USAGE, SettingsDest.SCHEDULES).forEachIndexed { i, d ->
                        if (i > 0) SettingsDivider()
                        val e = entries.first { it.dest == d }
                        SettingsNavRow(e.title, { onOpen(d) }, subtitle = e.subtitle, icon = e.icon, iconTint = e.iconTint)
                    }
                }

                SettingsSectionLabel(stringResource(Res.string.settings_hub_collaboration))
                SettingsCard {
                    listOf(SettingsDest.SHARES, SettingsDest.JOIN, SettingsDest.BRIDGES).forEachIndexed { i, d ->
                        if (i > 0) SettingsDivider()
                        val e = entries.first { it.dest == d }
                        SettingsNavRow(e.title, { onOpen(d) }, icon = e.icon, iconTint = e.iconTint)
                    }
                }

                SettingsSectionLabel(stringResource(Res.string.settings_title))
                SettingsCard {
                    listOf(
                        SettingsDest.AGENT, SettingsDest.APPEARANCE, SettingsDest.NOTIFICATIONS,
                        SettingsDest.SECURITY, SettingsDest.ADVANCED, SettingsDest.ABOUT,
                    ).forEachIndexed { i, d ->
                        if (i > 0) SettingsDivider()
                        val e = entries.first { it.dest == d }
                        SettingsNavRow(e.title, { onOpen(d) }, subtitle = e.subtitle, icon = e.icon, iconTint = e.iconTint)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentDefaultsPane(repo: PocketRepository) {
    SettingsSectionLabel(stringResource(Res.string.default_mode_section))
    SettingsChoiceList(
        options = MODES,
        selected = MODES.firstOrNull { it.key == repo.defaultMode.value } ?: MODES.first(),
        label = { stringResource(it.label) },
        onPick = { repo.setDefaultMode(it.key) },
        leading = { m ->
            Text("●", color = m.color, fontSize = 9.sp, modifier = Modifier.padding(end = 10.dp))
        },
    )
    SettingsConsequenceHint(stringResource(Res.string.settings_consequence_new_session))

    SettingsSectionLabel(stringResource(Res.string.default_model_section))
    val modelDefaultLabel = stringResource(Res.string.value_default)
    SettingsChoiceList(
        options = MODEL_DEFAULT_OPTS,
        selected = repo.defaultModel.value,
        label = { it ?: modelDefaultLabel },
        trailing = { it },
        onPick = { repo.setDefaultModel(it) },
    )
    SettingsConsequenceHint(stringResource(Res.string.default_model_hint))

    SettingsSectionLabel(stringResource(Res.string.default_effort_section))
    val effortDefaultLabel = stringResource(Res.string.value_default)
    SettingsChoiceList(
        options = EFFORT_DEFAULT_OPTS,
        selected = repo.defaultEffort.value,
        label = { it ?: effortDefaultLabel },
        onPick = { repo.setDefaultEffort(it) },
    )
    SettingsConsequenceHint(stringResource(Res.string.settings_consequence_effort))

    SettingsSectionLabel(stringResource(Res.string.af_show_from))
    val afOpts = listOf(
        Triple("both", stringResource(Res.string.af_both), null as Color?),
        Triple("claude", stringResource(Res.string.af_claude_only), Tok.accent),
        Triple("codex", stringResource(Res.string.af_codex_only), Tok.codex),
        Triple("opencode", stringResource(Res.string.af_opencode_only), Tok.opencode),
    )
    val afSelected = afOpts.firstOrNull { it.first == repo.agentFilter.value } ?: afOpts.first()
    SettingsChoiceList(
        options = afOpts,
        selected = afSelected,
        label = { it.second },
        onPick = { repo.setAgentFilter(it.first) },
        leading = { opt ->
            opt.third?.let { c ->
                Box(Modifier.padding(end = 10.dp).size(7.dp).clip(CircleShape).background(c))
            }
        },
    )
    SettingsConsequenceHint(stringResource(Res.string.settings_consequence_filter))
}

@Composable
private fun AppearancePane(repo: PocketRepository) {
    SettingsSectionLabel(stringResource(Res.string.appearance_section))
    SettingsCard {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Tok.base)
                    .border(1.dp, Tok.hair, RoundedCornerShape(10.dp)).padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val modes = listOf(
                    ThemeMode.SYSTEM to stringResource(Res.string.appearance_system),
                    ThemeMode.LIGHT to stringResource(Res.string.appearance_light),
                    ThemeMode.DARK to stringResource(Res.string.appearance_dark),
                )
                modes.forEach { (mode, label) ->
                    val sel = repo.themeMode.value == mode
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(7.dp))
                            .then(if (sel) Modifier.background(Tok.accent) else Modifier)
                            .clickable { repo.setThemeMode(mode) }.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            color = if (sel) Tok.base else Tok.tx2,
                            fontSize = 13.sp,
                            fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                }
            }
            SettingsHint(stringResource(Res.string.appearance_hint), Modifier.padding(top = 10.dp, start = 0.dp))
        }
    }

    SettingsSectionLabel(stringResource(Res.string.text_size_section))
    SettingsCard {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Tok.base)
                    .border(1.dp, Tok.hair, RoundedCornerShape(10.dp)).padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FONT_SCALE_STEPS.forEachIndexed { i, s ->
                    val sel = repo.fontScale.value in (s - 0.04f)..(s + 0.04f)
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(7.dp))
                            .then(if (sel) Modifier.background(Tok.accent) else Modifier)
                            .clickable { repo.setFontScale(s) }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "A",
                            color = if (sel) Tok.base else Tok.tx2,
                            fontSize = (11f + i * 2.5f).sp,
                            fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
            Box(
                Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(8.dp)).background(Tok.base)
                    .border(1.dp, Tok.hair, RoundedCornerShape(8.dp)).padding(12.dp),
            ) {
                Text(stringResource(Res.string.text_size_sample), color = Tok.tx, fontSize = 14.sp * repo.fontScale.value)
            }
        }
    }
}

@Composable
private fun NotificationsPane(repo: PocketRepository) {
    SettingsSectionLabel(stringResource(Res.string.notifications_section))
    SettingsCard {
        SettingsToggleRow(
            label = stringResource(Res.string.notify_on_complete),
            sub = stringResource(Res.string.notify_on_complete_sub),
            checked = repo.notificationsOn.value,
            onChange = { repo.setNotificationsEnabled(it) },
        )
    }
    if (NativeDictation.available) {
        SettingsSectionLabel(stringResource(Res.string.voice_section))
        SettingsCard {
            SettingsToggleRow(
                label = stringResource(Res.string.voice_use_whisper),
                sub = stringResource(Res.string.voice_use_whisper_sub),
                checked = repo.voiceWhisper.value,
                onChange = { repo.setVoiceWhisper(it) },
            )
        }
    }
}

@Composable
private fun AdvancedPane(repo: PocketRepository) {
    SettingsSectionLabel(stringResource(Res.string.context_window_section))
    val ctxDefaultLabel = stringResource(Res.string.value_default)
    var catchAllEdited by remember { mutableStateOf(false) }
    val ctxCurrent = repo.contextWindowOverride.value
    // Custom values aren't in the preset list — leave nothing checked (field below is the source of truth).
    val ctxSelectedForList: Long? =
        if (ctxCurrent == null || ctxCurrent == DEFAULT_CONTEXT_WINDOW || ctxCurrent == LARGE_CONTEXT_WINDOW) ctxCurrent
        else -1L
    SettingsChoiceList(
        options = CONTEXT_WINDOW_OPTS,
        selected = ctxSelectedForList,
        label = { opt ->
            when (opt) {
                null -> ctxDefaultLabel
                LARGE_CONTEXT_WINDOW -> "1M"
                else -> "${opt / 1000}K"
            }
        },
        trailing = { opt ->
            when (opt) {
                null -> stringResource(Res.string.settings_follow_model)
                LARGE_CONTEXT_WINDOW -> "1,000,000"
                else -> "200,000"
            }
        },
        onPick = { catchAllEdited = true; repo.setContextWindowOverride(it) },
    )
    ContextWindowCustomRow(repo) { catchAllEdited = true }
    val shadowing = repo.contextWindowOverrides.keys.sorted()
    if (catchAllEdited && shadowing.isNotEmpty()) CatchAllShadowedNote(shadowing)
    else SettingsHint(stringResource(Res.string.context_window_hint))
    PerModelWindows(repo)
}

@Composable
private fun AboutPane(repo: PocketRepository, onExit: () -> Unit) {
    SettingsSectionLabel(stringResource(Res.string.about_section))
    SettingsCard {
        AboutRow(stringResource(Res.string.about_version), APP_VERSION)
        SettingsDivider()
        AboutRow(stringResource(Res.string.about_license), "MIT")
        SettingsDivider()
        AboutRow(
            stringResource(Res.string.about_connection),
            repo.paired.value?.displayName() ?: stringResource(Res.string.settings_direct_lan),
        )
    }
    Row(
        Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(12.dp)).background(Tok.surface)
            .border(1.dp, Tok.hair, RoundedCornerShape(12.dp))
            .clickable(onClick = onExit).padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.exit), color = Tok.danger, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
    }
}

/** Security group (issue #109): Face ID switch + auto-lock sub-row. */
@Composable
private fun SecurityGroup(lock: AppLockController) {
    val kindName = biometryName(lock.biometryKind)
    val enableReason = stringResource(Res.string.app_lock_enable_reason)
    var showAutoLock by remember { mutableStateOf(false) }
    val showSub = lock.enabled.value && !lock.enabling.value

    SettingsCard {
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FaceIdGlyph(color = Tok.accent, size = 22.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text(stringResource(Res.string.app_lock_require, kindName), color = Tok.tx, fontSize = 14.sp)
                Text(stringResource(Res.string.app_lock_require_sub, kindName), color = Tok.muted, fontSize = 11.5.sp, lineHeight = 15.sp)
            }
            Switch(
                checked = lock.enabled.value,
                enabled = !lock.enabling.value && (lock.enabled.value || lock.canUseBiometrics()),
                onCheckedChange = { on -> if (on) lock.requestEnable(enableReason) else lock.disable() },
            )
        }
        if (showSub) {
            SettingsDivider()
            Row(
                Modifier.fillMaxWidth().clickable { showAutoLock = true }
                    .padding(start = 14.dp, end = 12.dp, top = 13.dp, bottom = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(34.dp))
                Text(stringResource(Res.string.app_lock_autolock), color = Tok.tx, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(autoLockText(lock.autoLock.value), color = Tok.tx2, fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.ChevronRight, null, tint = Tok.muted, modifier = Modifier.size(18.dp))
            }
        }
    }
    if (lock.enabling.value) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            FaceIdGlyph(color = Tok.muted, size = 14.dp)
            Spacer(Modifier.width(7.dp))
            Text(stringResource(Res.string.app_lock_verifying, kindName), color = Tok.muted, fontSize = 11.5.sp)
        }
    }
    if (showAutoLock) AutoLockSheet(lock) { showAutoLock = false }
}

@Composable
private fun AutoLockSheet(lock: AppLockController, onDismiss: () -> Unit) {
    PocketSheet(onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text(
                stringResource(Res.string.app_lock_autolock), color = Tok.muted, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp,
                modifier = Modifier.padding(start = 18.dp, top = 4.dp, bottom = 6.dp),
            )
            AutoLockDelay.entries.forEach { d ->
                val sel = lock.autoLock.value == d
                Row(
                    Modifier.fillMaxWidth().clickable { lock.setAutoLock(d); onDismiss() }.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        autoLockText(d),
                        color = if (sel) Tok.tx else Tok.tx2,
                        fontSize = 15.sp,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                    )
                    if (sel) Text("✓", color = Tok.accent, fontSize = 14.sp)
                }
            }
            Text(
                stringResource(Res.string.app_lock_autolock_hint),
                color = Tok.muted, fontSize = 11.5.sp, lineHeight = 16.sp,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 6.dp),
            )
        }
    }
}

@Composable
private fun autoLockText(d: AutoLockDelay): String = stringResource(
    when (d) {
        AutoLockDelay.IMMEDIATELY -> Res.string.app_lock_immediately
        AutoLockDelay.AFTER_1_MIN -> Res.string.app_lock_after_1min
    },
)

@Composable
private fun ContextWindowCustomRow(repo: PocketRepository, onEdit: () -> Unit = {}) {
    val current = repo.contextWindowOverride.value
    val isCustom = current != null && current != DEFAULT_CONTEXT_WINDOW && current != LARGE_CONTEXT_WINDOW
    var draft by remember { mutableStateOf(if (isCustom) current.toString() else "") }
    Row(
        Modifier.padding(top = 8.dp).fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(if (isCustom) Tok.raised else Tok.surface)
            .border(1.dp, if (isCustom) Tok.accent else Tok.hair, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.context_window_custom), color = Tok.tx2, fontSize = 13.sp, modifier = Modifier.weight(1f))
        OutlinedTextField(
            draft,
            { new ->
                draft = new.filter(Char::isDigit).take(9)
                onEdit()
                repo.setContextWindowOverride(draft.toLongOrNull()?.takeIf { it > 0 })
            },
            placeholder = { Text(stringResource(Res.string.context_window_tokens), color = Tok.muted, fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Tok.tx),
            modifier = Modifier.width(130.dp),
        )
    }
}

internal fun groupDigits(n: Long): String =
    n.toString().reversed().chunked(3).joinToString(",").reversed()

private fun Modifier.dashedBorder(color: Color, radius: Dp) = this.drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 6f))),
        cornerRadius = CornerRadius(radius.toPx()),
    )
}

@Composable
private fun CatchAllShadowedNote(shadowing: List<String>) {
    val text =
        if (shadowing.size == 1) stringResource(Res.string.ctx_conflict_one, shadowing[0])
        else stringResource(Res.string.ctx_conflict_many, shadowing[0], shadowing.size - 1)
    Row(
        Modifier.padding(top = 11.dp).fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(Tok.warn.copy(alpha = 0.09f))
            .border(1.dp, Tok.warn.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
            .padding(horizontal = 11.dp, vertical = 10.dp),
    ) {
        Text(text, color = Tok.warn, fontSize = 12.5.sp, lineHeight = 18.sp)
    }
}

@Composable
internal fun PerModelWindows(repo: PocketRepository) {
    Spacer(Modifier.height(22.dp))
    SettingsSectionLabel(stringResource(Res.string.per_model_section))
    val entries = repo.contextWindowOverrides.entries.sortedBy { it.key }.map { it.key to it.value }
    if (entries.isEmpty()) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).dashedBorder(Tok.hair, 14.dp)
                .padding(horizontal = 14.dp, vertical = 16.dp),
        ) {
            Text(stringResource(Res.string.per_model_empty_title), color = Tok.tx2, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
            Text(
                stringResource(Res.string.per_model_empty_body), color = Tok.muted, fontSize = 12.5.sp,
                lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp),
            )
        }
        return
    }
    val liveKey = repo.contextWindowKeyOf(repo.model.value)
    SettingsCard {
        entries.forEachIndexed { i, (id, tokens) ->
            if (i > 0) SettingsDivider()
            PerModelRow(id, tokens, live = id == liveKey) { repo.setContextWindowOverrideFor(id, null) }
        }
    }
    SettingsHint(stringResource(Res.string.per_model_hint))
}

@Composable
private fun PerModelRow(id: String, tokens: Long, live: Boolean, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = 14.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                id, color = Tok.tx, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            if (live) Text(
                stringResource(Res.string.per_model_overrides), color = Tok.warn,
                fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp),
            )
        }
        Text(
            groupDigits(tokens), color = Tok.tx2, fontFamily = FontFamily.Monospace,
            fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp),
        )
        val deleteLabel = stringResource(Res.string.per_model_delete)
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                .semantics { contentDescription = deleteLabel }
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.DeleteOutline, null, tint = Tok.muted, modifier = Modifier.size(18.dp))
        }
    }
}

/** Label/value row used by settings + session-info sheets. */
@Composable
fun AboutRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Tok.tx2, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
        Text(value, color = Tok.tx, fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, maxLines = 1)
    }
}
