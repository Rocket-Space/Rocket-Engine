#!/bin/bash
# Script para compilar Rocket Engine APK

set -e

echo "=========================================="
echo "Compilando Rocket Engine v0.5.4"
echo "=========================================="

# Configurar Java
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

echo "Java version:"
java -version

# Verificar Gradle
if [ -f /tmp/gradle-8.6/bin/gradle ]; then
    echo "Usando Gradle descargado..."
    GRADLE_CMD=/tmp/gradle-8.6/bin/gradle
elif [ -f ./gradlew ]; then
    echo "Usando Gradle Wrapper..."
    GRADLE_CMD=./gradlew
else
    echo "ERROR: No se encuentra Gradle"
    exit 1
fi

# Limpiar build anterior
rm -rf app/build/outputs/apk/

# Compilar APK Debug
echo ""
echo "Iniciando compilación..."
$GRADLE_CMD assembleDebug

# Verificar resultado
if [ -f app/build/outputs/apk/debug/app-debug.apk ]; then
    echo ""
    echo "=========================================="
    echo "✅ COMPILACIÓN EXITOSA!"
    echo "=========================================="
    
    # Copiar a carpeta dist
    mkdir -p dist
    cp app/build/outputs/apk/debug/app-debug.apk dist/RocketEngine-v0.5.4-debug.apk
    
    echo "APK generado:"
    ls -lh dist/RocketEngine-v0.5.4-debug.apk
    echo ""
    echo "Ubicación: $(pwd)/dist/RocketEngine-v0.5.4-debug.apk"
else
    echo ""
    echo "❌ ERROR: No se generó el APK"
    exit 1
fi
