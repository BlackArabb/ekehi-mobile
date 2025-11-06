@echo off
echo Adding Gradle to PATH...

REM Add Gradle to the PATH
setx PATH "%PATH%;C:\gradle\gradle-8.10.2-all\gradle-8.10.2\bin"

echo.
echo Gradle has been added to your PATH.
echo Please restart your command prompt or terminal for the changes to take effect.
echo.
echo To verify the installation, open a new command prompt and run:
echo gradle --version
echo.
pause