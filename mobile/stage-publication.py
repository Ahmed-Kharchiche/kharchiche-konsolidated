"""Validate a verified build artifact before replacing exactly one mobile package."""
import hashlib
from pathlib import Path
import re
import shutil
import sys
import zipfile


def stage(platform, incoming, repository):
    if platform not in {"android", "ios"}:
        raise ValueError("Only Android APK or signed iOS IPA can be published")
    extension = "apk" if platform == "android" else "ipa"
    name = f"Kharchiche-Konsolidated-1.0.0.{extension}"
    package = Path(incoming) / name
    checksum = Path(incoming) / (name + ".sha256")
    if package.is_symlink() or checksum.is_symlink():
        raise ValueError("Linked artifacts are not accepted")
    expected = checksum.read_text(encoding="utf-8").split()[0]
    if not re.fullmatch(r"[0-9a-fA-F]{64}", expected):
        raise ValueError("Invalid verification checksum")
    with package.open("rb") as stream:
        actual = hashlib.file_digest(stream, "sha256").hexdigest()
    if actual != expected.lower():
        raise ValueError("Artifact differs from the package verified by the build")
    with zipfile.ZipFile(package) as archive:
        names = set(archive.namelist())
        if platform == "android":
            valid = "AndroidManifest.xml" in names and "lib/arm64-v8a/libsubstrate.so" in names
            if valid:
                with archive.open("lib/arm64-v8a/libsubstrate.so") as stream:
                    header = stream.read(20)
                valid = header[:4] == b"\x7fELF" and header[4:6] == b"\x02\x01" and header[18:20] == b"\xb7\x00"
        else:
            valid = any(
                path.startswith("Payload/") and path.endswith(".app/Info.plist")
                and path[:-len("Info.plist")] + "embedded.mobileprovision" in names
                and path[:-len("Info.plist")] + "_CodeSignature/CodeResources" in names
                for path in names
            )
        if not valid:
            raise ValueError(f"Unexpected {extension} contents or missing native/signing data")
    root = Path(repository).resolve(strict=True)
    destination = root / "dist" / platform / name
    if destination.resolve().is_relative_to(root) is False:
        raise ValueError("Publication path escapes the repository")
    destination.parent.mkdir(parents=True, exist_ok=True)
    # Validation completes before touching the previous functional package.
    temporary = destination.with_name(name + ".publishing")
    try:
        shutil.copyfile(package, temporary)
        temporary.replace(destination)
    finally:
        temporary.unlink(missing_ok=True)
    print(f"Verified replacement: {destination}")
    return destination


if __name__ == "__main__":
    stage(*sys.argv[1:])
