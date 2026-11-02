@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo JDK 25 was not found at the specified location.
    pause
    exit /b 1
)
where mvn >nul 2>nul
if %errorlevel%==0 (
    set "MVN=mvn"
) else if exist "C:\maven\apache-maven-3.10.0\bin\mvn.cmd" (
    set "MVN=C:\maven\apache-maven-3.10.0\bin\mvn.cmd"
) else (
    echo Maven was not found. Install Maven or add its bin folder to PATH.
    pause
    exit /b 1
)

"%JAVA_HOME%\bin\java.exe" -version

if errorlevel 1 (
    echo Java 25 could not be executed.
    pause
    exit /b 1
)

call "%MVN%" clean test
if errorlevel 1 (
    echo Tests or compilation failed. Review the Maven messages above.
    pause
    exit /b 1
)
call "%MVN%" javafx:run
if errorlevel 1 pause
