@echo off
setlocal
rem Distribution fixe dist\Kharchiche Konsolidated et raccourci local ; publication apres validation.
cd /d "%~dp0"
set "PATH=%SystemRoot%\System32;%SystemRoot%\System32\WindowsPowerShell\v1.0;%PATH%"
call scripts\java21.bat
if errorlevel 1 exit /b 1
if not defined JAVA_HOME (
  echo Definissez JAVA_HOME vers votre JDK 21 complet.
  exit /b 1
)
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -File "%~dp0scripts\build-windows.ps1"
if errorlevel 1 (
  echo Echec de generation. Consultez le journal indique ci-dessus.
  exit /b 1
)
exit /b 0