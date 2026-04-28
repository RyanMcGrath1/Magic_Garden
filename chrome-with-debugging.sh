#!/usr/bin/env bash
set -euo pipefail

CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

if [[ ! -x "$CHROME" ]]; then
  echo "Chrome not found at: $CHROME"
  echo "Install Google Chrome or edit this script."
  exit 1
fi

# Dedicated profile so a new Chrome process is used; otherwise an already-running Chrome ignores --remote-debugging-port.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
USER_DATA_DIR="${SCRIPT_DIR}/.chrome-debug-profile"

echo "Using user-data-dir: ${USER_DATA_DIR}"
echo
echo "Open this game URL in the Chrome window that opens next:"
echo "  https://magicgarden.gg/r/QP9B"
echo "Then verify DevTools JSON: http://127.0.0.1:9222/json"
echo

# Chrome 111+: WebSocket to CDP returns 403 without an allowed origin (java client needs this).
exec "$CHROME" \
  --remote-debugging-port=9222 \
  --remote-debugging-address=127.0.0.1 \
  --remote-allow-origins='*' \
  --user-data-dir="$USER_DATA_DIR"
