#!/usr/bin/env bash
# Host-only test of the SAME static JNI feature/substitution used in the mobile app.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd -P)"
cd "$root"
: "${GRAALVM_HOME:?Source .cache/mobile-tools/env.sh first}"
python3 mobile/prepare.py
export MOBILE_NATIVE_DIR="$root/mobile/target/native-host"
bash scripts/build-mobile-sqlite.sh host
mvn -B -ntp -f mobile/pom.xml -DskipTests test-compile
# Deliberately exclude FXML metadata and JavaFX: this executable tests SQLite JNI alone.
mkdir -p "$MOBILE_NATIVE_DIR/classes/mobile/nativeimage"
cp mobile/target/classes/mobile/nativeimage/*.class "$MOBILE_NATIVE_DIR/classes/mobile/nativeimage/"
cp mobile/target/test-classes/NativeSQLiteSmoke.class "$MOBILE_NATIVE_DIR/classes/"
classpath="$MOBILE_NATIVE_DIR/classes:$root/mobile/target/dependencies/sqlite-jdbc-mobile.jar:$MOBILE_NATIVE_DIR/slf4j-api.jar"
"$GRAALVM_HOME/bin/native-image" -J-Xmx3g --no-fallback -cp "$classpath" \
  --features=mobile.nativeimage.SQLiteFeature --initialize-at-run-time=org.sqlite \
  -H:NativeLinkerOption=-lm \
  -H:CLibraryPath="$MOBILE_NATIVE_DIR" -H:Path="$MOBILE_NATIVE_DIR" -H:Name=sqlite-smoke NativeSQLiteSmoke
"$MOBILE_NATIVE_DIR/sqlite-smoke"
