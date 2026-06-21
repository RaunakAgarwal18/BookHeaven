@echo off
title Start book-service
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set BOOK_PORT=8082

echo Starting book-service on port %BOOK_PORT%...
cd /d %ROOT%\book-service\book-service
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%BOOK_PORT%
pause
