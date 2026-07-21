# 发布说明（ac54u-mobile/cc-pocket）

版本线分开：

| 线 | Tag | 内容 |
|---|---|---|
| Daemon | `daemon-vX.Y.Z` | 主机守护进程（标为 GitHub **Latest**） |
| App | `app-vX.Y.Z` | 手机安装包（TrollStore IPA 等，**不要**标 Latest） |

默认 relay：`wss://relay.txx.app`。App ID：`com.txx.ccpocket`。

## 用户安装

```bash
curl -fsSL https://raw.githubusercontent.com/ac54u-mobile/cc-pocket/main/scripts/install.sh | bash
cc-pocket-daemon pair
```

Windows：`irm https://raw.githubusercontent.com/ac54u-mobile/cc-pocket/main/scripts/install.ps1 | iex`

## 发 daemon

1. 本地或 CI 构建自包含包（`scripts/release-linux.sh` / `release.yml`，tag 为 `daemon-v…`）。
2. `gh release create daemon-vX.Y.Z … --latest`，附上 `SHA256SUMS`。
3. 安装脚本会优先解析 `daemon-v*`。

当前 Linux x86_64 已有：`daemon-v1.0.0`。

## 发 App（TrollStore IPA）

1. Actions → `ios-trollstore` → Run workflow。
2. Workflow 会创建/更新 `app-v<version>` 并上传 IPA（`--latest=false`）。
3. Bundle ID 必须是 `com.txx.ccpocket`。

## 本地校验

```bash
./gradlew :daemon:packageDaemon -PappVersion=1.0.0
bash scripts/check-all.sh
```

Homebrew / Scoop 模板在 `packaging/`，仅作参考；正式分发以 `scripts/install.sh` / `install.ps1` 为准。
