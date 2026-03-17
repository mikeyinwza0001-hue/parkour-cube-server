@echo off
echo =========================================
echo  Building ParkourCube Plugin
echo =========================================

set MAVEN_HOME=D:\Coding Project\Minecraft\Parkour Cube\apache-maven-3.9.12
set PATH=%MAVEN_HOME%\bin;%PATH%

call mvn clean package -f "%~dp0pom.xml"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =========================================
    echo  BUILD SUCCESS
    echo =========================================
    echo Output: target\ParkourCube-2.0.0.jar
    echo.
    echo To install: copy target\ParkourCube-2.0.0.jar to your server's plugins folder
) else (
    echo.
    echo =========================================
    echo  BUILD FAILED
    echo =========================================
)
pause
