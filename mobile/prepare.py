"""Prepare isolated shared sources and Xerial metadata; never edits desktop files/data."""
import json
import hashlib
from pathlib import Path
import shutil
import urllib.request
import zipfile

BASE = Path(__file__).resolve().parent
ROOT = BASE.parent
TARGET = BASE / "target"


def download(url, destination):
    destination.parent.mkdir(parents=True, exist_ok=True)
    if not destination.exists():
        temporary = destination.with_suffix(destination.suffix + ".download")
        with urllib.request.urlopen(url, timeout=120) as response, temporary.open("wb") as out:
            shutil.copyfileobj(response, out)
        temporary.replace(destination)


def main():
    generated = TARGET / "generated-sources/shared"
    generated.mkdir(parents=True, exist_ok=True)
    # Remove stale generated Java only, within this module's private target tree.
    for path in generated.rglob("*.java"):
        path.unlink()
    for source in (ROOT / "src/main/java").rglob("*.java"):
        relative = source.relative_to(ROOT / "src/main/java")
        if relative.as_posix() in {"OpenFileUtil.java", "Launcher.java"}:
            continue
        text = source.read_text(encoding="utf-8")
        # Unused desktop import in TicketPDF; its original PDF generation is retained.
        text = text.replace("import java.awt.Desktop;", "")
        destination = generated / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_text(text, encoding="utf-8")
    for source in (BASE / "src/main/java").rglob("*.java"):
        destination = generated / source.relative_to(BASE / "src/main/java")
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, destination)

    tests = TARGET / "generated-test-sources"
    tests.mkdir(parents=True, exist_ok=True)
    for path in tests.rglob("*.java"):
        path.unlink()
    for test_root in (ROOT / "src/test/java", BASE / "src/test/java"):
        for source in test_root.rglob("*.java"):
            destination = tests / source.relative_to(test_root)
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, destination)

    # Every FXML-instantiated type and controller must survive Native Image analysis.
    reflection = {"HotelController"}
    for fxml in (ROOT / "src/main/resources/view").glob("*.fxml"):
        for line in fxml.read_text(encoding="utf-8").splitlines():
            if "<?import " in line:
                reflection.add(line.split("<?import ", 1)[1].split("?>", 1)[0].strip())
    config = TARGET / "generated-resources/META-INF/native-image/kharchiche/mobile"
    config.mkdir(parents=True, exist_ok=True)
    (config / "reflect-config.json").write_text(json.dumps([
        {"name": name, "allDeclaredConstructors": True, "allDeclaredFields": True,
         "allDeclaredMethods": True, "allPublicMethods": True}
        for name in sorted(reflection)
    ], indent=2), encoding="utf-8")

    dependencies = TARGET / "dependencies"
    original = dependencies / "sqlite-jdbc-3.46.0.0.jar"
    download("https://repo.maven.apache.org/maven2/org/xerial/sqlite-jdbc/3.46.0.0/sqlite-jdbc-3.46.0.0.jar", original)
    if hashlib.sha256(original.read_bytes()).hexdigest() != "e697df15be3f95219d80773c5f1002030e33e932adda186c1c86fd51df6691a9":
        raise RuntimeError("SQLite JDBC 3.46.0.0 checksum mismatch")
    with zipfile.ZipFile(original) as src, zipfile.ZipFile(dependencies / "sqlite-jdbc-mobile.jar", "w", zipfile.ZIP_DEFLATED) as dst:
        for item in src.infolist():
            # Xerial's feature selects host libraries and requires a newer GraalVM API.
            # Our mobile feature replaces it; JDBC classes and service registration stay unchanged.
            if item.filename.startswith("META-INF/native-image/") or "/org/sqlite/nativeimage/" in item.filename:
                continue
            dst.writestr(item, src.read(item))
    print("Prepared shared Java, FXML reflection metadata and SQLite JDBC 3.46.0.0")


if __name__ == "__main__":
    main()
