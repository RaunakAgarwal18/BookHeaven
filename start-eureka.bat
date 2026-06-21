@echo off
title Start Eureka Server
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set EUREKA_PORT=8761

echo Starting Eureka Server on port %EUREKA_PORT%...
cd /d %ROOT%\eureka-server\server
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%EUREKA_PORT%
pause
