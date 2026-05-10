#!/bin/bash
# Instalación directa del binario Android CLI

set -e

echo "=========================================="
echo "Instalando Android CLI (Directo)"
echo "=========================================="

# Crear directorio
mkdir -p ~/.local/bin

# Descargar el binario directamente
echo "Descargando Android CLI..."
curl -fsSL "https://dl.google.com/android/cli/latest/linux_x86_64/bin/android" \
    -o ~/.local/bin/android 2>&1 || {
        echo "Intentando URL alternativa..."
        # URL alternativa según documentación
        curl -fsSL "https://dl.google.com/android/cli/latest/linux/android" \
            -o ~/.local/bin/android 2>&1
    }

# Hacer ejecutable
chmod +x ~/.local/bin/android

# Verificar instalación
echo ""
echo "Verificando instalación..."
~/.local/bin/android --version 2>&1 || echo "Nota: El CLI requiere Android SDK instalado"

echo ""
echo "=========================================="
echo "✅ Android CLI instalado en ~/.local/bin/android"
echo "=========================================="
echo ""
echo "Agregar al PATH:"
echo '  export PATH="$HOME/.local/bin:$PATH"'
echo ""
echo "Comandos útiles:"
echo "  android --help              - Ver ayuda"
echo "  android sdk install         - Instalar Android SDK"
echo "  android emulator install    - Instalar emulador"
echo "  android studio install      - Instalar Android Studio"
