#!/bin/bash

# Ensure PostgreSQL is running
echo "Starting PostgreSQL..."
sudo /etc/init.d/postgresql start

# Setup database if it doesn't exist
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = 'uetracker'" | grep -q 1 || sudo -u postgres psql -c "CREATE DATABASE uetracker;"
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"

# Function to handle shutdown gracefully
cleanup() {
    echo "Stopping applications..."
    kill $BACKEND_PID
    kill $FRONTEND_PID
    exit 0
}

# Trap SIGINT (Ctrl+C) to run cleanup
trap cleanup SIGINT

# Start Backend
echo "Starting Spring Boot Backend..."
cd backend
./gradlew bootRun &
BACKEND_PID=$!
cd ..

# Start Frontend
echo "Starting React Frontend..."
cd frontend
npm start &
FRONTEND_PID=$!
cd ..

echo "Both applications are running. Press Ctrl+C to stop."

# Wait indefinitely until interrupted
wait
