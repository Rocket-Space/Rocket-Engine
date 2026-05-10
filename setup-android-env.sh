#!/bin/bash
# Configuración completa del entorno Android para Rocket Engine

set -e

echo "=========================================="
echo "Configurando Entorno Android"
echo "=========================================="

export PATH="$HOME/.local/bin:$PATH"

# 1. Instalar Android SDK
echo ""
echo "📦 Instalando Android SDK..."
android sdk install --channel=stable || true

# Configurar ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator

echo "ANDROID_HOME=$ANDROID_HOME"

# 2. Aceptar licencias
echo ""
echo "📋 Aceptando licencias..."
yes | sdkmanager --licenses 2>/dev/null || true

# 3. Instalar herramientas necesarias
echo ""
echo "🔧 Instalando herramientas..."
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" 2>/dev/null || true

# 4. Instalar emulador
echo ""
echo "📱 Instalando emulador..."
sdkmanager "emulator" 2>/dev/null || true

# 5. Descargar imagen del sistema
echo ""
echo "💿 Descargando imagen de sistema..."
sdkmanager "system-images;android-34;google_apis_playstore;x86_64" 2>/dev/null || true

echo ""
echo "=========================================="
echo "✅ Entorno Android configurado!"
echo "=========================================="
echo ""
echo "Para crear emulador:"
echo "  avdmanager create avd -n RocketDevice -k \"system-images;android-34;google_apis_playstore;x86_64\" --device \"pixel_6\""
echo ""
echo "Para iniciar emulador:"
echo "  emulator -avd RocketDevice"
echo ""
echo "Variables a agregar a ~/.bashrc:"
echo "  export ANDROID_HOME=\$HOME/Android/Sdk"
echo "  export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/emulator"
