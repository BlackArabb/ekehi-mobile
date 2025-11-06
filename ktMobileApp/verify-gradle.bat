@echo off
echo Checking Gradle installation...

echo.
echo Looking for Gradle in PATH...
where gradle >nul 2>&1
if %errorlevel% == 0 (
    echo Gradle found in PATH!
    echo.
    echo Gradle version:
    gradle --version
) else (
    echo Gradle not found in PATH.
    echo.
    echo Please run 'add-gradle-to-path.bat' first and restart your command prompt.
)

echo.
pause