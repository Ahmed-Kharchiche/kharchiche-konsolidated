
@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME="

for %%V in (21 25) do (
    if not defined JAVA_HOME (
        for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-%%V.*") do (
            if exist "%%D\bin\javac.exe" (
                set "JAVA_HOME=%%D"
            )
        )
    )
)

if not defined JAVA_HOME (
    echo.
    echo ERREUR : Java 21 ou Java 25 est requis.
    echo Installez un JDK Temurin 21 ou 25.
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

echo.
echo Version Java utilisee :
java -version
echo.

call "%~dp0mvnw.cmd" javafx:run

pause
endlocal
