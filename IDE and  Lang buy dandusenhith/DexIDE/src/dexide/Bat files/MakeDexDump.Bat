@echo off
title Dex Dump Generator

echo ============================================
echo        DexLang Dump File Generator
echo ============================================
echo.

REM === Ask user for project root ===
set /p projectRoot=Enter project root path: 

if not exist "%projectRoot%" (
    echo Project root does not exist.
    pause
    exit /b
)

REM === Create output folders ===
set outputDir=%projectRoot%\.dex-output
set dumpDir=%outputDir%\.dexDump

mkdir "%outputDir%" >nul 2>&1
mkdir "%dumpDir%" >nul 2>&1

REM === Ask for source file ===
set /p sourceFile=Enter path to .dex source file: 

if not exist "%sourceFile%" (
    echo Source file not found.
    pause
    exit /b
)

REM === Read source ===
for /f "delims=" %%a in (%sourceFile%) do (
    set "sourceContent=!sourceContent!%%a\r\n"
)

REM === Timestamp ===
for /f "tokens=1-4 delims=/: " %%a in ("%date% %time%") do (
    set timestamp=%%a%%b%%c-%%d
)

REM === Dump file path ===
set dumpFile=%dumpDir%\build-%timestamp%.log

echo Writing dump file...
echo Timestamp: %timestamp% > "%dumpFile%"
echo. >> "%dumpFile%"
echo === SOURCE === >> "%dumpFile%"
type "%sourceFile%" >> "%dumpFile%"
echo. >> "%dumpFile%"
echo === BUILD LOG === >> "%dumpFile%"
echo (build log goes here) >> "%dumpFile%"
echo. >> "%dumpFile%"
echo === RUN OUTPUT === >> "%dumpFile%"
echo (run output goes here) >> "%dumpFile%"

echo.
echo Dump file created:
echo %dumpFile%
pause
