"""Copy only a real generated package with the expected contents into dist/<mobile>."""
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile

base = Path(__file__).resolve().parent
platform = sys.argv[1]
if platform == "ios-unsigned":
    bundles = list((base / "target/gluonfx").rglob("*.app"))
    if len(bundles) != 1 or not (bundles[0] / "Info.plist").is_file():
        raise SystemExit(f"Expected one real generated iOS app bundle: {bundles}")
    destination = base / "target/artifacts/Kharchiche-Konsolidated-ios-unsigned.app.zip"
    destination.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["ditto", "-c", "-k", "--sequesterRsrc", "--keepParent", str(bundles[0]), str(destination)], check=True)
    print(f"Unsigned iOS build (not installable): {destination}")
    raise SystemExit(0)
if platform not in {"android", "ios"}:
    raise SystemExit("Expected android or ios")
extension = "apk" if platform == "android" else "ipa"
candidates = []
for package in (base / "target/gluonfx").rglob("*." + extension):
    # Gluon copies the Gradle APK to gvm/; ignore the internal duplicate output.
    if "android_project" in package.parts:
        continue
    if package.stat().st_size == 0 or not zipfile.is_zipfile(package):
        raise SystemExit(f"Invalid generated package: {package}")
    with zipfile.ZipFile(package) as archive:
        names = archive.namelist()
        if platform == "android":
            valid = "AndroidManifest.xml" in names and any(n.startswith("lib/arm64-v8a/") and n.endswith(".so") for n in names)
        else:
            valid = any(n.startswith("Payload/") and n.endswith(".app/Info.plist") for n in names)
        if not valid:
            raise SystemExit(f"Unexpected {extension} contents: {package}")
    candidates.append(package)
if len(candidates) != 1:
    raise SystemExit(f"Expected exactly one generated {extension}, found: {candidates}")
if platform == "ios":
    bundles = list((base / "target/gluonfx").rglob("*.app"))
    if len(bundles) != 1 or not (bundles[0] / "embedded.mobileprovision").is_file():
        raise SystemExit("A signed iOS app with embedded provisioning profile is required")
    subprocess.run(["codesign", "--verify", "--deep", "--strict", str(bundles[0])], check=True)
destination = base.parent / "dist" / platform / f"Kharchiche-Konsolidated-1.0.0.{extension}"
destination.parent.mkdir(parents=True, exist_ok=True)
shutil.copyfile(candidates[0], destination)
print(f"Generated package: {destination}")
