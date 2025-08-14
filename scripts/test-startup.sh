#!/bin/bash

echo "======================================"
echo "Service Startup Validation Test"
echo "======================================"

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

FRONTEND_PID=""
BACKEND_PID=""

cleanup() {
    echo ""
    echo "Cleaning up processes..."
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null || true
    fi
}

trap cleanup EXIT

echo ""
echo "Test 1: Backend Spring Boot Application Startup"
echo "-------------------------------------"
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev &
BACKEND_PID=$!
echo "Waiting for backend to start (PID: $BACKEND_PID)..."
sleep 15

# Check if backend is running
if curl -f http://localhost:8080/api/actuator/health >/dev/null 2>&1; then
    echo "✓ Backend started successfully"
else
    echo "✗ Backend failed to start"
    exit 1
fi

echo ""
echo "Test 2: Frontend Vite Dev Server Startup"
echo "-------------------------------------"
cd ../frontend
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi
npm run dev &
FRONTEND_PID=$!
echo "Waiting for frontend to start (PID: $FRONTEND_PID)..."
sleep 10

# Check if frontend is running
if curl -f http://localhost:5173 >/dev/null 2>&1; then
    echo "✓ Frontend started successfully"
else
    echo "✗ Frontend failed to start"
    exit 1
fi

echo ""
echo "Test 3: Health Check Endpoint"
echo "-------------------------------------"
HEALTH_RESPONSE=$(curl -s http://localhost:8080/api/actuator/health)
if echo "$HEALTH_RESPONSE" | grep -q "UP"; then
    echo "✓ Health check passed: $HEALTH_RESPONSE"
else
    echo "✗ Health check failed"
    exit 1
fi

echo ""
echo "Test 4: Frontend Proxy to Backend"
echo "-------------------------------------"
# Test if frontend can proxy requests to backend
if curl -f http://localhost:5173/api/actuator/health >/dev/null 2>&1; then
    echo "✓ Frontend proxy to backend working"
else
    echo "✗ Frontend proxy to backend failed"
    exit 1
fi

echo ""
echo "======================================"
echo "All startup tests passed successfully!"
echo "======================================"
echo ""
echo "Services are running at:"
echo "  Frontend: http://localhost:5173"
echo "  Backend:  http://localhost:8080/api"
echo "  Health:   http://localhost:8080/api/actuator/health"
echo ""
echo "Press Ctrl+C to stop services..."
wait