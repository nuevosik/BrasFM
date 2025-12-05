@echo off
echo ========================================
echo        BrasFM - Football Manager
echo ========================================
echo.
echo Iniciando o jogo...
echo.

cd /d "%~dp0"
java -cp "target\classes;lib\flatlaf-3.2.5.jar" com.brasfm.ui.GameWindow

pause
