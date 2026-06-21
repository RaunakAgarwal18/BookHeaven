@echo off
title Start order-service
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set ORDER_PORT=8085

echo Starting order-service on port %ORDER_PORT%...
cd /d %ROOT%\order-service\order-service
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%ORDER_PORT%
pause
