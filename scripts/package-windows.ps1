# Requires the application image built by build-windows.ps1 and WiX Toolset 3.
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$app = Join-Path $root 'dist/build/Kharchiche Konsolidated'
$output = Join-Path $root 'dist/windows'
$jpackage = Join-Path $env:JAVA_HOME 'bin/jpackage.exe'
if (!(Test-Path -LiteralPath (Join-Path $app 'Kharchiche Konsolidated.exe'))) {
    throw 'Construisez la distribution Windows avant de creer les paquets.'
}
New-Item -ItemType Directory -Force -Path $output | Out-Null
$staging = Join-Path $root ('target/windows-installer-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $staging | Out-Null
try {
    & $jpackage --verbose --type exe --name 'Kharchiche Konsolidated' `
        --app-version '1.0.0' --vendor 'Ahmed Kharchiche' `
        --app-image $app --dest $staging `
        --win-per-user-install --win-menu --win-shortcut --win-dir-chooser `
        --win-upgrade-uuid '52882ee6-0706-4d5a-9ed1-e70504625382'
    if ($LASTEXITCODE -ne 0) { throw "Echec jpackage EXE : $LASTEXITCODE" }
    $installers = @(Get-ChildItem -LiteralPath $staging -Filter '*.exe' -File)
    if ($installers.Count -ne 1) { throw 'Installateur EXE attendu absent ou ambigu.' }
    Copy-Item -LiteralPath $installers[0].FullName -Destination (Join-Path $output 'Kharchiche-Konsolidated-Setup.exe') -Force
    Compress-Archive -LiteralPath $app -DestinationPath (Join-Path $output 'Kharchiche-Konsolidated-Portable.zip') -Force
} finally {
    # Only this invocation's private staging directory can be removed.
    $expectedParent = [IO.Path]::GetFullPath((Join-Path $root 'target'))
    if ((Split-Path -Parent ([IO.Path]::GetFullPath($staging))) -ne $expectedParent) {
        throw "Chemin temporaire inattendu : $staging"
    }
    Remove-Item -LiteralPath $staging -Recurse -Force
}
