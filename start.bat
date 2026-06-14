@echo off
cd /d "%~dp0"

echo ================================
echo    CogniFlow - Yi Jian Qi Dong
echo ================================
echo.
echo Dir: %cd%
echo.

set "JAVA_HOME=F:\jdk25\zulu25.30.17-ca-jdk25.0.1-win_x64"
set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 9.6\bin"
set "PATH=%JAVA_HOME%\bin;%MYSQL_BIN%;%PATH%"
set "ARK_API_KEY=ark-3daed409-4a92-4045-93d7-f8a5a7e46abb-deafb"
set "VOLCENGINE_SESSION_TOKEN=ep-20260609191412-v2nps"

call :main
pause
exit /b

:main
echo [1/3] Checking MySQL ...
"%MYSQL_BIN%\mysqladmin.exe" ping -u root -proot >nul 2>nul
if not errorlevel 1 (
    echo MySQL already running
    goto check_jar
)

echo Starting MySQL ...
start "" "%MYSQL_BIN%\mysqld.exe" --datadir="C:\tools\mysql\data"
echo Waiting for MySQL (10s) ...
timeout /t 10 /nobreak >nul

"%MYSQL_BIN%\mysqladmin.exe" ping -u root -proot >nul 2>nul
if errorlevel 1 (
    echo ERROR: MySQL failed to start!
    echo Check C:\tools\mysql\data\*.err for details
    exit /b 1
)
echo MySQL OK

:check_jar
echo.
echo [2/3] Checking JAR ...
if not exist "target\cogniflow-learnnav-1.0.0.jar" (
    echo ERROR: JAR not found!
    echo Please run: mvn clean package -DskipTests
    exit /b 1
)
echo JAR OK

echo.
echo [3/3] Starting Spring Boot ...
start "CogniFlow" /MIN javaw -jar "target\cogniflow-learnnav-1.0.0.jar"
echo Waiting for server (15s) ...
timeout /t 15 /nobreak >nul

echo.
echo ================================
echo    Server Ready!
echo    http://localhost:8080
echo ================================
start http://localhost:8080
exit /b 0
