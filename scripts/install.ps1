# CC Pocket daemon — Windows 一键安装（x86_64）：
#
#   irm https://raw.githubusercontent.com/ac54u-mobile/cc-pocket/main/scripts/install.ps1 | iex
#
# 下载最新自包含 Release（自带 JRE），校验 SHA256SUMS，安装到
#   %LOCALAPPDATA%\cc-pocket\versions\<ver>\cc-pocket-daemon\
# 注册开机服务并默认连接 wss://relay.txx.app，然后进入配对。
#
# 环境变量（可选）：
#   CC_POCKET_REPO / CC_POCKET_ASSET_REPO / CC_POCKET_RELAY
$ErrorActionPreference = "Stop"

$repo = if ($env:CC_POCKET_REPO) { $env:CC_POCKET_REPO } else { "ac54u-mobile/cc-pocket" }
$fallbackAssetRepo = "heypandax/cc-pocket"
$assetRepo = if ($env:CC_POCKET_ASSET_REPO) { $env:CC_POCKET_ASSET_REPO } else { $null }
$relay = if ($env:CC_POCKET_RELAY) { $env:CC_POCKET_RELAY } else { "wss://relay.txx.app" }
$root = Join-Path $env:LOCALAPPDATA "cc-pocket"

Write-Host "-- cc-pocket daemon 安装器 --"
Write-Host "relay: $relay"

# 优先 daemon-v*；跳过 app-v*。无 daemon 包时再回退 /releases/latest。
function Get-LatestDaemonRelease($ownerRepo) {
    $rels = Invoke-RestMethod "https://api.github.com/repos/$ownerRepo/releases?per_page=30"
    $withDaemon = @($rels | Where-Object {
        -not $_.draft -and
        ($_.tag_name -notlike 'app-v*') -and ($_.tag_name -notlike 'app/*') -and
        ($_.assets | Where-Object { $_.name -like 'cc-pocket-daemon-*' })
    })
    $daemonTagged = @($withDaemon | Where-Object {
        $_.tag_name -like 'daemon-v*' -or $_.tag_name -like 'daemon/*'
    })
    if ($daemonTagged.Count -gt 0) { return $daemonTagged[0] }
    if ($withDaemon.Count -gt 0) { return $withDaemon[0] }
    return Invoke-RestMethod "https://api.github.com/repos/$ownerRepo/releases/latest"
}

if (-not $assetRepo) {
    try {
        $rel = Get-LatestDaemonRelease $repo
        $assetRepo = $repo
    } catch {
        Write-Host "warning: $repo 尚无 daemon Release，回退使用 $fallbackAssetRepo 的二进制（relay 仍为 $relay）"
        $assetRepo = $fallbackAssetRepo
        $rel = Get-LatestDaemonRelease $assetRepo
    }
} else {
    $rel = Get-LatestDaemonRelease $assetRepo
}

$ver = $rel.tag_name -replace '^daemon-v', '' -replace '^daemon/', '' -replace '^v', ''
$asset = $rel.assets | Where-Object { $_.name -like "*windows-x86_64.zip" } | Select-Object -First 1
if (-not $asset) { throw "最新 daemon Release ($($rel.tag_name)) 没有 Windows 包 — 见 https://github.com/$assetRepo/releases" }

$zip = Join-Path $env:TEMP $asset.name
Write-Host "下载 $($asset.name) ($($rel.tag_name)，来源 $assetRepo)..."
Invoke-WebRequest $asset.browser_download_url -OutFile $zip

$sums = $rel.assets | Where-Object { $_.name -eq "SHA256SUMS" } | Select-Object -First 1
if ($sums) {
    $line = (Invoke-RestMethod $sums.browser_download_url) -split "`n" | Where-Object { $_ -match [regex]::Escape($asset.name) } | Select-Object -First 1
    if ($line -and $line -match '^([0-9a-fA-F]{64})') {
        $expected = $Matches[1].ToLower()
        $actual = (Get-FileHash $zip -Algorithm SHA256).Hash.ToLower()
        if ($actual -ne $expected) { throw "校验失败 $($asset.name)（期望 $expected，实际 $actual）" }
        Write-Host "校验通过"
    } else { Write-Host "warning: SHA256SUMS 中没有 $($asset.name) — 跳过校验" }
} else { Write-Host "warning: 此 Release 无 SHA256SUMS — 跳过校验" }

schtasks /End /TN cc-pocket-daemon 2>$null | Out-Null
Get-Process cc-pocket-daemon -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

$dest = Join-Path $root "versions\$ver"
if (Test-Path $dest) { Remove-Item $dest -Recurse -Force }
New-Item -ItemType Directory -Force -Path $dest | Out-Null
Expand-Archive $zip $dest -Force
Remove-Item $zip

$exe = Join-Path $dest "cc-pocket-daemon\cc-pocket-daemon.exe"
if (-not (Test-Path $exe)) {
    $found = Get-ChildItem $dest -Recurse -Filter "cc-pocket-daemon.exe" | Select-Object -First 1
    if (-not $found) { throw "压缩包中未找到 cc-pocket-daemon.exe" }
    $exe = $found.FullName
}

Write-Host "注册并启动后台服务（relay=$relay）..."
& $exe service-install --apply --exec $exe --relay $relay

$binDir = Join-Path $root "bin"
New-Item -ItemType Directory -Force -Path $binDir | Out-Null
$shim = Join-Path $binDir "cc-pocket-daemon.cmd"
Set-Content -Path $shim -Value @('@echo off', "`"$exe`" %*") -Encoding Oem

$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
$already = $userPath -and (($userPath -split ';') | Where-Object { $_.TrimEnd('\') -ieq $binDir.TrimEnd('\') })
if (-not $already) {
    $newPath = if ([string]::IsNullOrEmpty($userPath)) { $binDir } else { "$($userPath.TrimEnd(';'));$binDir" }
    [Environment]::SetEnvironmentVariable('Path', $newPath, 'User')
    Write-Host "已将 $binDir 加入 PATH（请打开新终端后使用：cc-pocket-daemon update）"
}
if (-not (($env:Path -split ';') | Where-Object { $_.TrimEnd('\') -ieq $binDir.TrimEnd('\') })) {
    $env:Path = "$($env:Path.TrimEnd(';'));$binDir"
}

$legacy = Join-Path $root "daemon"
if (Test-Path $legacy) { Remove-Item $legacy -Recurse -Force -ErrorAction SilentlyContinue }
Get-ChildItem (Join-Path $root "versions") -Directory |
    Sort-Object { [version]($_.Name -replace '[^\d.].*$', '') } -Descending |
    Select-Object -Skip 2 |
    ForEach-Object { Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue }

Write-Host ""
Write-Host "已安装: $exe"
Write-Host "relay:  $relay"
Write-Host "命令:   cc-pocket-daemon   （请开新终端）"
Write-Host "升级:   cc-pocket-daemon update"
Write-Host ""
Write-Host "正在打开配对 — 用 CC Pocket App 扫描二维码："
& $exe pair
