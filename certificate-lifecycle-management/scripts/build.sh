#!/bin/bash

# Certificate Lifecycle Management System - Build Script

set -e

echo "========================================="
echo "Certificate Management System Build"
echo "========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

echo -e "${YELLOW}Project root: $PROJECT_ROOT${NC}"

# Build backend
echo -e "\n${GREEN}Building Backend...${NC}"
cd "$PROJECT_ROOT/backend"
if command -v mvn &> /dev/null; then
    mvn clean package -DskipTests
    echo -e "${GREEN}✓ Backend build completed${NC}"
else
    echo -e "${RED}Maven not found. Please install Maven to build the backend.${NC}"
    exit 1
fi

# Build frontend
echo -e "\n${GREEN}Building Frontend...${NC}"
cd "$PROJECT_ROOT/frontend"
if command -v npm &> /dev/null; then
    npm install
    npm run build
    echo -e "${GREEN}✓ Frontend build completed${NC}"
else
    echo -e "${RED}npm not found. Please install Node.js to build the frontend.${NC}"
    exit 1
fi

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}Build completed successfully!${NC}"
echo -e "${GREEN}=========================================${NC}"

# Output build artifacts location
echo -e "\nBuild artifacts:"
echo -e "  Backend JAR: ${YELLOW}$PROJECT_ROOT/backend/target/*.jar${NC}"
echo -e "  Frontend dist: ${YELLOW}$PROJECT_ROOT/frontend/dist/${NC}"