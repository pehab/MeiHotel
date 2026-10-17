#!/usr/bin/env bash
set -euo pipefail
sdk_root="${RUNNER_TEMP}/meihotel-android-sdk"
mkdir -p "$sdk_root/cmdline-tools"
curl --fail --silent --show-error --location --retry 3 \
  https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip \
  --output "${RUNNER_TEMP}/android-tools.zip"
unzip -q "${RUNNER_TEMP}/android-tools.zip" -d "$sdk_root/cmdline-tools"
mv "$sdk_root/cmdline-tools/cmdline-tools" "$sdk_root/cmdline-tools/latest"
export ANDROID_HOME="$sdk_root"
export ANDROID_SDK_ROOT="$sdk_root"
export PATH="$sdk_root/cmdline-tools/latest/bin:$sdk_root/platform-tools:$sdk_root/emulator:$PATH"
(yes || true) | sdkmanager --licenses >/dev/null
sdkmanager "platform-tools" "platforms;android-36" "build-tools;35.0.0"
printf 'ANDROID_HOME=%s\nANDROID_SDK_ROOT=%s\n' "$sdk_root" "$sdk_root" >> "$GITHUB_ENV"
printf '%s\n' "$sdk_root/cmdline-tools/latest/bin" "$sdk_root/platform-tools" "$sdk_root/emulator" >> "$GITHUB_PATH"
