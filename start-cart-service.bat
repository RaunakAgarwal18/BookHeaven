@echo off
title Start cart-service
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set CART_PORT=8084

echo Starting cart-service on port %CART_PORT%...
cd /d %ROOT%\cart-service\cart-service
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%CART_PORT%
pause
