#!/bin/bash
# Single script to run the whole stack (postgres + backend + frontend).
#   ./run.sh         build + start everything, wait until ready
#   ./run.sh down    stop and remove containers + data volume
#   ./run.sh logs    tail logs
set -e

cd "$(dirname "$0")"

# docker compose v2 with fallback to v1
DC="docker compose"; command -v docker-compose >/dev/null 2>&1 && ! docker compose version >/dev/null 2>&1 && DC="docker-compose"

cat > docker-compose.yml <<'EOF'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: uetracker
    ports:
      - "5433:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: backend/Dockerfile
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/uetracker?reWriteBatchedInserts=true
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres

  frontend:
    build:
      context: .
      dockerfile: frontend/Dockerfile
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  postgres_data:
EOF

case "${1:-up}" in
  down) $DC down -v; echo "Stopped and removed."; exit 0 ;;
  logs) $DC logs -f; exit 0 ;;
esac

echo "Building and starting..."
$DC up --build -d

echo -n "Waiting for backend (http://localhost:8080) "
for i in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/projection/status 2>/dev/null || echo 000)
  [ "$code" = "200" ] && { echo " ready."; break; }
  echo -n "."; sleep 2
done

echo
echo "UI:      http://localhost:3000   (Generate / Clear, model + strategy toggles, query-time)"
echo "API:     http://localhost:8080/api"
echo "Logs:    ./run.sh logs"
echo "Stop:    ./run.sh down"
