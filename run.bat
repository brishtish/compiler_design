@echo off
chcp 65001 > nul

if not exist out mkdir out

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -cp out Main %1
