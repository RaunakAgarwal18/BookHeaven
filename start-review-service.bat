@echo off
title Start review-service
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set REVIEW_PORT=8083

echo Starting review-service on port %REVIEW_PORT%...
cd /d %ROOT%\review-service\review-service
mvn -f pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=%REVIEW_PORT%
pause
