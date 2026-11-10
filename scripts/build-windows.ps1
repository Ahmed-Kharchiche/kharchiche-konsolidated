# Windows packaging only; never reads, moves or deletes application databases.
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$oldTemp = $env:TEMP
$oldTmp = $env:TMP
$oldPath = $env:PATH
$failed = $false
$lock = $null
$work = $null
$logDir = Join-Path $root 'target/windows-build-logs'
$final = Join-Path $root 'dist/Kharchiche Konsolidated'
. (Join-Path $PSScriptRoot 'windows-distribution.ps1')

function Invoke-BuildTool {
    param([string]$Tool, [string[]]$ToolArguments, [string]$Log)
    Write-Host ("Etape : " + [IO.Path]::GetFileName($Tool) + " ; journal : " + $Log)
    $previousPreference = $ErrorActionPreference
    try {
        # Windows PowerShell treats native stderr (including JVM warnings) as ErrorRecord.
        # The process exit code, rather than its use of stderr, determines failure.
        $ErrorActionPreference = 'Continue'
        & $Tool @ToolArguments 2>&1 | ForEach-Object { $_.ToString() } | Tee-Object -FilePath $Log
        $toolExit = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousPreference
    }
    if ($toolExit -ne 0) { throw "Code $toolExit : $Tool. Journal : $Log" }
}

function Test-WritableDirectory([string]$Directory) {
    New-Item -ItemType Directory -Path $Directory -Force | Out-Null
    $probe = Join-Path $Directory ([guid]::NewGuid().ToString('N') + '.write-probe')
    [IO.File]::WriteAllText($probe, 'write test')
    [IO.File]::Delete($probe)
}

Push-Location $root
try {
    $windowsMajor = (Get-ItemProperty -LiteralPath 'HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion').CurrentMajorVersionNumber
    if ($windowsMajor -lt 10 -or ![Environment]::Is64BitProcess -or
        !(Test-Path -LiteralPath (Join-Path $env:SystemRoot 'System32/ucrtbase.dll'))) {
        throw 'Ce packaging cible Windows 10/11 x64 avec son UCRT systeme.'
    }
    if (!$env:JAVA_HOME) { throw 'JAVA_HOME doit designer un JDK 21 complet.' }
    $jdk = (Resolve-Path -LiteralPath $env:JAVA_HOME).Path
    foreach ($tool in @('java.exe', 'jlink.exe', 'jpackage.exe')) {
        if (!(Test-Path -LiteralPath (Join-Path $jdk "bin/$tool"))) { throw "Outil absent : $jdk/bin/$tool" }
    }
    if (!(Test-Path -LiteralPath (Join-Path $jdk 'jmods/java.base.jmod'))) {
        throw 'Les JMODs du JDK 21 sont requis.'
    }
    $java = Join-Path $jdk 'bin/java.exe'
    $ErrorActionPreference = 'Continue'
    $settings = & $java -XshowSettings:properties -version 2>&1 | Out-String
    $javaExit = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($javaExit -ne 0) { throw 'Impossible de lancer le JDK selectionne.' }
    if ($settings -notmatch '(?m)^\s*java[.]specification[.]version = 21\s*$') {
        throw 'Le packaging doit utiliser un JDK 21.'
    }
    if ($settings -notmatch '(?m)^\s*os[.]arch = (amd64|x86_64)\s*$') {
        throw 'Le packaging requiert un JDK 21 x64.'
    }

    New-Item -ItemType Directory -Force (Join-Path $root 'target'), (Join-Path $root 'dist'), $logDir | Out-Null
    try { $lock = [IO.File]::Open((Join-Path $root 'target/windows-build.lock'), 'OpenOrCreate', 'ReadWrite', 'None') }
    catch { throw 'Une autre generation Windows est en cours.' }
    $work = Join-Path $root 'target/windows-build-tmp'
    $previous = Join-Path $work 'previous'
    # Recover an interrupted rename before removing any temporary generation.
    if (Test-Path -LiteralPath $previous) {
        Assert-ManagedDistribution $previous $root
        if (!(Test-Path -LiteralPath $final)) { Move-Item -LiteralPath $previous -Destination $final }
        else {
            Assert-ManagedDistribution $final $root
            Remove-GeneratedTree $previous $root
        }
    }
    if (Test-Path -LiteralPath $final) { Assert-ManagedDistribution $final $root }
    Remove-GeneratedTree $work $root
    $inputDir = Join-Path $work 'input'
    $runtime = Join-Path $work 'runtime'
    $nativeTemp = Join-Path $work 'os-temp'
    $packageTemp = Join-Path $work 'jpackage-temp'
    $destination = Join-Path $work 'image'
    foreach ($directory in @($work, $inputDir, $nativeTemp, $destination)) {
        Test-WritableDirectory $directory
    }
    Write-Host "JDK : $jdk"
    Write-Host "Travail : $work"

    $env:PATH = "$jdk/bin;$env:SystemRoot/System32;$env:SystemRoot/System32/WindowsPowerShell/v1.0;$oldPath"
    Invoke-BuildTool (Join-Path $root 'mvnw.cmd') @('-B', "-Dapp.packageDirectory=$inputDir", 'verify') (Join-Path $logDir 'maven.log')
    Copy-Item -LiteralPath (Join-Path $root 'target/kharchiche-konsolidated.jar') -Destination $inputDir
    if (!(Test-Path -LiteralPath (Join-Path $inputDir 'lib/sqlite-jdbc-3.46.0.0.jar'))) {
        throw 'Les dependances runtime ne sont pas presentes dans le dossier de packaging.'
    }

    # JavaFX also bundles UCRT in its Windows JAR. Remove only that redundant OS
    # resource from the staged copies so its loader falls back to the system UCRT.
    # Maven's original artifacts and all JavaFX classes/native engines stay intact.
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    foreach ($jar in Get-ChildItem -LiteralPath (Join-Path $inputDir 'lib') -Filter 'javafx-*-win.jar') {
        $archive = [IO.Compression.ZipFile]::Open($jar.FullName, [IO.Compression.ZipArchiveMode]::Update)
        try {
            $redundant = @($archive.Entries | Where-Object Name -eq 'ucrtbase.dll')
            if ($redundant.Count -gt 0) {
                if ($archive.Entries | Where-Object FullName -match '^META-INF/[^/]+[.](SF|RSA|DSA|EC)$') {
                    throw "JAR signe : impossible de retirer la ressource UCRT redondante de $($jar.Name)."
                }
                foreach ($entry in $redundant) { $entry.Delete() }
                Write-Host "UCRT systeme utilise ; ressource locale retiree de la copie de $($jar.Name)."
            }
        } finally { $archive.Dispose() }
    }

    $env:TEMP = $nativeTemp
    $env:TMP = $nativeTemp

    # Run jlink in its own process, outside the final distribution.
    # Windows 10/11 always load the system UCRT, even when an app-local copy exists.
    # Do not redeploy that OS component; retain all other native libraries.
    # https://learn.microsoft.com/en-us/cpp/windows/universal-crt-deployment
    Invoke-BuildTool (Join-Path $jdk 'bin/jlink.exe') @(
        "-J-Djava.io.tmpdir=$nativeTemp",
        '--module-path', (Join-Path $jdk 'jmods'),
        '--add-modules', 'java.se,jdk.unsupported,jdk.crypto.ec',
        '--strip-debug', '--no-header-files', '--no-man-pages',
        '--exclude-files', '**/ucrtbase.dll',
        '--output', $runtime
    ) (Join-Path $logDir 'jlink.log')

    Invoke-BuildTool (Join-Path $runtime 'bin/java.exe') @('--list-modules') (Join-Path $logDir 'runtime-modules.log')

    # Supplying --runtime-image prevents jpackage from invoking jlink internally.
    Invoke-BuildTool (Join-Path $jdk 'bin/jpackage.exe') @(
        "-J-Djava.io.tmpdir=$nativeTemp",
        '--verbose', '--type', 'app-image',
        '--name', 'Kharchiche Konsolidated', '--app-version', '1.0.0',
        '--vendor', 'Ahmed Kharchiche',
        '--input', $inputDir,
        '--main-jar', 'kharchiche-konsolidated.jar', '--main-class', 'Launcher',
        '--java-options', '-Dkharchiche.packaged=true',
        '--runtime-image', $runtime,
        '--temp', $packageTemp, '--dest', $destination
    ) (Join-Path $logDir 'jpackage.log')

    $app = Join-Path $destination 'Kharchiche Konsolidated'
    $exe = Join-Path $app 'Kharchiche Konsolidated.exe'
    foreach ($file in @($exe, (Join-Path $app 'runtime/bin/server/jvm.dll'))) {
        if (!(Test-Path -LiteralPath $file)) { throw "Fichier attendu absent : $file" }
    }
    Invoke-BuildTool (Join-Path $app 'runtime/bin/java.exe') @('-version') (Join-Path $logDir 'packaged-java.log')

    Write-DistributionManifest $app $root
    Publish-WindowsDistribution $app $final $work $root
    $exe = Join-Path $final 'Kharchiche Konsolidated.exe'
    Write-Host "Application creee : $exe"
    Write-Host "Raccourci local : $(Join-Path $root 'Kharchiche Konsolidated.lnk')"
    Write-Host 'Distribuez le dossier Kharchiche Konsolidated entier (app et runtime inclus).'
} catch {
    $failed = $true
    Write-Host ("ERREUR : " + $_.Exception.Message)
    Write-Host "Journaux : $logDir"
} finally {
    $env:TEMP = $oldTemp
    $env:TMP = $oldTmp
    $env:PATH = $oldPath
    if ($work -and !(Test-Path -LiteralPath (Join-Path $work 'previous'))) {
        try { Remove-GeneratedTree $work $root }
        catch { Write-Host "Dossier temporaire conserve par securite : $work"; $failed = $true }
    }
    if ($lock) { $lock.Dispose() }
    Pop-Location
}
if ($failed) { exit 1 }
exit 0