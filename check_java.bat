@echo off
echo Checking for Java installations...

echo.
echo Checking JAVA_HOME:
echo %JAVA_HOME%

echo.
echo Checking for Java in PATH:
where java 2>nul

echo.
echo Checking common Java installation directories:
if exist "C:\Program Files\Java\jdk-24" (
    echo JDK 24 found
    "C:\Program Files\Java\jdk-24\bin\java" -version 2>&1
) else (
    echo JDK 24 not found
)

if exist "C:\Program Files\Java\jdk-17" (
    echo JDK 17 found
    "C:\Program Files\Java\jdk-17\bin\java" -version 2>&1
) else (
    echo JDK 17 not found
)

if exist "C:\Program Files\Java\jdk-11" (
    echo JDK 11 found
    "C:\Program Files\Java\jdk-11\bin\java" -version 2>&1
) else (
    echo JDK 11 not found
)

echo.
echo Done.