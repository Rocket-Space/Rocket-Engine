#!/bin/bash
# Corregir error SDK y compilar

set -e

echo "=========================================="
echo "🔧 Corrigiendo error: SDK no encontrado"
echo "=========================================="

export PATH="$HOME/.local/bin:$PATH"

# PASO 1: Instalar Android SDK
echo ""
echo "📦 Instalando Android SDK..."
android sdk install --channel=stable

# PASO 2: Configurar variables
echo ""
echo "⚙️  Configurando entorno..."
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator

echo "ANDROID_HOME=$ANDROID_HOME"

# PASO 3: Crear local.properties
echo ""
echo "📝 Creando local.properties..."
echo "sdk.dir=$ANDROID_HOME" > /home/rocket/Proyectos/Windsurf/Rocket-Engine/local.properties
echo "✅ local.properties creado"
cat /home/rocket/Proyectos/Windsurf/Rocket-Engine/local.properties

# PASO 4: Instalar build tools
echo ""
echo "🔧 Instalando build tools..."
if [ -f $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager ]; then
    yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses 2>/dev/null || true
    $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-34" "build-tools;34.0.0" 2>/dev/null || true
fi

# PASO 5: Compilar
echo ""
echo "🔨 Compilando APK..."
cd /home/rocket/Proyectos/Windsurf/Rocket-Engine
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

./gradlew clean assembleDebug

# Verificar
echo ""
echo "=========================================="
if [ -f app/build/outputs/apk/debug/app-debug.apk ]; then
    echo "✅ COMPILACIÓN EXITOSA!"
    echo "=========================================="
    mkdir -p dist
    cp app/build/outputs/apk/debug/app-debug.apk dist/RocketEngine-v0.5.4-debug.apk
    ls -lh dist/RocketEngine-v0.5.4-debug.apk
    
    echo ""
    echo "🎯 Siguiente pasos:"
    echo "1. Conecta un dispositivo Android o inicia emulador"
    echo "2. adb install dist/RocketEngine-v0.5.4-debug.apk"
else
    echo "❌ Error en compilación"
    exit 1
fi
