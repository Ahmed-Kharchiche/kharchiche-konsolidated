
#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd -P)"

: "${JAVA_HOME:?Source .cache/mobile-tools/env.sh first}"

sdk="${MOBILE_ANDROID_SDK_DIR:-$root/.cache/mobile-tools/android-sdk}"
manager="$sdk/cmdline-tools/latest/bin/sdkmanager"

if [[ ! -x "$manager" ]]; then
  mkdir -p "$sdk/cmdline-tools"

  curl --fail --location --retry 3 \
    'https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip' \
    --output "$sdk/commandline-tools.zip"

  unzip -o -q "$sdk/commandline-tools.zip" -d "$sdk/cmdline-tools"

  mv "$sdk/cmdline-tools/cmdline-tools" \
    "$sdk/cmdline-tools/latest"
fi

set +o pipefail
yes | "$manager" --sdk_root="$sdk" --licenses > "$sdk/licenses.log"
status=${PIPESTATUS[1]}
set -o pipefail

if [[ "$status" != 0 ]]; then
  cat "$sdk/licenses.log"
  exit "$status"
fi

"$manager" --sdk_root="$sdk" \
  'platform-tools' \
  'platforms;android-34' \
  'build-tools;34.0.0' \
  'ndk;26.3.11579264' \
  'extras;android;m2repository' \
  'extras;google;m2repository'

printf \
  'export ANDROID_SDK=%q\nexport ANDROID_HOME=%q\nexport ANDROID_SDK_ROOT=%q\nexport ANDROID_NDK=%q\n' \
  "$sdk" \
  "$sdk" \
  "$sdk" \
  "$sdk/ndk/26.3.11579264" \
  > "$root/.cache/mobile-tools/android-env.sh"

echo 'Android SDK 34 / build-tools 34.0.0 / NDK 26.3.11579264 installed.'
