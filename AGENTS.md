# AGENTS.md — CC Pocket

手机 App 经零知识 E2E relay 驱动本机 agent CLI。模块：`mobile/`、`daemon/`、`relay/`、`protocol/`。

- 仓库：`ac54u-mobile/cc-pocket`
- 默认 relay：`wss://relay.txx.app`
- App ID：`com.txx.ccpocket`
- 版本：App `app-v*` · daemon `daemon-v*`

## daemon 铁律

不要同时跑两个 daemon（抢 relay 账号 + 8799）。更新用：

```bash
bash scripts/update-local-daemon.sh
```

App 会话内改用 `bash scripts/update-local-daemon-detached.sh`。

禁止：`./gradlew :daemon:run`、直接跑 `build/install/...` 启动常驻实例。

## 构建

```bash
bash scripts/check-all.sh
./gradlew :mobile:composeApp:compileKotlinDesktop
```

详见 [CLAUDE.md](CLAUDE.md)、[docs/RUN.md](docs/RUN.md)。
