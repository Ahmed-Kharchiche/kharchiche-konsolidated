@echo off
cd /d "C:\BUT\kharchiche-konsolidated"
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call .\mvnw.cmd javafx:run
pause