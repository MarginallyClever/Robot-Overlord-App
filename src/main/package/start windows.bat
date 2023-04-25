@echo off
setlocal enabledelayedexpansion

:: Detect system architecture and set Java executable path
if "%PROCESSOR_ARCHITECTURE%"=="x86" (
    set JAVA_EXE=java.exe
) else (
    if exist "%ProgramFiles(x86)%\Java\jre*\bin\java.exe" (
        set JAVA_EXE="%ProgramFiles(x86)%\Java\jre*\bin\java.exe"
    ) else (
        set JAVA_EXE=java.exe
    )
)

:: Check if Java is installed
where %JAVA_EXE% >nul 2>nul
if errorlevel 1 (
    echo Java is not installed. Please install Java 15 or later.
    pause
    exit /b 1
)

:: Get Java version
for /f "tokens=3" %%g in ('%JAVA_EXE% -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%g
echo Java version: %JAVA_VERSION%

:: Check if the Java version is at least 15
for /f "tokens=1 delims=." %%a in ("%JAVA_VERSION%" ) do set MAJOR_VERSION=%%a
if %MAJOR_VERSION% lss 15 (
    echo The installed Java version is too old. Please update to at least Java 15.
    pause
    exit /b 1
)

:: Find and run the JAR file
set JAR_FILE=
for %%f in (RobotOverlord*-with-dependencies.jar) do set JAR_FILE=%%f

if not "%JAR_FILE%"=="" (
    echo Running JAR file: %JAR_FILE%
    %JAVA_EXE% -jar %JAR_FILE%
) else (
    echo No matching JAR file found.
)
pause
