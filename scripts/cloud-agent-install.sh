#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f .env ]]; then
  cp .env.example .env
fi

./scripts/cloud-agent-mysql-setup.sh

cd frontend
npm ci
cd "$ROOT_DIR"

cd backend
./mvnw -q -DskipTests package
cd "$ROOT_DIR"
