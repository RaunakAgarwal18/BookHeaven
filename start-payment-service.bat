@echo off
title Start payment-service
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set PAYMENT_PORT=8086

echo Starting payment-service on port %PAYMENT_PORT%...
cd /d %ROOT%\payment-service\payment-service
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%PAYMENT_PORT%
pause
