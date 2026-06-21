@echo off
title Start search-service
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set SEARCH_PORT=8088

echo Starting search-service on port %SEARCH_PORT%...
cd /d %ROOT%\search-service
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%SEARCH_PORT%
pause
