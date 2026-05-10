#!/bin/bash
# Compilar Rocket Engine APK y preparar para pruebas

set -e

echo "=========================================="
echo "Compilando Rocket Engine v0.5.4"
echo "=========================================="

# Configurar entorno
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$JAVA_HOME/bin:$PATH:$HOME/.local/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator

# Verificar Java
echo ""
echo "☕ Verificando Java..."
java -version

# Compilar APK Debug
echo ""
echo "🔨 Compilando APK..."
./gradlew assembleDebug

# Verificar resultado
if [ -f app/build/outputs/apk/debug/app-debug.apk ]; then
    echo ""
    echo "=========================================="
    echo "✅ COMPILACIÓN EXITOSA!"
    echo "=========================================="
    
    # Copiar a carpeta dist
    mkdir -p dist
    cp app/build/outputs/apk/debug/app-debug.apk dist/RocketEngine-v0.5.4-debug.apk
    
    echo ""
    echo "📦 APK Generado:"
    ls -lh dist/RocketEngine-v0.5.4-debug.apk
    
    echo ""
    echo "📋 Información del APK:"
    file dist/RocketEngine-v0.5.4-debug.apk
    
    echo ""
    echo "🎯 Para instalar en emulador:"
    echo "  adb install dist/RocketEngine-v0.5.4-debug.apk"
    
    echo ""
    echo "🖼️  Para tomar screenshot:"
    echo "  adb shell screencap -p /sdcard/screenshot.png"
    echo "  adb pull /sdcard/screenshot.png"
    
    echo ""
    echo "📱 Para ver UI hierarchy:"
    echo "  adb shell uiautomator dump /sdcard/window_dump.xml"
    echo "  adb pull /sdcard/window_dump.xml"
    
else
    echo ""
    echo "❌ ERROR: No se generó el APK"
    exit 1
fi
