#!/usr/bin/env bash
# Native OS/CPU only. Does not touch Windows builds or existing application data.
set -euo pipefail
platform="${1:?Expected linux or macos}"
root="$(cd "$(dirname "$0")/.." && pwd -P)"
cd "$root"
case "$platform:$(uname -s)" in
  linux:Linux|macos:Darwin) ;;
  *) echo "Build $platform on its native operating system." >&2; exit 1 ;;
esac
: "${JAVA_HOME:?Set JAVA_HOME to a full native JDK 21}"
export PATH="$JAVA_HOME/bin:$PATH"
for tool in java javac jlink jpackage; do test -x "$JAVA_HOME/bin/$tool"; done
command -v python3 >/dev/null || { echo "Python 3.9+ required." >&2; exit 1; }
python3 -c 'import sys; assert sys.version_info >= (3, 9), "Python 3.9+ required"'
settings="$("$JAVA_HOME/bin/java" -XshowSettings:properties -version 2>&1)"
grep -Eq 'java.specification.version = 21[[:space:]]*$' <<< "$settings" || {
  echo "JDK 21 required." >&2; exit 1;
}
case "$(uname -m)" in
  x86_64) arch=x64; cpu=x86_64; java_arch="(amd64|x86_64)" ;;
  arm64|aarch64) arch=arm64; cpu=aarch64; java_arch="(aarch64|arm64)" ;;
  *) echo "Unsupported CPU." >&2; exit 1 ;;
esac
grep -Eq "os.arch = $java_arch[[:space:]]*$" <<< "$settings" || {
  echo "JAVA_HOME must match the native CPU ($arch)." >&2; exit 1;
}
if [[ "$platform" == linux ]]; then
  for tool in dpkg-deb fakeroot; do command -v "$tool" >/dev/null; done
  [[ -n "${DISPLAY:-}" ]] || { echo "Use a graphical session or xvfb-run -a." >&2; exit 1; }
fi
publish_target="$platform"
if [[ "$platform" == macos ]]; then publish_target="macos/$arch"; fi
mkdir -p "$root/target" "$root/dist"
python3 "$root/scripts/publish-native.py" "$root" "$publish_target" --recover
work="$(mktemp -d "$root/target/native-$platform.XXXXXX")"
smoke_data=""
launcher_pid=""
cleanup() {
  status=$?
  trap - EXIT
  if [[ -n "$launcher_pid" ]]; then
    kill "$launcher_pid" 2>/dev/null || true
    wait "$launcher_pid" 2>/dev/null || true
  fi
  # Delete only this invocation's private work tree, never target/ or dist/ wholesale.
  case "$work" in "$root/target/native-$platform."*) rm -rf -- "$work" ;; esac
  exit "$status"
}
trap cleanup EXIT
logs="$root/target/native-logs/$platform-$arch"
mkdir -p "$logs" "$work/input" "$work/output" "$work/os-temp"
export TMPDIR="$work/os-temp"
read -r version fx_version < <(python3 - <<'PY'
import xml.etree.ElementTree as E
r=E.parse("pom.xml").getroot(); ns={"p":"http://maven.apache.org/POM/4.0.0"}
print(r.findtext("p:version",namespaces=ns).split("-")[0],
      r.findtext("p:properties/p:javafx.version",namespaces=ns))
PY
)
bash "$root/mvnw" -B "-Dapp.packageDirectory=$work/input" verify 2>&1 | tee "$logs/maven.log"
cp "$root/target/kharchiche-konsolidated.jar" "$work/input/"
classifier=linux; sqlite_os=Linux; native_ext=so
if [[ "$platform" == macos ]]; then classifier=mac; sqlite_os=Mac; native_ext=dylib; fi
if [[ "$arch" == arm64 ]]; then classifier="$classifier-aarch64"; fi
test -f "$work/input/lib/javafx-graphics-$fx_version-$classifier.jar" || {
  echo "Wrong or missing JavaFX native classifier: $classifier" >&2; exit 1;
}
python3 "$root/scripts/check-native-input.py" "$work/input" "$root" "$sqlite_os" "$cpu" "$native_ext"
"$JAVA_HOME/bin/jlink" --module-path "$JAVA_HOME/jmods" \
  --add-modules java.se,jdk.unsupported,jdk.crypto.ec --strip-debug \
  --no-header-files --no-man-pages --output "$work/runtime" 2>&1 | tee "$logs/jlink.log"
"$work/runtime/bin/java" -version 2>&1 | tee "$logs/runtime.log"
name="Kharchiche Konsolidated"
image_options=(--verbose --type app-image)
if [[ "$platform" == macos ]]; then
  image_options+=(--mac-package-identifier com.kharchiche.konsolidated)
fi
"$JAVA_HOME/bin/jpackage" --name "$name" \
  --app-version "$version" --vendor "Ahmed Kharchiche" \
  --input "$work/input" --main-jar kharchiche-konsolidated.jar --main-class Launcher \
  --java-options "-Dkharchiche.packaged=true" --runtime-image "$work/runtime" \
  --temp "$work/jpackage-image" --dest "$work/image" "${image_options[@]}" \
  2>&1 | tee "$logs/jpackage-image.log"
if [[ "$platform" == linux ]]; then
  image="$work/image/$name"
  launcher="$image/bin/$name"
  app_java="$image/lib/runtime/bin/java"
  app_lib="$image/lib/app"
else
  image="$work/image/$name.app"
  launcher="$image/Contents/MacOS/$name"
  app_java="$image/Contents/runtime/Contents/Home/bin/java"
  app_lib="$image/Contents/app"
fi
test -x "$launcher"
# Short check using the bundled Java, followed by the actual native launcher.
"$app_java" -cp "$root/target/test-classes:$app_lib/*:$app_lib/lib/*" \
  DistributionSmoke 2>&1 | tee "$logs/package-smoke.log"
smoke_data="$(mktemp -d "$work/os-temp/kharchiche-smoke.XXXXXX")"
_JAVA_OPTIONS="-Dkharchiche.dataDir=\"$smoke_data/data\" -Duser.home=\"$smoke_data/home\"" \
  "$launcher" > "$logs/launcher.log" 2>&1 &
launcher_pid=$!
for ((attempt=0; attempt<40; attempt++)); do
  [[ -f "$smoke_data/data/hotel.db" ]] && break
  kill -0 "$launcher_pid" 2>/dev/null || { cat "$logs/launcher.log" >&2; exit 1; }
  sleep 1
done
test -f "$smoke_data/data/hotel.db" || { echo "Launcher did not initialise SQLite." >&2; exit 1; }
kill "$launcher_pid"
wait "$launcher_pid" 2>/dev/null || true
launcher_pid=""
if [[ "$platform" == linux ]]; then
  "$JAVA_HOME/bin/jpackage" --verbose --type deb --name "$name" --app-version "$version" \
    --vendor "Ahmed Kharchiche" --app-image "$image" --dest "$work/output" \
    --linux-package-name kharchiche-konsolidated --linux-menu-group Utility --linux-shortcut \
    --temp "$work/jpackage-deb" 2>&1 | tee "$logs/jpackage-deb.log"
  packages=("$work/output/"*.deb)
  [[ ${#packages[@]} == 1 && -f "${packages[0]}" ]]
  package="$work/output/kharchiche-konsolidated-$version-$arch.deb"
  mv "${packages[0]}" "$package"
  dpkg-deb --info "$package" > "$logs/deb-info.log"
  dpkg-deb --contents "$package" > "$logs/deb-contents.log"
  grep -q '\.desktop' "$logs/deb-contents.log"
else
  if [[ "${CREATE_DMG:-1}" == 1 ]]; then
    "$JAVA_HOME/bin/jpackage" --verbose --type dmg --name "$name" --app-version "$version" \
      --vendor "Ahmed Kharchiche" --app-image "$image" --dest "$work/output" \
      --temp "$work/jpackage-dmg" 2>&1 | tee "$logs/jpackage-dmg.log"
    images=("$work/output/"*.dmg)
    [[ ${#images[@]} == 1 && -f "${images[0]}" ]]
    mv "${images[0]}" "$work/output/Kharchiche-Konsolidated-$version-$arch.dmg"
  fi
  mv "$image" "$work/output/$name.app"
fi
python3 "$root/scripts/publish-native.py" "$root" "$publish_target" "$work/output"
echo "Distribution ready: $root/dist/$publish_target ($arch). Logs: $logs"