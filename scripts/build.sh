#!/bin/bash

echo "======================================"
echo "Certificate Lifecycle Management System"
echo "Build Script"
echo "======================================"

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo ""
echo "Step 1: Building Frontend..."
echo "-------------------------------------"
cd frontend
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi
npm run build
echo "Frontend build completed!"

echo ""
echo "Step 2: Building Backend..."
echo "-------------------------------------"
cd ../backend
mvn clean package -DskipTests
echo "Backend build completed!"

echo ""
echo "======================================"
echo "Build completed successfully!"
echo "======================================"
echo ""
echo "Output locations:"
echo "  Frontend: $PROJECT_ROOT/frontend/dist"
echo "  Backend:  $PROJECT_ROOT/backend/target/certificate-lifecycle-management-backend-1.0.0.jar"
echo ""