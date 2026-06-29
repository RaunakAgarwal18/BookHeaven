@echo off
setlocal enabledelayedexpansion
title Library E-Commerce - Start All Services
color 0A

echo ================================================
echo   Library E-Commerce - Starting All Services
echo ================================================
echo.

:: -----------------------------------------------
:: CONFIG - Hardcoded root path
:: -----------------------------------------------
set ROOT=C:\Users\rauna\Desktop\BookHeaven

:: -----------------------------------------------
:: LOAD SECRETS from .env file
:: -----------------------------------------------
if not exist "%ROOT%\.env" (
    echo [ERROR] .env file not found at %ROOT%\.env
    echo Please create it from .env.example:
    echo   copy .env.example .env
    echo Then fill in your actual secret values.
    pause
    exit /b 1
)
echo Loading environment variables from .env...
for /f "usebackq tokens=1,* delims==" %%A in ("%ROOT%\.env") do (
    set "line=%%A"
    if not "%%A"=="" (
        if not "!line:~0,1!"=="#" (
            set "%%A=%%B"
        )
    )
)
echo Environment variables loaded successfully.

:: Ports
set EUREKA_PORT=8761
set GATEWAY_PORT=8090

set USER_PORT=8081
set BOOK_PORT=8082
set REVIEW_PORT=8083
set CART_PORT=8084
set ORDER_PORT=8085
set PAYMENT_PORT=8086
set EMAIL_PORT=8087
set SEARCH_PORT=8088
set FRONTEND_PORT=5173
set ARCH_EXPLORER_PORT=3001

:: -----------------------------------------------
:: STEP -1 - Kill Existing Processes on Required Ports
:: -----------------------------------------------
echo [0/5] Killing any existing processes on required ports...
powershell -Command "$ports = @(%EUREKA_PORT%, %GATEWAY_PORT%, %USER_PORT%, %BOOK_PORT%, %REVIEW_PORT%, %CART_PORT%, %ORDER_PORT%, %PAYMENT_PORT%, %EMAIL_PORT%, %SEARCH_PORT%, %FRONTEND_PORT%, %ARCH_EXPLORER_PORT%); foreach($p in $ports) { $conns = Get-NetTCPConnection -LocalPort $p -ErrorAction SilentlyContinue; foreach($c in $conns) { try { Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue } catch {} } }"
echo Ports cleared!
echo.

:: -----------------------------------------------
:: STEP 0 - Infrastructure (Docker)
:: -----------------------------------------------
echo [0/4] Starting Infrastructure Docker containers...

:: Check if Docker is running
docker info >nul 2>&1
if %errorlevel% equ 0 goto docker_running

echo Docker is not running. Starting Docker Desktop...
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
echo Waiting for Docker engine to initialize...

:wait_for_docker
timeout /t 5 /nobreak >nul
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Still waiting for Docker...
    goto wait_for_docker
)
echo Docker is now running!

:docker_running
echo Starting Redis (loving_blackburn)...
docker start loving_blackburn
echo Starting RabbitMQ (rabbitmq)...
docker start rabbitmq
echo Starting Elasticsearch...
docker compose -f "%ROOT%\docker-compose-elasticsearch.yml" up -d
echo Starting Analytics Stack (Prometheus, Grafana, Metabase)...
docker compose -f "%ROOT%\docker-compose-analytics.yml" up -d
echo Waiting 5 seconds for infrastructure to be ready...
timeout /t 5 /nobreak >nul

:: -----------------------------------------------
:: STEP 1 - Eureka Server
:: -----------------------------------------------
echo [1/4] Starting Eureka Server on port %EUREKA_PORT%...
start "Eureka Server" cmd /k "cd /d %ROOT%\eureka-server\server && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%EUREKA_PORT%"

echo Waiting 20 seconds for Eureka to be ready...
timeout /t 20 /nobreak >nul

:: -----------------------------------------------
:: STEP 2 - Microservices (1 instance each)
:: -----------------------------------------------
echo [2/4] Starting Microservices...
echo.

echo Starting user-service on port %USER_PORT%...
start "user-service" cmd /k "cd /d %ROOT%\user-service\user-service && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%USER_PORT%"
timeout /t 3 /nobreak >nul

echo Starting book-service on port %BOOK_PORT%...
start "book-service" cmd /k "cd /d %ROOT%\book-service\book-service && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%BOOK_PORT%"
timeout /t 3 /nobreak >nul

echo Starting review-service on port %REVIEW_PORT%...
start "review-service" cmd /k "cd /d %ROOT%\review-service\review-service && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%REVIEW_PORT%"
timeout /t 3 /nobreak >nul

echo Starting cart-service on port %CART_PORT%...
start "cart-service" cmd /k "cd /d %ROOT%\cart-service\cart-service && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%CART_PORT%"
timeout /t 3 /nobreak >nul

echo Starting order-service on port %ORDER_PORT%...
start "order-service" cmd /k "cd /d %ROOT%\order-service\order-service && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%ORDER_PORT%"
timeout /t 3 /nobreak >nul

echo Starting payment-service on port %PAYMENT_PORT%...
start "payment-service" cmd /k "cd /d %ROOT%\payment-service\payment-service && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%PAYMENT_PORT%"
timeout /t 3 /nobreak >nul

echo Starting email-service on port %EMAIL_PORT%...
start "email-service" cmd /k "cd /d %ROOT%\email-service\email-service && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%EMAIL_PORT%"
timeout /t 3 /nobreak >nul

echo Starting search-service on port %SEARCH_PORT%...
start "search-service" cmd /k "cd /d %ROOT%\search-service && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%SEARCH_PORT%"
timeout /t 3 /nobreak >nul

:: -----------------------------------------------
:: STEP 3 - API Gateway (starts last)
:: -----------------------------------------------
echo.
echo Waiting 30 seconds for services to register with Eureka...
timeout /t 30 /nobreak >nul

echo [3/4] Starting API Gateway on port %GATEWAY_PORT%...
start "API Gateway" cmd /k "cd /d %ROOT%\api-gateway\gateway && mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%GATEWAY_PORT%"

:: -----------------------------------------------
:: STEP 4 - Bookstore Frontend (React + Vite)
:: -----------------------------------------------
echo.
echo Waiting 10 seconds for API Gateway to be ready...
timeout /t 10 /nobreak >nul

echo [4/5] Starting Bookstore Frontend on port %FRONTEND_PORT%...
start "Bookstore Frontend" cmd /k "cd /d %ROOT%\bookstore-frontend && npm run dev -- --port %FRONTEND_PORT% --strictPort"

:: -----------------------------------------------
:: STEP 5 - Architecture Explorer
:: -----------------------------------------------
echo.
echo [5/5] Starting Architecture Explorer on port %ARCH_EXPLORER_PORT%...
start "Architecture Explorer" cmd /k "cd /d %ROOT%\architecture-explorer && npm run dev"

:: -----------------------------------------------
:: DONE
:: -----------------------------------------------
echo.
echo ================================================
echo   All services started!
echo ================================================
echo.
echo   Eureka Dashboard : http://localhost:%EUREKA_PORT%
echo   API Gateway      : http://localhost:%GATEWAY_PORT%
echo   Bookstore UI     : http://localhost:%FRONTEND_PORT%
echo   Arch Explorer    : http://localhost:%ARCH_EXPLORER_PORT%
echo   Prometheus       : http://localhost:9090
echo   Grafana          : http://localhost:3000
echo   Metabase         : http://localhost:3002
echo   Loki             : http://localhost:3100
echo.
echo   user-service     : %USER_PORT%
echo   book-service     : %BOOK_PORT%
echo   review-service   : %REVIEW_PORT%
echo   cart-service     : %CART_PORT%
echo   order-service    : %ORDER_PORT%
echo   payment-service  : %PAYMENT_PORT%
echo   email-service    : %EMAIL_PORT%
echo   search-service   : %SEARCH_PORT%
echo.
echo   Each service is running in its own terminal window.
echo   Press any key to exit this window.
pause >nul
