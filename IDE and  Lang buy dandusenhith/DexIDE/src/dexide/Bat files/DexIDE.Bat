@echo off
title Dex IDE Launcher
echo ==========================================================
echo        Starting Dex IDE (Beast Edition)
echo                                            By dandusnehith
echo ==========================================================
echo.

REM Change directory to DexIDE
cd /d "F:\Repos\IDE and  Lang buy dandusenhith\DexIDE"

REM Compile DexIDE
echo Compiling DexIDE...
javac -cp "..\DexLang\out" -d out src\dexide\DexIDE.java

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation FAILED.
    echo Fix the errors above and run again.
    pause
    exit /b
)

echo Compilation successful.
echo.

REM Run DexIDE
echo Launching Dex IDE...
echo.
java -cp "out;..\DexLang\out" dexide.DexIDE

echo.
echo Dex IDE closed.
pause
