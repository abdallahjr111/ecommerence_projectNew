@echo off
title Perfume eCommerce - Spring Boot

echo.
echo ============================================================
echo   PERFUME eCOMMERCE - Spring Boot Launcher
echo ============================================================
echo.

REM ── Check Java ──────────────────────────────────────────────
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install JDK 17 or higher.
    echo         Download: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo [INFO] Java found:
java -version
echo.

REM ── Set Maven path ──────────────────────────────────────────
set MVN_PATH=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.15\apache-maven-3.9.15\bin\mvn.cmd

if not exist "%MVN_PATH%" (
    echo [INFO] Maven wrapper not found at expected path.
    echo [INFO] Trying system mvn...
    where mvn >nul 2>&1
    if %errorlevel% neq 0 (
        echo [ERROR] Maven not found. Run the project once via Kiro or IntelliJ first.
        pause
        exit /b 1
    )
    set MVN_PATH=mvn
)

echo [INFO] Maven found: %MVN_PATH%
echo.

REM ── Choose database mode ────────────────────────────────────
echo ============================================================
echo   DATABASE MODE
echo ============================================================
echo.
echo   [1] H2 In-Memory  (no setup needed, data resets on restart)
echo   [2] MySQL         (requires MySQL running on localhost:3306)
echo.
set /p DB_CHOICE="Choose [1 or 2, default=1]: "

if "%DB_CHOICE%"=="2" (
    echo.
    echo [INFO] Using MySQL mode.
    echo [INFO] Make sure MySQL is running and ecommerce_db exists.
    set PROFILE=mysql
) else (
    echo.
    echo [INFO] Using H2 in-memory mode.
    set PROFILE=h2
)

REM ── Update active profile ───────────────────────────────────
powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.profiles.active=.*', 'spring.profiles.active=%PROFILE%' | Set-Content 'src\main\resources\application.properties'"

echo.
echo ============================================================
echo   STARTING APPLICATION
echo ============================================================
echo.
echo [INFO] Profile  : %PROFILE%
echo [INFO] Port     : 8080
echo.
echo   Once started, open your browser at:
echo.
echo     Swagger UI  →  http://localhost:8080/swagger-ui.html
echo     H2 Console  →  http://localhost:8080/h2-console
echo     Products    →  http://localhost:8080/api/products
echo.
echo   Test accounts:
echo     Customer  →  customer@perfume.com / customer123
echo     Admin     →  admin@perfume.com    / admin123
echo.
echo ============================================================
echo   Press Ctrl+C to stop the server
echo ============================================================
echo.

REM ── Run ─────────────────────────────────────────────────────
"%MVN_PATH%" spring-boot:run -f pom.xml

echo.
echo [INFO] Server stopped.
pause
