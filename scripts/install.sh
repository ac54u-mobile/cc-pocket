#!/usr/bin/env bash
# CC Pocket daemon 一键安装（macOS arm64/x86_64 · Linux x86_64/arm64）：
#
#   curl -fsSL https://raw.githubusercontent.com/ac54u-mobile/cc-pocket/main/scripts/install.sh | bash
#
# 下载自包含构建（自带 JRE，无需系统 Java），校验 SHA256SUMS，安装到：
#   ~/.local/share/cc-pocket/versions/<ver>/
#   ~/.local/bin/cc-pocket-daemon -> versions/<ver>/…（稳定符号链接）
# 并注册后台服务（macOS launchd / Linux systemd --user），默认连接
#   wss://relay.txx.app
#
# 可重复执行以升级。环境变量：
#   CC_POCKET_VERSION=daemon-vX.Y.Z  固定 daemon 版本（默认 latest；也兼容 vX.Y.Z）
#   CC_POCKET_REPO=owner/repo        安装脚本所属仓库（默认 ac54u-mobile/cc-pocket）
#   CC_POCKET_ASSET_REPO=…           二进制 Release 仓库（默认先试 CC_POCKET_REPO，没有则回退 heypandax/cc-pocket）
#   CC_POCKET_RELAY=wss://…          默认 relay（默认 wss://relay.txx.app）
#   CC_POCKET_ROOT / CC_POCKET_BIN / CC_POCKET_NO_SERVICE=1
#
# 版本约定：daemon 用 tag daemon-vX.Y.Z；App 安装包用 app-vX.Y.Z（本脚本只装 daemon）。
set -euo pipefail

REPO="${CC_POCKET_REPO:-ac54u-mobile/cc-pocket}"
FALLBACK_ASSET_REPO="heypandax/cc-pocket"
BIN="cc-pocket-daemon"
ROOT="${CC_POCKET_ROOT:-$HOME/.local/share/cc-pocket}"
BINDIR="${CC_POCKET_BIN:-$HOME/.local/bin}"
VERSION="${CC_POCKET_VERSION:-latest}"
RELAY="${CC_POCKET_RELAY:-wss://relay.txx.app}"
ASSET_REPO="${CC_POCKET_ASSET_REPO:-}"

say()  { printf '\033[1;36m==>\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33mwarning:\033[0m %s\n' "$*" >&2; }
err()  { printf '\033[1;31merror:\033[0m %s\n'  "$*" >&2; exit 1; }

# 解析最新 **daemon** Release tag（优先 daemon-v*，跳过 app-v*）
resolve_latest() {
  local repo="$1" tag=""
  if command -v python3 >/dev/null 2>&1; then
    tag="$(curl -fsSL -H 'Accept: application/vnd.github+json' -H 'User-Agent: cc-pocket-install' \
      "https://api.github.com/repos/$repo/releases?per_page=30" 2>/dev/null \
      | python3 -c '
import json, sys
try:
    rels = json.load(sys.stdin)
except Exception:
    sys.exit(0)
def has_daemon(r):
    return any(str(a.get("name", "")).startswith("cc-pocket-daemon-") for a in (r.get("assets") or []))
daemon, plain = [], []
for r in rels:
    if r.get("draft") or not has_daemon(r):
        continue
    t = str(r.get("tag_name") or "")
    if t.startswith("app-v") or t.startswith("app/"):
        continue
    if t.startswith("daemon-v") or t.startswith("daemon/"):
        daemon.append(t)
    else:
        plain.append(t)
pick = (daemon or plain or [""])[0]
print(pick, end="")
' 2>/dev/null || true)"
  fi
  if [ -n "$tag" ]; then
    printf '%s\n' "$tag"
    return 0
  fi
  # 回退：GitHub /releases/latest 重定向
  curl -fsSL -o /dev/null -w '%{url_effective}' "https://github.com/$repo/releases/latest" \
    | sed -E 's#.*/tag/##'
}

# tag → 裸版本号（asset 文件名用）
daemon_ver() {
  local t="$1"
  t="${t#daemon-v}"; t="${t#daemon/}"; t="${t#v}"
  printf '%s\n' "$t"
}

# --- platform ---
os="$(uname -s)"; arch="$(uname -m)"
case "$os" in
  Darwin) plat="macos" ;;
  Linux)  plat="linux" ;;
  *) err "不支持的系统: $os（Windows: irm https://raw.githubusercontent.com/$REPO/main/scripts/install.ps1 | iex）" ;;
esac
case "$arch" in
  x86_64|amd64)  arch="x86_64" ;;
  aarch64|arm64) arch="arm64" ;;
  *) err "不支持的架构: $arch" ;;
esac
for c in curl tar; do command -v "$c" >/dev/null 2>&1 || err "缺少命令: $c"; done
shasum_cmd=""
if command -v sha256sum >/dev/null 2>&1; then shasum_cmd="sha256sum"
elif command -v shasum   >/dev/null 2>&1; then shasum_cmd="shasum -a 256"; fi

# --- resolve version + asset repo ---
if [ "$VERSION" = "latest" ]; then
  say "解析最新 Release"
  if [ -n "$ASSET_REPO" ]; then
    VERSION="$(resolve_latest "$ASSET_REPO")" \
      || err "无法解析 $ASSET_REPO 的最新 daemon 版本（可设 CC_POCKET_VERSION=daemon-vX.Y.Z）"
  else
    if VERSION="$(resolve_latest "$REPO" 2>/dev/null)" && [ -n "$VERSION" ] && \
       [[ "$VERSION" == daemon-v* || "$VERSION" == daemon/* || "$VERSION" == v* || "$VERSION" =~ ^[0-9] ]]; then
      ASSET_REPO="$REPO"
    else
      warn "$REPO 尚无 daemon Release，回退使用 $FALLBACK_ASSET_REPO 的二进制（relay 仍为 $RELAY）"
      ASSET_REPO="$FALLBACK_ASSET_REPO"
      VERSION="$(resolve_latest "$ASSET_REPO")" \
        || err "无法解析最新版本（可设 CC_POCKET_VERSION=daemon-vX.Y.Z）"
    fi
  fi
else
  : "${ASSET_REPO:=$REPO}"
  # 指定版本时若本仓没有该 asset，再试上游
  if [ "$ASSET_REPO" = "$REPO" ] && [ "$REPO" != "$FALLBACK_ASSET_REPO" ]; then
    ver_probe="$(daemon_ver "$VERSION")"
    probe_url="https://github.com/$ASSET_REPO/releases/download/${VERSION}/${BIN}-${ver_probe}-${plat}-${arch}.tar.gz"
    if ! curl -fsSI "$probe_url" >/dev/null 2>&1; then
      warn "$ASSET_REPO 没有 ${VERSION} 的 ${plat}/${arch} 包，回退 $FALLBACK_ASSET_REPO"
      ASSET_REPO="$FALLBACK_ASSET_REPO"
    fi
  fi
fi
[ -n "$VERSION" ] || err "版本为空"
ver="$(daemon_ver "$VERSION")"
asset="${BIN}-${ver}-${plat}-${arch}.tar.gz"
base="https://github.com/$ASSET_REPO/releases/download/${VERSION}"

# --- download + verify ---
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
say "下载 $asset  （来源 $ASSET_REPO）"
curl -fSL --progress-bar "$base/$asset" -o "$tmp/$asset" \
  || err "下载失败: $base/$asset — 若 404，说明该版本尚无 ${plat}/${arch} 构建；或从源码构建: ./gradlew :daemon:packageDaemon"
if [ -n "$shasum_cmd" ] && curl -fsSL "$base/SHA256SUMS" -o "$tmp/SHA256SUMS" 2>/dev/null; then
  expected="$(awk -v a="$asset" '$2==a || $2=="*"a {print tolower($1)}' "$tmp/SHA256SUMS" | head -1)"
  if [ -n "$expected" ]; then
    actual="$($shasum_cmd "$tmp/$asset" | awk '{print tolower($1)}')"
    [ "$actual" = "$expected" ] || err "校验失败 $asset（期望 $expected，实际 $actual）"
    say "校验通过"
  else
    warn "SHA256SUMS 中没有 $asset — 跳过校验"
  fi
else
  warn "此 Release 无 SHA256SUMS（或本机无 sha256 工具）— 跳过校验"
fi

# --- install into the versioned layout + atomic symlink flip ---
dest="$ROOT/versions/$ver"
say "安装到 $dest"
rm -rf "$dest"; mkdir -p "$dest" "$BINDIR"
tar -xzf "$tmp/$asset" -C "$tmp"
if [ -d "$tmp/$BIN.app" ]; then
  mv "$tmp/$BIN.app" "$dest/"
  launcher="$dest/$BIN.app/Contents/MacOS/$BIN"
elif [ -d "$tmp/$BIN" ]; then
  mv "$tmp/$BIN" "$dest/"
  launcher="$dest/$BIN/bin/$BIN"
else
  err "压缩包布局异常：没有顶层 $BIN[.app]"
fi
[ -x "$launcher" ] || err "布局异常：找不到可执行文件 $launcher"
ln -sfn "$launcher" "$BINDIR/$BIN"

# --- background service（固定用符号链接，升级只换链接）---
if [ "${CC_POCKET_NO_SERVICE:-}" = "1" ]; then
  warn "CC_POCKET_NO_SERVICE=1 — 跳过服务注册"
elif [ "$plat" = "macos" ]; then
  say "注册 launchd（relay=$RELAY）"
  "$BINDIR/$BIN" service-install --apply --exec "$BINDIR/$BIN" --relay "$RELAY"
else
  if command -v systemctl >/dev/null 2>&1 && systemctl --user show-environment >/dev/null 2>&1; then
    say "注册 systemd --user（relay=$RELAY）"
    "$BINDIR/$BIN" service-install --apply --exec "$BINDIR/$BIN" --relay "$RELAY"
    say "重启到新版本"
    systemctl --user restart "$BIN"
  else
    warn "未检测到 systemd --user，跳过服务注册"
    warn "请手动启动: $BINDIR/$BIN run --relay $RELAY"
  fi
fi

# --- migrate legacy flat layout ---
legacy="$HOME/.local/opt/$BIN"
if [ -d "$legacy" ]; then
  say "删除旧版目录 $legacy"
  rm -rf "$legacy"
fi

# --- prune old versions (keep newest 2) ---
count="$(ls -1 "$ROOT/versions" 2>/dev/null | wc -l | tr -d ' ')"
if [ "${count:-0}" -gt 2 ]; then
  ls -1 "$ROOT/versions" | sort -t. -k1,1n -k2,2n -k3,3n | head -n "$((count - 2))" | while read -r old; do
    if [ "$old" != "$ver" ]; then
      rm -rf "$ROOT/versions/$old"
      say "清理旧版本 $old"
    fi
  done
fi

case ":$PATH:" in
  *":$BINDIR:"*) ;;
  *) warn "$BINDIR 不在 PATH 中，请添加:  echo 'export PATH=\"\$HOME/.local/bin:\$PATH\"' >> ~/.$(basename "${SHELL:-bash}")rc" ;;
esac

logs="journalctl --user -u $BIN -f"
[ "$plat" = "macos" ] && logs="tail -f ~/Library/Logs/cc-pocket/daemon.err.log"
cat <<EOF

  ✅ 已安装 $BIN $VERSION
     relay: $RELAY
     二进制来源: $ASSET_REPO

  下一步 — 手机配对：

      $BIN pair

  （终端会显示二维码 + 6 位配对码，用 CC Pocket App 扫描或输入）

  日志:    $logs
  升级:    $BIN update
EOF
