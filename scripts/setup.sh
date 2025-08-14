#!/bin/bash

echo "======================================"
echo "Certificate Lifecycle Management System"
echo "Environment Setup Script"
echo "======================================"

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo ""
echo "Checking prerequisites..."
echo "-------------------------------------"

command -v node >/dev/null 2>&1 || { echo "Node.js is required but not installed. Please install Node.js 16+"; exit 1; }
command -v npm >/dev/null 2>&1 || { echo "npm is required but not installed."; exit 1; }
command -v java >/dev/null 2>&1 || { echo "Java is required but not installed. Please install JDK 8"; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "Maven is required but not installed. Please install Maven 3.8+"; exit 1; }

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 16 ]; then
    echo "Node.js version 16+ is required. Current version: $(node -v)"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
if [ "$JAVA_VERSION" != "1.8" ]; then
    echo "Warning: Java 8 is recommended. Current version: $JAVA_VERSION"
fi

echo "Prerequisites check passed!"

echo ""
echo "Step 1: Installing root dependencies..."
echo "-------------------------------------"
npm install

echo ""
echo "Step 2: Installing frontend dependencies..."
echo "-------------------------------------"
cd frontend
npm install

echo ""
echo "Step 3: Installing backend dependencies..."
echo "-------------------------------------"
cd ../backend
mvn dependency:resolve

echo ""
echo "Step 4: Creating necessary directories..."
echo "-------------------------------------"
cd "$PROJECT_ROOT"
mkdir -p logs
mkdir -p frontend/dist
mkdir -p backend/target

echo ""
echo "Step 5: Database setup reminder..."
echo "-------------------------------------"
echo "Please ensure MySQL 8.0 is installed and running."
echo "Create database with: CREATE DATABASE cc_bmad_opus_certificate_management;"
echo "Default connection: localhost:3306 with user:root password:root"

echo ""
echo "======================================"
echo "Setup completed successfully!"
echo "======================================"
echo ""
echo "To start development servers, run:"
echo "  npm run dev"
echo ""
echo "To build the project, run:"
echo "  npm run build"
echo ""