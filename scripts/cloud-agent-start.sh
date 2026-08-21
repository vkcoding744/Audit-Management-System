#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

sudo service mysql start

for _ in $(seq 1 30); do
  if sudo mysqladmin ping -h 127.0.0.1 --silent 2>/dev/null; then
    echo "MySQL is ready"
    exit 0
  fi
  sleep 1
done

echo "MySQL failed to become ready within 30 seconds" >&2
exit 1
