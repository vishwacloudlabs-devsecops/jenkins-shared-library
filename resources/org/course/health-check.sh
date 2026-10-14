#!/bin/sh
# Usage: health-check.sh <url> [retries]
URL=$1
RETRIES=${2:-20}
i=1
while [ "$i" -le "$RETRIES" ]; do
  if curl -fsS "$URL"; then
    echo
    echo "Healthy after $i attempt(s): $URL"
    exit 0
  fi
  echo "Attempt $i/$RETRIES: not healthy yet"
  i=$((i + 1))
  sleep 3
done
echo "Health check FAILED: $URL"
exit 1
