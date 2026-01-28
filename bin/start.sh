#!/bin/bash
# Script de inicio para el servicio de Facturacion (Linux/Mac)
# ============================================================

echo "========================================"
echo "Iniciando servicio de Facturacion..."
echo "========================================"

# Ir al directorio raiz del proyecto
cd "$(dirname "$0")/.." || exit 1

# Verificar que el JAR existe
if [ ! -f "target/facturacion-0.0.1-SNAPSHOT.jar" ]; then
    echo "ERROR: No se encontró el archivo JAR en target/facturacion-0.0.1-SNAPSHOT.jar"
    echo "Por favor ejecuta: ./mvnw clean package -DskipTests"
    exit 1
fi

# Crear directorio de logs si no existe
mkdir -p logs

# Nombre del archivo de log con fecha y hora
LOGFILE="logs/facturacion-$(date +%Y%m%d-%H%M%S).log"

echo "Archivo de log: $LOGFILE"
echo ""

# Iniciar la aplicacion en segundo plano y redirigir salida al log
nohup java -jar target/facturacion-0.0.1-SNAPSHOT.jar > "$LOGFILE" 2>&1 &

# Guardar el PID
PID=$!
echo $PID > bin/service.pid

echo ""
echo "========================================"
echo "Servicio iniciado correctamente"
echo "PID: $PID"
echo "Log: $LOGFILE"
echo "========================================"
echo ""
echo "Para verificar el estado, revisa el archivo de log"
echo "Para detener el servicio, ejecuta: bin/stop.sh"
echo ""
