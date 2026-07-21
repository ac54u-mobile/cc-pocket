# Template cask for the cc-pocket daemon. Prefer the install script:
#   curl -fsSL https://raw.githubusercontent.com/ac54u-mobile/cc-pocket/main/scripts/install.sh | bash
#
# Assets live on ac54u-mobile/cc-pocket Releases under tag daemon-v<version>.
cask "cc-pocket" do
  version "1.0.0"

  arch arm: "arm64", intel: "x86_64"

  url "https://github.com/ac54u-mobile/cc-pocket/releases/download/daemon-v#{version}/cc-pocket-daemon-#{version}-macos-#{arch}.tar.gz"
  name "CC Pocket daemon"
  desc "Drive a coding agent on your Mac from your phone over a zero-knowledge E2E relay"
  homepage "https://github.com/ac54u-mobile/cc-pocket"

  binary "cc-pocket-daemon.app/Contents/MacOS/cc-pocket-daemon"

  postflight do
    system_command "#{HOMEBREW_PREFIX}/bin/cc-pocket-daemon",
                   args: ["service-install", "--apply", "--relay", "wss://relay.txx.app"]
  end

  uninstall launchctl: "dev.ccpocket.daemon"

  zap trash: [
    "~/.cc-pocket",
    "~/Library/Logs/cc-pocket",
  ]
end
