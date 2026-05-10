#!/bin/bash
# Script para instalar Android CLI (herramienta de agente para Android Studio)
# Esta herramienta permite a los agentes AI:
# - Tomar screenshots del emulador/app
# - Analizar la jerarquía de UI en JSON
# - Ver la posición exacta de cada elemento
# - Interactuar con la app programáticamente

set -e

echo "=========================================="
echo "Instalando Android CLI (Agent Tool)"
echo "=========================================="

# Crear directorio de instalación
mkdir -p ~/bin
cd ~/bin

# Descargar Android CLI para Linux
echo "Descargando Android CLI..."
curl -fsSL https://dl.google.com/android/cli/latest/linux_x86_64/install.sh -o install-android-cli.sh

# Hacer ejecutable y ejecutar
chmod +x install-android-cli.sh
bash install-android-cli.sh

# Agregar al PATH si no está
echo ""
echo "Agregando al PATH..."
if ! grep -q "android" ~/.bashrc; then
    echo 'export PATH=$PATH:$HOME/bin' >> ~/.bashrc
    echo 'export PATH=$PATH:$HOME/.local/bin' >> ~/.bashrc
fi

echo ""
echo "=========================================="
echo "✅ Android CLI instalado!"
echo "=========================================="
echo ""
echo "Para usarlo, recarga la terminal o ejecuta:"
echo "  source ~/.bashrc"
echo ""
echo "Comandos disponibles:"
echo "  android --version         - Ver versión"
echo "  android sdk install       - Instalar SDK"
echo "  android emulator start    - Iniciar emulador"
echo "  android studio install    - Instalar Android Studio"
echo ""
echo "Para usar el agente con tu app:"
echo "  1. Compila tu APK: ./gradlew assembleDebug"
echo "  2. Instala en emulador: adb install app/build/outputs/apk/debug/app-debug.apk"
echo "  3. El agente puede tomar screenshots y analizar la UI"
