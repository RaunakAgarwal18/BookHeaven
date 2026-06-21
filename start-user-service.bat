@echo off
title Start user-service
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set USER_PORT=8081

echo Starting user-service on port %USER_PORT%...
cd /d %ROOT%\user-service\user-service
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%USER_PORT%
pause
