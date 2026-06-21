@echo off
title Start Frontend
color 0A

set ROOT=C:\Users\rauna\Desktop\BookHeaven
set FRONTEND_PORT=5173

echo Starting Bookstore Frontend on port %FRONTEND_PORT%...
cd /d %ROOT%\bookstore-frontend
npm run dev
pause
