# Publication helpers: generated trees only; SQLite, PDFs and unexpected changes stop replacement.
function Get-GeneratedFiles([string]$Path, [string]$Root) {
    $resolved = [IO.Path]::GetFullPath($Path)
    $project = [IO.Path]::GetFullPath($Root).TrimEnd('\')
    $allowed = @((Join-Path $project 'target/windows-build-tmp'), (Join-Path $project 'dist/build/Kharchiche Konsolidated'))
    if (!($allowed | Where-Object { $resolved -eq $_ -or $resolved.StartsWith($_ + '\', [StringComparison]::OrdinalIgnoreCase) })) {
        throw "Chemin de generation non autorise : $resolved"
    }
    # Do not follow a junction in dist/target or another ancestor of the tree.
    $ancestor = $resolved
    while ($ancestor -and $ancestor.StartsWith($project, [StringComparison]::OrdinalIgnoreCase)) {
        if (Test-Path -LiteralPath $ancestor) {
            if ((Get-Item -LiteralPath $ancestor -Force).Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Lien dans un parent de $resolved ; operation refusee."
            }
        }
        if ($ancestor -eq $project) { break }
        $ancestor = Split-Path -Parent $ancestor
    }
    if (!(Test-Path -LiteralPath $resolved)) { return }
    $items = @(Get-Item -LiteralPath $resolved -Force) + @(Get-ChildItem -LiteralPath $resolved -Recurse -Force)
    if ($items | Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint }) {
        throw "Lien detecte dans $resolved ; aucune suppression."
    }
    $files = @($items | Where-Object { !$_.PSIsContainer })
    if ($files | Where-Object { $_.Name -match '\.(db|sqlite|sqlite3)([-.].*)?$|\.pdf$|\.bak$|\.backup$' }) {
        throw "Base, PDF ou sauvegarde dans $resolved ; conservez ces donnees hors de la distribution."
    }
    return $files
}

function Remove-GeneratedTree([string]$Path, [string]$Root) {
    if (Test-Path -LiteralPath $Path) {
        Get-GeneratedFiles $Path $Root | Out-Null
        Remove-Item -LiteralPath ([IO.Path]::GetFullPath($Path)) -Recurse -Force
    }
}

function Write-DistributionManifest([string]$App, [string]$Root) {
    $files = @(Get-GeneratedFiles $App $Root | ForEach-Object {
        [pscustomobject]@{Path=$_.FullName.Substring($App.Length+1); SHA256=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash}
    })
    @{Format=1; BuildId=[guid]::NewGuid().ToString('N'); Files=$files} |
        ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $App '.kharchiche-distribution.json') -Encoding UTF8
}

function Assert-ManagedDistribution([string]$App, [string]$Root) {
    $files = @(Get-GeneratedFiles $App $Root)
    $marker = Join-Path $App '.kharchiche-distribution.json'
    if (!(Test-Path -LiteralPath $marker)) { throw "Distribution non geree : $App. Aucune suppression automatique." }
    $manifest = Get-Content -LiteralPath $marker -Raw | ConvertFrom-Json
    if ($manifest.Format -ne 1) { throw 'Manifeste de distribution inconnu.' }
    $actual = @($files | Where-Object FullName -ne $marker)
    if ($actual.Count -ne @($manifest.Files).Count) { throw 'Fichiers ajoutes/supprimes dans la distribution : remplacement refuse.' }
    $expected = @{}
    foreach ($entry in $manifest.Files) { $expected[$entry.Path] = $entry.SHA256 }
    foreach ($file in $actual) {
        $relative = $file.FullName.Substring($App.Length+1)
        # A running EXE/DLL or another open file prevents any replacement up front.
        try {
            $handle = [IO.File]::Open($file.FullName, [IO.FileMode]::Open, [IO.FileAccess]::Read, [IO.FileShare]::None)
            $handle.Dispose()
        } catch { throw "Fichier verrouille : $relative. Fermez l'application avant de generer." }
        if (!$expected.ContainsKey($relative) -or (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256 -ErrorAction Stop).Hash -ne $expected[$relative]) {
            throw "Fichier ajoute ou modifie : $relative. Remplacement refuse pour preserver vos fichiers."
        }
    }
}

function Publish-WindowsDistribution([string]$Candidate, [string]$Final, [string]$Work, [string]$Root) {
    Get-GeneratedFiles $Final $Root | Out-Null
    $backup = Join-Path $Work 'previous'
    $shortcut = Join-Path $Root 'Kharchiche Konsolidated.lnk'
    $newShortcut = Join-Path $Work 'new-shortcut.lnk'
    $oldShortcut = Join-Path $Work 'previous-shortcut.lnk'
    if (Test-Path -LiteralPath $Final) { Assert-ManagedDistribution $Final $Root }
    $shell = New-Object -ComObject WScript.Shell
    try {
        $link = $shell.CreateShortcut($newShortcut)
        $link.TargetPath = Join-Path $Final 'Kharchiche Konsolidated.exe'
        $link.WorkingDirectory = $Final
        $link.IconLocation = $link.TargetPath + ',0'
        $link.Description = 'Kharchiche Konsolidated'
        $link.Save()
    } finally { [Runtime.InteropServices.Marshal]::FinalReleaseComObject($shell) | Out-Null }
    if (Test-Path -LiteralPath $shortcut) { Copy-Item -LiteralPath $shortcut -Destination $oldShortcut }
    $movedOld = $false
    $movedNew = $false
    try {
        if (Test-Path -LiteralPath $Final) {
            Move-Item -LiteralPath $Final -Destination $backup
            $movedOld = $true
        }
        Move-Item -LiteralPath $Candidate -Destination $Final
        $movedNew = $true
        if (Test-Path -LiteralPath $shortcut) {
            [IO.File]::Replace($newShortcut, $shortcut, (Join-Path $Work 'replaced-shortcut.lnk'))
        } else { Move-Item -LiteralPath $newShortcut -Destination $shortcut }
    } catch {
        if ($movedNew) { Move-Item -LiteralPath $Final -Destination $Candidate }
        if ($movedOld) { Move-Item -LiteralPath $backup -Destination $Final }
        if (Test-Path -LiteralPath $oldShortcut) { Copy-Item -LiteralPath $oldShortcut -Destination $shortcut -Force }
        throw
    }
    if ($movedOld) { Remove-GeneratedTree $backup $Root }
}
