#!/bin/bash
# Script de detencion para el servicio de Facturacion (Linux/Mac)
# ===============================================================

echo "========================================"
echo "Deteniendo servicio de Facturacion..."
echo "========================================"

# Ir al directorio raiz del proyecto
cd "$(dirname "$0")/.." || exit 1

# Verificar si existe el archivo PID
if [ -f "bin/service.pid" ]; then
    PID=$(cat bin/service.pid)
    echo "Deteniendo proceso con PID: $PID"
    
    # Matar el proceso
    if kill $PID 2>/dev/null; then
        echo ""
        echo "========================================"
        echo "Servicio detenido correctamente"
        echo "========================================"
        rm bin/service.pid
    else
        echo ""
        echo "ADVERTENCIA: No se pudo detener el proceso con PID $PID"
        echo "El proceso puede ya no estar activo"
        rm bin/service.pid
    fi
else
    echo ""
    echo "No se encontró archivo PID."
    echo "Intentando detener todos los procesos Java relacionados con facturacion..."
    echo ""
    
    # Buscar y matar procesos Java que contengan facturacion
    pkill -f "facturacion-0.0.1-SNAPSHOT.jar"
    
    echo ""
    echo "========================================"
    echo "Operacion completada"
    echo "========================================"
fi

echo ""
