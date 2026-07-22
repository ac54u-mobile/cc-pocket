# iOS 与 Codex IDE 实时双向同步教程

CC Pocket 可以让 iPhone 和 Codex IDE 打开同一个 Codex thread。任意一端发送消息后，另一端会实时看到用户消息、流式回复、完成状态、Token 用量和限额信息。

这不是聊天记录定时刷新：iOS 和 IDE 会同时连接同一个 Codex managed app-server，并订阅同一个 thread。

## 支持范围

| 组件 | 最低版本／状态 |
|---|---|
| CC Pocket iOS App | `1.5.3 build 6` |
| CC Pocket daemon | `1.0.3` |
| Codex CLI | standalone managed 安装版，建议 `0.145.0+` |
| Linux | 已完成真实双客户端端到端验证 |
| macOS | 支持相同的 Unix Socket 链路 |
| Windows | 普通 Codex 对话可用；IDE 实时同步暂不支持，会安全回退到独立 app-server |

> 关键要求：Codex IDE、Codex managed app-server 和 CC Pocket daemon 必须由**同一个系统用户**运行。不要混用 `sudo`、`root` 和普通用户，否则它们会使用不同的 `~/.codex` 与 socket。

## 一、安装 iOS App

需要 iOS 17+ 和 TrollStore：

- [下载 CC Pocket 1.5.3 build 6 IPA](https://github.com/ac54u-mobile/cc-pocket/releases/download/app-v1.5.3/cc-pocket-1.5.3-build6-trollstore.ipa)
- 在 TrollStore 中打开并安装 IPA。

如果设备上已有旧版，直接覆盖安装即可；配对信息通常会保留。

## 二、安装或升级 CC Pocket daemon

Linux／macOS：

```bash
curl -fsSL https://raw.githubusercontent.com/ac54u-mobile/cc-pocket/main/scripts/install.sh | bash
cc-pocket-daemon --version
```

版本必须为 `1.0.3` 或更高。也可以从 [daemon-v1.0.3](https://github.com/ac54u-mobile/cc-pocket/releases/tag/daemon-v1.0.3) 手动下载安装包。

首次使用时配对手机：

```bash
cc-pocket-daemon pair
```

在 iPhone 上扫描二维码或输入 6 位配对码。

## 三、安装 managed Codex

双端同步不能使用旧的 npm-managed Codex，必须使用 Codex 官方 installer 管理的 standalone 版本：

```bash
curl -fsSL https://chatgpt.com/codex/install.sh | sh
```

安装器若发现 npm 全局版本，可按提示卸载。然后重新打开终端，或刷新 shell 命令缓存：

```bash
hash -r
which codex
codex --version
```

Linux／macOS 的预期路径类似：

```text
~/.local/bin/codex
```

再确认 managed 固定路径存在：

```bash
ls -l ~/.codex/packages/standalone/current/codex
```

如果这里不存在，说明当前并非 installer 管理的 standalone 安装，IDE 同步不会启用。

## 四、启动 managed app-server

```bash
codex app-server daemon start
codex app-server daemon version
```

成功时应看到类似结果：

```json
{
  "status": "running",
  "managedCodexVersion": "0.145.0",
  "socketPath": "/home/you/.codex/app-server-control/app-server-control.sock"
}
```

确认 socket 存在：

```bash
test -S ~/.codex/app-server-control/app-server-control.sock && echo OK
```

不要另外手动启动第二个 app-server。managed daemon 负责维护这个唯一实例。

## 五、确认 CC Pocket daemon 正常

Linux：

```bash
systemctl --user status cc-pocket-daemon.service --no-pager
journalctl --user -u cc-pocket-daemon.service -n 50 --no-pager
```

macOS：

```bash
launchctl print gui/$(id -u)/dev.ccpocket.daemon
```

如果刚升级过，可以重启一次服务，确保加载的是新版本：

```bash
cc-pocket-daemon update
```

Linux 上还可以确认只有一个 daemon：

```bash
pgrep -af 'cc-pocket-daemon/lib'
```

只应看到一个常驻 daemon。不要同时运行源码版、前台版和系统服务版。

## 六、打开同一个 Codex 会话

1. 在 Codex IDE 中打开项目并创建或打开一个会话。
2. 在 iPhone 打开 CC Pocket，选择运行 Codex IDE 的那台电脑。
3. 进入相同工作目录。
4. 在历史会话列表中选择 IDE 当前的 Codex 会话，而不是新建另一个会话。
5. 等待聊天页显示该会话的已有内容。

只有 thread id 相同才会同步。同一个项目目录下新建的两个会话仍然是两个独立 thread，不会互相复制消息。

## 七、双向验收

先从 iPhone 发送：

```text
同步测试 A：请回复 PHONE_OK
```

预期：

- Codex IDE 立即出现这条用户消息；
- IDE 与 iPhone 同时看到流式回复；
- 两端最终都显示 `PHONE_OK` 和完成状态。

再从 Codex IDE 发送：

```text
同步测试 B：请回复 IDE_OK
```

预期：

- iPhone 立即出现 IDE 发出的用户消息；
- 回复在两端同步流式更新；
- iPhone 不会生成重复的用户气泡；
- Token 已用百分比和重置时间随后更新。

## 八、故障排查

### iPhone 一直显示“思考中”

依次执行：

```bash
codex app-server daemon version
test -S ~/.codex/app-server-control/app-server-control.sock && echo socket-ok
cc-pocket-daemon --version
```

Codex 应为 managed standalone，CC Pocket daemon 应为 `1.0.3+`。旧 daemon 不包含 `/rpc` WebSocket 桥接修复。

### IDE 与手机都能聊天，但互相看不到

最常见的原因：

1. 两端打开的不是同一个 thread；回到会话列表重新选择 IDE 当前会话。
2. IDE 和 daemon 由不同系统用户运行。
3. 使用 `sudo codex ...` 启动了另一个用户的 app-server。
4. Windows 正在使用安全回退模式，当前不支持 IDE 实时同步。

检查进程所属用户：

```bash
ps -eo user,pid,cmd | grep -E 'codex.*app-server|cc-pocket-daemon' | grep -v grep
```

相关进程的用户应一致。

### 报错 `managed standalone Codex install not found`

重新使用官方 installer：

```bash
curl -fsSL https://chatgpt.com/codex/install.sh | sh
hash -r
codex app-server daemon start
```

### 报错无法连接 `app-server-control.sock`

先启动 managed daemon：

```bash
codex app-server daemon start
codex app-server daemon version
```

如果仍失败，确认当前 shell、IDE 和 CC Pocket 服务使用相同账户及相同 `HOME`。

### Linux daemon 日志

```bash
journalctl --user -u cc-pocket-daemon.service -f
```

### macOS daemon 日志

先用下面命令确认服务：

```bash
launchctl print gui/$(id -u)/dev.ccpocket.daemon
```

若需要前台诊断，先停止系统服务，避免两个 daemon 抢 relay 账号与端口；诊断完成后再恢复服务。

## 工作原理

```text
iPhone App ──E2E relay── CC Pocket daemon ──WebSocket /rpc── managed app-server
                                                               ├── Codex IDE
                                                               └── 同一个 thread
```

CC Pocket daemon 会：

1. 连接当前用户的 managed Unix Socket；
2. 完成 `/rpc` WebSocket Upgrade；
3. 以全双工方式转发 JSON-RPC；
4. 按 thread id 过滤事件；
5. 将 IDE 产生的用户消息推送到手机；
6. 对手机自己发送的消息做去重；
7. 同步流式回复、完成状态、Token 用量和 rate limit。

managed transport 不可用时，daemon 会安全回退到独立 app-server，避免聊天永久卡住。回退后普通 Codex 聊天仍可用，但 IDE 与手机不会实时共享界面。
