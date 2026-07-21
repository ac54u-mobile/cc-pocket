# CLAUDE.md — CC Pocket

给编码助手的仓库速记。用户文档见 [docs/](docs/)。

手机 / 桌面 App 经零知识 E2E relay 驱动主机上的 Claude Code、OpenAI Codex、Cursor Agent。

| 模块 | 路径 |
|---|---|
| App | `mobile/`（Compose Multiplatform） |
| daemon | `daemon/`（Kotlin/JVM） |
| relay | `relay/`（Ktor；默认 `wss://relay.txx.app`） |
| 协议 | `protocol/` |

发行：GitHub `ac54u-mobile/cc-pocket`。App ID：`com.txx.ccpocket`。  
版本线：App **`app-v*`** · daemon **`daemon-v*`**（当前 daemon **1.0.0**）。

## 本机 daemon 铁律

**症状**：手机连不上 / 卡死 / 状态乱跳 / 会话疯狂 fork。  
**根因几乎总是同时跑了两个 daemon**——抢同一 relay 账号 + 端口 8799。

### macOS 开发机更新

```bash
bash scripts/update-local-daemon.sh
```

幂等：`installDist` → 安装 → 杀干净现存 daemon + 清 8799 → `service-install` → 校验单实例。

在 **本 App 开的会话里**不要直接跑上面脚本。改用：

```bash
bash scripts/update-local-daemon-detached.sh
```

### 绝对不要做

- `./gradlew :daemon:run`（与常驻实例抢账号）
- 直接跑 `daemon/build/install/.../bin/cc-pocket-daemon`（同上）
- `nohup … run &` 测完忘杀

### 只读排查

```bash
ps aux | grep 'cc-pocket-daemon/lib' | grep -v grep | wc -l   # 应 = 1
lsof -nP -iTCP:8799 -sTCP:LISTEN
```

## relay

```bash
./gradlew :relay:installDist
bash scripts/redeploy-relay.sh   # 读 .env 的 RELAY_HOST / SSHPASS
```

`MAX_FRAME` 源码为 **4MB**；改完必须重新部署。

## 构建速记

```bash
bash scripts/check-all.sh
./gradlew :mobile:composeApp:compileKotlinDesktop
python3 scripts/probe-claude-wire.py   # 升级 claude CLI 后必跑
```

TrollStore IPA：`.github/workflows/ios-trollstore.yml` → 发布到 `app-v*`。
