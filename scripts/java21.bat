@echo off
if defined JAVA_HOME (
  set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_CMD=java"
)
"%JAVA_CMD%" -XshowSettings:properties -version 2>&1 | findstr /R /C:"java.specification.version = 21$" >nul
if errorlevel 1 (
  echo Java 21 est requis. Installez un JDK 21 et configurez JAVA_HOME.
  exit /b 1
)
exit /b 0