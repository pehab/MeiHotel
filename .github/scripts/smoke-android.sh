#!/usr/bin/env bash
set -euo pipefail
sdkmanager "emulator" "system-images;android-30;google_apis;x86_64"
printf 'no\n' | avdmanager create avd --name hotel --package "system-images;android-30;google_apis;x86_64" --force
sudo chmod 666 /dev/kvm
emulator -avd hotel -no-window -no-audio -no-boot-anim -no-snapshot -gpu swiftshader_indirect -camera-back none > "$RUNNER_TEMP/emulator.log" 2>&1 &
emulator_pid=$!
trap 'kill "$emulator_pid" 2>/dev/null || true' EXIT
adb wait-for-device
timeout 180 bash -c 'until [ "$(adb shell getprop sys.boot_completed | tr -d "\r")" = "1" ]; do sleep 2; done'
adb shell wm size 1280x900
adb shell input keyevent 82
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w de.meiapps.meihotel.test/de.meiapps.meihotel.SmokeInstrumentation | tee "$RUNNER_TEMP/smoke-result.txt"
# A custom instrumentation runner reports RESULT_OK as -1; am instrument itself always exits zero.
rg -q 'INSTRUMENTATION_CODE: -1' "$RUNNER_TEMP/smoke-result.txt"
! rg -q 'FAIL:|INSTRUMENTATION_FAILED' "$RUNNER_TEMP/smoke-result.txt"
adb shell am start -W -n de.meiapps.meihotel/.MainActivity
sleep 1
mkdir -p app/build/screenshots
adb exec-out screencap -p > app/build/screenshots/hotel.png
