@echo off
chcp 65001 > nul
cls
echo ======================================================================
echo       EchoScript Voice - Android APK Automated Build Script
echo ======================================================================
echo.

echo [1/3] Checking environment and Gradle wrapper...
if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat not found in the current directory!
    echo Please make sure you extract the entire zip before running this script.
    pause
    exit /b 1
)

echo.
echo [2/3] Compiling and generating Debug APK (assembleDebug)...
echo Please wait while Gradle downloads dependencies and builds the APK...
echo.

call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================================================
    echo   [SUCCESS] APK build completed successfully!
    echo ======================================================================
    echo.
    echo Your ready-to-install Android APK is located at:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Opening output directory...
    if exist "app\build\outputs\apk\debug" (
        explorer "app\build\outputs\apk\debug"
    )
) else (
    echo.
    echo ======================================================================
    echo   [BUILD FAILED]
    echo ======================================================================
    echo Possible causes:
    echo 1. Java JDK (version 17 or higher) is not installed or not in system PATH.
    echo    Download JDK: https://adoptium.net/
    echo 2. Android SDK is missing. You can simply open this project in Android Studio.
)

echo.
pause
