
@echo off
setlocal
cd /d "%~dp0"

rem Detection automatique du JDK 21 Temurin
set "JDK_DIR="

for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do (
    if exist "%%~fD\bin\javac.exe" (
        set "JDK_DIR=%%~fD"
    )
)

if not defined JDK_DIR (
    echo ERREUR : JDK 21 introuvable.
    echo Installez Temurin JDK 21 dans C:\Program Files\Eclipse Adoptium
    pause
    exit /b 1
)

rem Configuration de Java 21
set "JAVA_HOME=%JDK_DIR%"
set "PATH=%JAVA_HOME%\bin;%SystemRoot%\System32;%SystemRoot%\System32\WindowsPowerShell\v1.0;%PATH%"

echo.
echo ==========================================
echo    KHARCHICHE KONSOLIDATED - BUILD
echo ==========================================
echo.
echo JDK utilise : %JAVA_HOME%
echo.

"%JAVA_HOME%\bin\java.exe" -version

echo.
echo Generation de la distribution Windows...
echo.

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-windows.ps1"

if errorlevel 1 (
    echo.
    echo ERREUR : echec de generation.
    echo Consultez les journaux de compilation.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo    DISTRIBUTION GENEREE AVEC SUCCES
echo ==========================================
echo.
echo Emplacement :
echo dist\build\Kharchiche Konsolidated\
echo.

pause
exit /b 0
