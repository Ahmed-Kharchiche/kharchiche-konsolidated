
@echo off
setlocal
cd /d "%~dp0"

echo ==========================================
echo   KHARCHICHE KONSOLIDATED - PUBLICATION
echo ==========================================
echo.

REM Verifier la branche
for /f "delims=" %%B in ('git branch --show-current') do set "BRANCH=%%B"

if /I not "%BRANCH%"=="main" (
    echo ERREUR : tu dois etre sur la branche main.
    pause
    exit /b 1
)

REM Verifier que le depot distant est accessible
git fetch origin main
if errorlevel 1 goto erreur

REM Eviter une publication si main est en retard
for /f %%C in ('git rev-list --count HEAD..origin/main') do set "BEHIND=%%C"

if not "%BEHIND%"=="0" (
    echo ERREUR : des modifications sont disponibles sur GitHub.
    echo Recupere-les avec git pull --rebase origin main.
    pause
    exit /b 1
)

REM Compilation et tests
echo.
echo [1/4] Compilation et tests...
call mvnw.cmd clean verify
if errorlevel 1 goto erreur

REM Afficher les modifications
echo.
echo [2/4] Modifications detectees :
git status --short

echo.
echo [3/4] Preparation du commit...
git add -u

git diff --cached --quiet
if not errorlevel 1 (
    echo Aucune modification suivie a publier.
    pause
    exit /b 0
)

set /p "MESSAGE=Message du commit : "

if "%MESSAGE%"=="" (
    set "MESSAGE=chore(release): mise a jour de l'application"
)

git commit -m "%MESSAGE%"
if errorlevel 1 goto erreur

REM Publication
echo.
echo [4/4] Envoi sur GitHub...
git push origin main
if errorlevel 1 goto erreur

echo.
echo ==========================================
echo   PUBLICATION REUSSIE !
echo   GitHub Actions peut generer les versions.
echo ==========================================
pause
exit /b 0

:erreur
echo.
echo ERREUR : publication interrompue.
pause
exit /b 1
