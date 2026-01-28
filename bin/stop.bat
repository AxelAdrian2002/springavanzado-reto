@echo off
REM Script de detencion para el servicio de Facturacion
REM ===================================================

echo ========================================
echo Deteniendo servicio de Facturacion...
echo ========================================

REM Ir al directorio raiz del proyecto
cd /d "%~dp0.."

REM Detener todos los procesos Java
echo Deteniendo todos los procesos Java...
powershell -Command "Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force"

REM Limpiar archivo PID si existe
if exist "bin\service.pid" del bin\service.pid

echo.
echo ========================================
echo Servicio detenido correctamente
echo ========================================
echo.
