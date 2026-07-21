# CC Pocket

[![CI](https://github.com/ac54u-mobile/cc-pocket/actions/workflows/ci.yml/badge.svg)](https://github.com/ac54u-mobile/cc-pocket/actions/workflows/ci.yml) [![Latest release](https://img.shields.io/github/v/release/ac54u-mobile/cc-pocket)](https://github.com/ac54u-mobile/cc-pocket/releases/latest) [![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

[English](README.md) | **简体中文**

用手机经**零知识端到端中继**操控本机 Claude Code / Codex / OpenCode。本仓库由 [ac54u-mobile](https://github.com/ac54u-mobile/cc-pocket) 维护，默认 relay 为 `wss://relay.txx.app`。

应用 ID：`com.txx.ccpocket`。版本线分开：

| 线 | Tag | 内容 |
|---|---|---|
| Daemon | `daemon-v*` | 主机守护进程（GitHub **Latest**） |
| App | `app-v*` | 手机安装包（TrollStore IPA） |

## 获取

| | 平台 | 下载 |
|---|---|---|
| 手机 | iOS 17+（TrollStore） | [App Releases](https://github.com/ac54u-mobile/cc-pocket/releases?q=app-v) |
| Daemon | macOS · Linux | `curl -fsSL https://raw.githubusercontent.com/ac54u-mobile/cc-pocket/main/scripts/install.sh \| bash` |
| Daemon | Windows | `irm https://raw.githubusercontent.com/ac54u-mobile/cc-pocket/main/scripts/install.ps1 \| iex` |

## 安装与配对

1. 在跑 agent CLI 的电脑上安装 daemon。
2. 执行 `cc-pocket-daemon pair`（二维码 + 6 位码）。
3. 打开手机 App 扫码或输入。

默认 relay：`wss://relay.txx.app`（可用 `CC_POCKET_RELAY` 覆盖）。

## 文档

- [USAGE](docs/USAGE.md) — 日常使用
- [SECURITY](docs/SECURITY.md) — 威胁模型
- [RUN](docs/RUN.md) — 本地开发
- [RELEASE](docs/RELEASE.md) — 发版说明

## 构建

```bash
./gradlew :daemon:installDist
./gradlew :mobile:composeApp:compileKotlinDesktop
bash scripts/check-all.sh
```

MIT — 见 [LICENSE](LICENSE)。
