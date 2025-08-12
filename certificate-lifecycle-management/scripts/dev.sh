#!/bin/bash

# Certificate Lifecycle Management System - Development Script

set -e

echo "========================================="
echo "Certificate Management System Dev Server"
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

# Function to cleanup background processes
cleanup() {
    echo -e "\n${YELLOW}Stopping all services...${NC}"
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null || true
    fi
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    exit 0
}

# Set trap to cleanup on exit
trap cleanup EXIT INT TERM

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven not found. Please install Maven first.${NC}"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo -e "${RED}npm not found. Please install Node.js first.${NC}"
    exit 1
fi

# Start backend
echo -e "\n${GREEN}Starting Backend Service...${NC}"
cd "$PROJECT_ROOT/backend"
mvn spring-boot:run -Dspring-boot.run.profiles=dev &
BACKEND_PID=$!
echo -e "${GREEN}Backend started with PID: $BACKEND_PID${NC}"

# Wait for backend to start
echo -e "${YELLOW}Waiting for backend to be ready...${NC}"
sleep 10

# Start frontend
echo -e "\n${GREEN}Starting Frontend Service...${NC}"
cd "$PROJECT_ROOT/frontend"
npm install
npm run dev &
FRONTEND_PID=$!
echo -e "${GREEN}Frontend started with PID: $FRONTEND_PID${NC}"

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}Development servers are running!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo -e "\nAccess points:"
echo -e "  Frontend: ${YELLOW}http://localhost:3000${NC}"
echo -e "  Backend API: ${YELLOW}http://localhost:8080/api${NC}"
echo -e "\n${YELLOW}Press Ctrl+C to stop all services${NC}"

# Wait for background processes
wait