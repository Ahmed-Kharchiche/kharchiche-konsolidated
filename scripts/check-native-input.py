"""Check packaged resources and the JDBC native matching the build host."""
import sys
import zipfile
from pathlib import Path

input_dir, root = map(Path, sys.argv[1:3])
os_name, cpu, extension = sys.argv[3:]
with zipfile.ZipFile(input_dir / "kharchiche-konsolidated.jar") as jar:
    names = set(jar.namelist())
    for resource in (root / "src/main/resources").rglob("*"):
        if resource.is_file():
            name = resource.relative_to(root / "src/main/resources").as_posix()
            if name not in names:
                raise SystemExit(f"Missing packaged resource: {name}")
with zipfile.ZipFile(input_dir / "lib/sqlite-jdbc-3.46.0.0.jar") as jar:
    native = f"org/sqlite/native/{os_name}/{cpu}/libsqlitejdbc.{extension}"
    if native not in jar.namelist():
        raise SystemExit(f"Missing SQLite native: {native}")
print("RESOURCES_AND_SQLITE_NATIVE_OK")