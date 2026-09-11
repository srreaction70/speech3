#!/bin/bash
set -e

echo "======================================================================"
echo "      EchoScript Voice - Android APK Automated Build Script (Mac/Linux)"
echo "======================================================================"
echo ""

echo "[1/3] Making gradlew executable..."
chmod +x ./gradlew 2>/dev/null || true

echo ""
echo "[2/3] Compiling and generating Debug APK..."
echo "Please wait while Gradle builds the APK..."
echo ""

./gradlew assembleDebug

echo ""
echo "======================================================================"
echo "  [SUCCESS] APK build completed successfully!"
echo "======================================================================"
echo ""
echo "Your ready-to-install Android APK is located at:"
echo "  app/build/outputs/apk/debug/app-debug.apk"
echo ""

if [[ "$OSTYPE" == "darwin"* ]]; then
    open "app/build/outputs/apk/debug" 2>/dev/null || true
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    xdg-open "app/build/outputs/apk/debug" 2>/dev/null || true
fi
