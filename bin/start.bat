@echo off
REM Script de inicio para el servicio de Facturacion
REM ================================================

echo ========================================
echo Iniciando servicio de Facturacion...
echo ========================================

REM Ir al directorio raiz del proyecto
cd /d "%~dp0.."

REM Verificar que el JAR existe
if not exist "target\facturacion-0.0.1-SNAPSHOT.jar" (
    echo ERROR: No se encontro el archivo JAR en target\facturacion-0.0.1-SNAPSHOT.jar
    echo Por favor ejecuta: mvnw.cmd clean package -DskipTests
    pause
    exit /b 1
)

REM Crear directorio de logs si no existe
if not exist "logs" mkdir logs

REM Nombre del archivo de log con fecha y hora
set LOGFILE=logs\facturacion-%date:~-4,4%%date:~-7,2%%date:~-10,2%-%time:~0,2%%time:~3,2%%time:~6,2%.log
set LOGFILE=%LOGFILE: =0%

echo Archivo de log: %LOGFILE%
echo.
echo Iniciando servicio...
echo Presiona Ctrl+C para detener el servicio
echo.

REM Ejecutar Java y duplicar salida a consola y archivo usando PowerShell
powershell -Command "& { java -jar target\facturacion-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath '%LOGFILE%' }"

echo.
echo ========================================
echo Servicio finalizado
echo ========================================
echo.
