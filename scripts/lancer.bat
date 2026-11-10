@echo off
setlocal
cd /d "%~dp0.."
call scripts\java21.bat
if errorlevel 1 goto erreur
call mvnw.cmd javafx:run
if errorlevel 1 goto erreur
exit /b 0
:erreur
echo Echec du lancement. Consultez les messages ci-dessus et verifiez Java 21 et Internet.
pause
exit /b 1