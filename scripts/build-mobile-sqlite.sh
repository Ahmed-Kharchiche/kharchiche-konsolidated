#!/usr/bin/env bash
# Compile the SAME SQLite/JDBC release as desktop for the target CPU, statically.
set -euo pipefail
platform="${1:?Expected android, ios or host (JNI test)}"
root="$(cd "$(dirname "$0")/.." && pwd -P)"
work="${MOBILE_NATIVE_DIR:-$root/mobile/target/native}"
mkdir -p "$work"
: "${JAVA_HOME:?Source .cache/mobile-tools/env.sh first}"
fetch() { if [[ ! -f "$2" ]]; then curl --fail --location --retry 3 "$1" --output "$2.download"; mv "$2.download" "$2"; fi; }
fetch 'https://www.sqlite.org/2024/sqlite-amalgamation-3460000.zip' "$work/sqlite.zip"
unzip -o -q "$work/sqlite.zip" -d "$work"
for file in NativeDB.c NativeDB.java; do
  fetch "https://raw.githubusercontent.com/xerial/sqlite-jdbc/3.46.0.0/src/main/java/org/sqlite/core/$file" "$work/$file"
done
fetch 'https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar' "$work/slf4j-api.jar"
python3 - "$work" <<'PY'
import hashlib, sys
from pathlib import Path
for name, expected in {
    'sqlite.zip': '712a7d09d2a22652fb06a49af516e051979a3984adb067da86760e60ed51a7f5',
    'NativeDB.c': '1a695760a4bdf8d1a9b50a899b0fad18a87168c1b24de2fb6404b680d44ce31f',
    'NativeDB.java': '72e65a3d778a6bad5be0aa7429f8d7bf19d1db51af71dbc23115a5f06eb003ca',
}.items():
    if hashlib.sha256((Path(sys.argv[1]) / name).read_bytes()).hexdigest() != expected:
        raise SystemExit('SQLite source checksum mismatch: ' + name)
PY
"$JAVA_HOME/bin/javac" -cp "$root/mobile/target/dependencies/sqlite-jdbc-mobile.jar:$work/slf4j-api.jar" \
  -h "$work" -d "$work/header-classes" "$work/NativeDB.java"
cp "$work/org_sqlite_core_NativeDB.h" "$work/NativeDB.h"
case "$platform:$(uname -s)" in
  host:Linux)
    cc=(cc)
    ar=ar
    jni=linux ;;
  android:Linux)
    : "${ANDROID_NDK:?Set ANDROID_NDK to NDK 26.3.11579264}"
    cc=("$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android21-clang")
    ar="$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ar"
    jni=linux ;;
  ios:Darwin)
    cc=("$(xcrun --sdk iphoneos --find clang)" -arch arm64 -isysroot "$(xcrun --sdk iphoneos --show-sdk-path)" -miphoneos-version-min=12.2)
    ar="$(xcrun --find ar)"
    jni=darwin ;;
  *) echo "Build $platform on its supported host." >&2; exit 1 ;;
esac
flags=(-O2 -fPIC -DSQLITE_THREADSAFE=1 -DSQLITE_ENABLE_COLUMN_METADATA -DSQLITE_ENABLE_FTS5
       -DSQLITE_ENABLE_RTREE -DSQLITE_ENABLE_DBSTAT_VTAB
       -I"$work/sqlite-amalgamation-3460000"
       -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/$jni")
"${cc[@]}" "${flags[@]}" -c "$work/sqlite-amalgamation-3460000/sqlite3.c" -o "$work/sqlite3.o"
"${cc[@]}" "${flags[@]}" -DJNI_OnLoad=Kharchiche_SQLite_OnLoad -DJNI_OnUnload=Kharchiche_SQLite_OnUnload \
  -c "$work/NativeDB.c" -o "$work/NativeDB.o"
"${cc[@]}" "${flags[@]}" -c "$root/mobile/native/sqlite-bootstrap.c" -o "$work/sqlite-bootstrap.o"
"$ar" rcs "$work/libsqlitejdbc.a" "$work/NativeDB.o" "$work/sqlite3.o" "$work/sqlite-bootstrap.o"
test -s "$work/libsqlitejdbc.a"
echo "SQLite JNI static library: $work/libsqlitejdbc.a"
