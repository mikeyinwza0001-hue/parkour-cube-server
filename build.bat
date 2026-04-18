@echo off
setlocal enabledelayedexpansion

set PLUGIN_NAME=ParkourCube
set MAVEN_HOME=D:\Coding Project\Minecraft\Parkour Cube\apache-maven-3.9.12
set SERVER_PLUGINS=D:\Coding Project\Minecraft\Parkour Cube\minecraft-server\plugins
set SERVER_ROOT=D:\Coding Project\Minecraft\Parkour Cube\minecraft-server
set PATH=%MAVEN_HOME%\bin;%PATH%

:: --- Read version from pom.xml ---
for /f "tokens=3 delims=<>" %%a in ('findstr "<version>" "%~dp0pom.xml"') do (
    set VERSION=%%a
    goto :got_version
)
:got_version
echo =========================================
echo  Building %PLUGIN_NAME% v%VERSION%
echo =========================================

call mvn clean package -f "%~dp0pom.xml"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo =========================================
    echo  BUILD FAILED
    echo =========================================
    pause
    exit /b 1
)

set NEW_JAR=%PLUGIN_NAME%-%VERSION%.jar

echo.
echo =========================================
echo  BUILD SUCCESS: %NEW_JAR%
echo =========================================

:: --- Remove old JARs from server plugins ---
echo.
echo Cleaning old %PLUGIN_NAME% JARs from server plugins...
pushd "%SERVER_ROOT%"
for %%f in ("%SERVER_PLUGINS%\%PLUGIN_NAME%-*.jar") do (
    if /I not "%%~nxf"=="%NEW_JAR%" (
        echo  Removing: %%~nxf
        git rm -f "plugins/%%~nxf" >nul 2>&1
        if exist "%%f" del "%%f"
    )
)
popd

:: --- Copy new JAR ---
echo  Copying:  %NEW_JAR%
copy /Y "%~dp0target\%NEW_JAR%" "%SERVER_PLUGINS%\%NEW_JAR%" >nul

:: --- Git stage new JAR ---
pushd "%SERVER_ROOT%"
git add "plugins\%NEW_JAR%"
popd

echo.
echo =========================================
echo  Done! %NEW_JAR% deployed to server.
echo  Old versions removed automatically.
echo =========================================
echo.
echo Next steps:
echo   cd "%SERVER_ROOT%"
echo   git commit -m "update %PLUGIN_NAME% to v%VERSION%"
echo   git tag v%VERSION%
echo   git push origin master --tags
echo.
pause
