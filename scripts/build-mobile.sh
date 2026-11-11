#!/usr/bin/env bash
set -euo pipefail
platform="${1:?Expected android or ios}"
root="$(cd "$(dirname "$0")/.." && pwd -P)"
case "$platform:$(uname -s)" in android:Linux|ios:Darwin) ;; *) echo 'Unsupported mobile build host/target.' >&2; exit 1 ;; esac
cd "$root"
: "${GRAALVM_HOME:?Source .cache/mobile-tools/env.sh first}"
mkdir -p mobile/target/logs
exec > >(tee "mobile/target/logs/$platform-build.log") 2>&1
python3 mobile/prepare.py
bash scripts/build-mobile-sqlite.sh "$platform"
profiles="$platform"
if [[ "$platform" == ios && "${MOBILE_IOS_SIGNED:-false}" == true ]]; then
  : "${MOBILE_IOS_SIGNING_IDENTITY:?Missing signing identity}"
  : "${MOBILE_IOS_PROFILE_NAME:?Missing provisioning profile name}"
  profiles='ios,ios-signed'
fi
# JVM verification is run separately (with a display); this step cross-compiles native code.
mvn -B -ntp -f mobile/pom.xml -P"$profiles" -DskipTests package gluonfx:build gluonfx:package
if [[ "$platform" == android || "${MOBILE_IOS_SIGNED:-false}" == true ]]; then
  python3 mobile/collect.py "$platform"
else
  python3 mobile/collect.py ios-unsigned
  echo 'iOS native app completed without signing. Only the unsigned .app archive is published.'
fi
