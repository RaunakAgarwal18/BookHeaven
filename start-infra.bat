@echo off
title Start Infrastructure (Docker)
color 0A

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
docker-compose -f docker-compose-elasticsearch.yml up -d
echo Infrastructure ready!
pause
