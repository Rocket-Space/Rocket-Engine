#!/bin/bash
# Script completo: Compilar -> Instalar -> Probar Rocket Engine
# Incluye verificación de audio y UI

set -e

echo "=========================================="
echo "🚀 ROCKET ENGINE - Build & Test Complete"
echo "=========================================="

# Configurar entorno
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$JAVA_HOME/bin:$PATH:$HOME/.local/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator

cd /home/rocket/Proyectos/Windsurf/Rocket-Engine

# ============================================================
# PASO 1: COMPILAR
# ============================================================
echo ""
echo "📦 PASO 1: Compilando APK..."
echo "=========================================="

# Usar gradle descargado si está disponible
if [ -f /tmp/gradle-8.6/bin/gradle ]; then
    export PATH=/tmp/gradle-8.6/bin:$PATH
    echo "✅ Usando Gradle descargado"
else
    echo "⚠️  Usando Gradle Wrapper (puede descargar)"
fi

# Compilar
./gradlew clean assembleDebug 2>&1 | tee /tmp/build-output.log

# Verificar resultado
if [ ! -f app/build/outputs/apk/debug/app-debug.apk ]; then
    echo ""
    echo "❌ ERROR: No se generó el APK"
    echo "Revisando errores..."
    tail -50 /tmp/build-output.log
    exit 1
fi

echo ""
echo "✅ APK compilado exitosamente!"
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Copiar a dist
mkdir -p dist
cp app/build/outputs/apk/debug/app-debug.apk dist/RocketEngine-v0.5.4-debug.apk
echo "✅ APK copiado a dist/"

# ============================================================
# PASO 2: VERIFICAR EMULADOR/DISPOSITIVO
# ============================================================
echo ""
echo "📱 PASO 2: Verificando dispositivo..."
echo "=========================================="

# Verificar si hay dispositivo conectado
adb devices
DEVICE_COUNT=$(adb devices | grep -v "List" | grep -v "^$" | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo ""
    echo "⚠️  No hay dispositivo/emulador conectado"
    echo ""
    echo "Opciones:"
    echo "1. Conecta un dispositivo Android con USB debugging activado"
    echo "2. Crea un emulador:"
    echo "   avdmanager create avd -n TestDevice -k \"system-images;android-34;google_apis_playstore;x86_64\" --device \"pixel_6\""
    echo "   emulator -avd TestDevice"
    echo ""
    echo "Una vez conectado el dispositivo, ejecuta:"
    echo "   ./full-build-and-test.sh"
    exit 1
fi

echo "✅ Dispositivo encontrado:"
adb devices

# ============================================================
# PASO 3: INSTALAR APK
# ============================================================
echo ""
echo "📲 PASO 3: Instalando APK..."
echo "=========================================="

# Desinstalar versión anterior si existe
adb shell pm uninstall it.pixiekevin.rocketengine 2>/dev/null || true

# Instalar nueva versión
adb install -r dist/RocketEngine-v0.5.4-debug.apk

echo "✅ APK instalada!"

# ============================================================
# PASO 4: LANZAR APP
# ============================================================
echo ""
echo "🚀 PASO 4: Lanzando aplicación..."
echo "=========================================="

adb shell am start -n it.pixiekevin.rocketengine/it.fast4x.rimusic.MainActivity

echo "✅ App lanzada!"
echo "Esperando 5 segundos para que cargue..."
sleep 5

# ============================================================
# PASO 5: CAPTURAS DE PANTALLA
# ============================================================
echo ""
echo "📸 PASO 5: Capturando pantallas..."
echo "=========================================="

# Crear directorio para screenshots
mkdir -p /tmp/rimusic-screenshots

# Screenshot inicial
adb shell screencap -p /sdcard/screenshot1.png
adb pull /sdcard/screenshot1.png /tmp/rimusic-screenshots/01-home.png
echo "✅ Screenshot 1: Home guardado"

# Tocar en posición de búsqueda (aproximada)
echo "Navegando a búsqueda..."
adb shell input tap 540 1800  # Posición aproximada de búsqueda
sleep 2
adb shell screencap -p /sdcard/screenshot2.png
adb pull /sdcard/screenshot2.png /tmp/rimusic-screenshots/02-search.png
echo "✅ Screenshot 2: Search guardado"

# Volver al home
adb shell input keyevent KEYCODE_BACK
sleep 1

# ============================================================
# PASO 6: VERIFICAR REPRODUCCIÓN
# ============================================================
echo ""
echo "🎵 PASO 6: Probando reproducción..."
echo "=========================================="
echo ""
echo "⚠️  NOTA: Para probar reproducción de audio necesitas:"
echo "   1. Buscar una canción en la app"
echo "   2. Reproducirla"
echo "   3. Confirmar si escuchas audio"
echo ""
echo "Capturando estado del reproductor..."

# Intentar detectar estado del reproductor
adb shell dumpsys media_session | head -30 > /tmp/media_session.log 2>/dev/null || true
echo "✅ Estado del reproductor guardado en /tmp/media_session.log"

# Verificar si hay audio activo
AUDIO_PROCESSES=$(adb shell ps | grep -E "(audio|music|media)" | wc -l)
echo "Procesos de audio detectados: $AUDIO_PROCESSES"

# ============================================================
# PASO 7: UI DUMP (Jerarquía de interfaz)
# ============================================================
echo ""
echo "📊 PASO 7: Exportando jerarquía UI..."
echo "=========================================="

adb shell uiautomator dump /sdcard/window_dump.xml
adb pull /sdcard/window_dump.xml /tmp/rimusic-screenshots/ui-dump.xml
echo "✅ Jerarquía UI guardada"

# ============================================================
# PASO 8: RESUMEN
# ============================================================
echo ""
echo "=========================================="
echo "✅ PRUEBA COMPLETADA!"
echo "=========================================="
echo ""
echo "📁 Archivos generados:"
echo "   APK: dist/RocketEngine-v0.5.4-debug.apk"
echo "   Screenshots: /tmp/rimusic-screenshots/"
echo "   UI Dump: /tmp/rimusic-screenshots/ui-dump.xml"
echo "   Log: /tmp/build-output.log"
echo ""
echo "🔍 Para verificar audio:"
echo "   1. Mira el dispositivo/emulador"
echo "   2. Busca y reproduce una canción"
echo "   3. Confirma si el tiempo avanza y escuchas audio"
echo ""
echo "📸 Screenshots capturadas:"
ls -lh /tmp/rimusic-screenshots/
echo ""
echo "🎧 ¿Se escucha audio y avanza el tiempo? (Responde sí/no)"
