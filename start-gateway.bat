@echo off
title Start API Gateway
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set GATEWAY_PORT=8090

echo Starting API Gateway on port %GATEWAY_PORT%...
cd /d %ROOT%\api-gateway\gateway
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%GATEWAY_PORT%
pause
