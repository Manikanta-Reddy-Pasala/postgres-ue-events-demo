#!/bin/bash

# Create docker-compose.yml
cat << 'EOF' > docker-compose.yml
version: '3.8'

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

echo "Generated docker-compose.yml"
echo "Starting application with Docker Compose..."
docker-compose up --build -d

echo "Application is starting!"
echo "Backend API will be available at http://localhost:8080"
echo "Frontend UI will be available at http://localhost:3000"
echo "To view logs, run: docker-compose logs -f"
echo "To stop, run: docker-compose down"
