#!/usr/bin/env bash
# Pinned mobile-only toolchain. Source the generated env.sh after running this script.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd -P)"
tools="$root/.cache/mobile-tools"
mkdir -p "$tools"
case "$(uname -s):$(uname -m)" in
  Linux:x86_64) platform=linux; jni_os=linux ;;
  Darwin:x86_64) platform=darwin; jni_os=darwin ;;
  *) echo 'Mobile builds require Linux x64 (Android) or macOS Intel (iOS).' >&2; exit 1 ;;
esac
graal="graalvm-svm-java17-$platform-gluon-22.1.0.1-Final"
if [[ ! -d "$tools/$graal" ]]; then
  curl --fail --location --retry 3 \
    "https://github.com/gluonhq/graal/releases/download/gluon-22.1.0.1-Final/$graal.tar.gz" \
    --output "$tools/$graal.tar.gz"
  tar -xzf "$tools/$graal.tar.gz" -C "$tools"
fi
graal_home="$tools/$graal"
if [[ "$platform" == darwin ]]; then graal_home="$graal_home/Contents/Home"; fi
test -x "$graal_home/bin/native-image"
if [[ ! -x "$tools/apache-maven-3.8.8/bin/mvn" ]]; then
  curl --fail --location --retry 3 \
    'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.8.8/apache-maven-3.8.8-bin.tar.gz' \
    --output "$tools/maven.tar.gz"
  tar -xzf "$tools/maven.tar.gz" -C "$tools"
fi
printf 'export GRAALVM_HOME=%q\nexport JAVA_HOME=%q\nexport PATH=%q:%q:"$PATH"\n' \
  "$graal_home" "$graal_home" "$graal_home/bin" "$tools/apache-maven-3.8.8/bin" > "$tools/env.sh"
echo "Toolchain environment: $tools/env.sh"
"$graal_home/bin/java" -version
