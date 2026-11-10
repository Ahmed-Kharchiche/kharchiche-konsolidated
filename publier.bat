
@echo off
cd /d "%~dp0"

call mvnw.cmd clean verify
if errorlevel 1 (
    echo ERREUR : compilation ou tests echoues.
    pause
    exit /b 1
)

git status --short
git add -u

git diff --cached --quiet
if not errorlevel 1 (
    echo Aucune modification a publier.
    pause
    exit /b 0
)

git commit -m "feat(app): mise a jour de l'application"
if errorlevel 1 exit /b 1

git push origin main
if errorlevel 1 exit /b 1

echo.
echo Publication reussie ! GitHub Actions va compiler les 4 versions.
pause
