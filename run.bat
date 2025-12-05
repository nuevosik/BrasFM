@echo off
echo ========================================
echo        BrasFM - Football Manager
echo ========================================
echo.

cd /d "%~dp0"

REM Configura Maven temporariamente
set PATH=%USERPROFILE%\maven\apache-maven-3.9.6\bin;%PATH%

echo Verificando compilacao...
call mvn compile -q 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Compilando projeto...
    call mvn compile
    if %ERRORLEVEL% NEQ 0 (
        echo Erro na compilacao!
        pause
        exit /b 1
    )
)

echo.
echo Iniciando o jogo...
echo.

REM Executa o jogo usando Maven
call mvn exec:java -q

pause
