"""Exercise replacement safety using a genuine compiled APK, never a fabricated package."""
import hashlib
import importlib.util
import os
from pathlib import Path
import shutil
import tempfile
import unittest

BASE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("stage_publication", BASE / "stage-publication.py")
publication = importlib.util.module_from_spec(spec)
spec.loader.exec_module(publication)
APK = Path(os.environ.get("APK_ARTIFACT_PATH", str(BASE.parent / "dist/android/Kharchiche-Konsolidated-1.0.0.apk")))


class PublicationTest(unittest.TestCase):
    def test_unsigned_ios_is_never_a_repository_distribution(self):
        with tempfile.TemporaryDirectory() as directory:
            with self.assertRaises(ValueError):
                publication.stage("ios-unsigned", directory, directory)
            self.assertFalse((Path(directory) / "dist").exists())

    @unittest.skipUnless(APK.is_file(), "No verified Android artifact in this run")
    def test_real_apk_and_failed_replacements_preserve_distributions(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            incoming = root / "incoming"
            incoming.mkdir()
            package = incoming / APK.name
            shutil.copyfile(APK, package)
            with package.open("rb") as stream:
                digest = hashlib.file_digest(stream, "sha256").hexdigest()
            checksum = package.with_name(package.name + ".sha256")
            checksum.write_text(digest + "  " + package.name, encoding="utf-8")
            for platform in ("windows", "linux", "macos", "ios"):
                sentinel = root / "dist" / platform / "keep.txt"
                sentinel.parent.mkdir(parents=True)
                sentinel.write_text("existing distribution", encoding="utf-8")
            published = publication.stage("android", incoming, root)
            self.assertEqual(published, root / "dist/android" / APK.name)
            with published.open("rb") as stream:
                self.assertEqual(hashlib.file_digest(stream, "sha256").hexdigest(), digest)
            before = published.stat().st_mtime_ns
            checksum.write_text("0" * 64, encoding="utf-8")
            with self.assertRaises(ValueError):
                publication.stage("android", incoming, root)
            checksum.unlink()
            with self.assertRaises(FileNotFoundError):
                publication.stage("android", incoming, root)
            self.assertEqual(published.stat().st_mtime_ns, before)
            for platform in ("windows", "linux", "macos", "ios"):
                self.assertEqual((root / "dist" / platform / "keep.txt").read_text(), "existing distribution")


if __name__ == "__main__":
    unittest.main()
