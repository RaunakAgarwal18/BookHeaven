@echo off
title Start email-service
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set EMAIL_PORT=8087

echo Starting email-service on port %EMAIL_PORT%...
cd /d %ROOT%\email-service\email-service
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%EMAIL_PORT%
pause
