"""Replace only dist/linux or dist/macos; preserve user data and other platforms."""
import fcntl
import hashlib
import json
import os
import re
import shutil
import sys
from pathlib import Path

MARKER = ".kharchiche-native.json"
PERSONAL = re.compile(r"\.(?:db|sqlite|sqlite3)(?:[-.].*)?$|\.pdf$|\.bak$|\.backup$", re.I)


def inventory(directory):
    files = {}
    for path in directory.rglob("*"):
        relative = path.relative_to(directory).as_posix()
        if PERSONAL.search(path.name):
            raise RuntimeError(f"User data detected; refusing replacement: {path}")
        # Legitimate .app symlinks are recorded, never followed for deletion.
        if path.is_symlink():
            files[relative] = {"link": os.readlink(path)}
        elif path.is_file() and relative != MARKER:
            files[relative] = {"sha256": hashlib.sha256(path.read_bytes()).hexdigest()}
    return files


def main():
    root = Path(sys.argv[1]).resolve(strict=True)
    platform = sys.argv[2]
    if platform not in {"linux", "macos/arm64", "macos/x64"}:
        raise RuntimeError("Only Linux/macOS publication is allowed.")
    recover_only = sys.argv[3] == "--recover"
    candidate = None if recover_only else Path(sys.argv[3]).resolve(strict=True)
    if candidate is not None and not candidate.is_relative_to(root / "target"):
        raise RuntimeError("Candidate must be a private build directory under target.")
    dist = root / "dist"
    if dist.is_symlink():
        raise RuntimeError("dist must not be a symlink.")
    dist.mkdir(exist_ok=True)
    final = dist / platform
    if final.parent.is_symlink():
        raise RuntimeError("Refusing linked platform directory.")
    final.parent.mkdir(parents=True, exist_ok=True)
    backup = final.parent / f".{final.name}-previous"
    lock_name = platform.replace("/", "-")
    with (root / "target" / f"{lock_name}-publish.lock").open("a") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        if final.is_symlink() or backup.is_symlink():
            raise RuntimeError("Refusing linked distribution directories.")

        def managed(path):
            if not any(path.iterdir()):
                return
            saved = json.loads((path / MARKER).read_text())
            if saved.get("format") != 1 or saved.get("files") != inventory(path):
                raise RuntimeError(f"Unmanaged or modified distribution: {path}")

        if backup.exists():
            managed(backup)
            if not final.exists() or not any(final.iterdir()):
                if final.exists():
                    final.rmdir()
                backup.rename(final)
            else:
                managed(final)
                shutil.rmtree(backup)
        if final.exists():
            managed(final)
        if recover_only:
            return
        (candidate / MARKER).write_text(json.dumps(
            {"format": 1, "files": inventory(candidate)}, indent=2))
        had_old = final.exists()
        if had_old:
            final.rename(backup)
        try:
            candidate.rename(final)
        except BaseException:
            if had_old:
                backup.rename(final)
            raise
        if had_old:
            shutil.rmtree(backup)
        print(f"PUBLISHED: {final}")


if __name__ == "__main__":
    main()