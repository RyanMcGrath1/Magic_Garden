@echo off

set "CHROME=%ProgramFiles%\Google\Chrome\Application\chrome.exe"

if not exist "%CHROME%" set "CHROME=%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe"

if not exist "%CHROME%" (

  echo Chrome not found. Install Google Chrome or edit this script.

  exit /b 1

)

rem Dedicated profile so a new Chrome process is used; otherwise an already-running Chrome ignores --remote-debugging-port.

set "USER_DATA_DIR=%~dp0.chrome-debug-profile"

echo Using user-data-dir: %USER_DATA_DIR%

echo.

echo Open this game URL in the Chrome window that opens next:

echo   https://magicgarden.gg/r/QP9B

echo Then verify DevTools JSON: http://127.0.0.1:9222/json

echo.

rem Chrome 111+: WebSocket to CDP returns 403 without an allowed origin (java client needs this).

start "" "%CHROME%" --remote-debugging-port=9222 --remote-debugging-address=127.0.0.1 --remote-allow-origins=* --user-data-dir="%USER_DATA_DIR%"

