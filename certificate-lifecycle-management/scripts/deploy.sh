#!/bin/bash

# Certificate Lifecycle Management System - Deployment Script (Basic Version)

set -e

echo "========================================="
echo "Certificate Management System Deployment"
echo "========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Default values
DEPLOY_ENV=${1:-staging}
DEPLOY_DIR="/opt/certificate-management"

echo -e "${YELLOW}Deployment environment: $DEPLOY_ENV${NC}"
echo -e "${YELLOW}Project root: $PROJECT_ROOT${NC}"

# Validate environment
if [[ "$DEPLOY_ENV" != "staging" && "$DEPLOY_ENV" != "production" ]]; then
    echo -e "${RED}Invalid environment. Use 'staging' or 'production'${NC}"
    exit 1
fi

# Build the project first
echo -e "\n${GREEN}Building project...${NC}"
"$SCRIPT_DIR/build.sh"

# Create deployment package
echo -e "\n${GREEN}Creating deployment package...${NC}"
TEMP_DIR=$(mktemp -d)
PACKAGE_NAME="certificate-management-$(date +%Y%m%d-%H%M%S).tar.gz"

# Copy backend JAR
mkdir -p "$TEMP_DIR/backend"
cp "$PROJECT_ROOT/backend/target"/*.jar "$TEMP_DIR/backend/"

# Copy frontend dist
cp -r "$PROJECT_ROOT/frontend/dist" "$TEMP_DIR/frontend"

# Copy configuration files
mkdir -p "$TEMP_DIR/config"
cp "$PROJECT_ROOT/backend/src/main/resources/application-${DEPLOY_ENV}.yml" "$TEMP_DIR/config/"

# Create deployment script
cat > "$TEMP_DIR/start.sh" << 'EOF'
#!/bin/bash
java -jar backend/*.jar --spring.profiles.active=$1 &
echo $! > app.pid
echo "Application started with PID: $(cat app.pid)"
EOF

cat > "$TEMP_DIR/stop.sh" << 'EOF'
#!/bin/bash
if [ -f app.pid ]; then
    kill $(cat app.pid)
    rm app.pid
    echo "Application stopped"
else
    echo "No running application found"
fi
EOF

chmod +x "$TEMP_DIR/start.sh"
chmod +x "$TEMP_DIR/stop.sh"

# Create package
cd "$TEMP_DIR"
tar czf "$PROJECT_ROOT/$PACKAGE_NAME" .
cd "$PROJECT_ROOT"
rm -rf "$TEMP_DIR"

echo -e "${GREEN}✓ Deployment package created: $PACKAGE_NAME${NC}"

# Deployment instructions
echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}Deployment package ready!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo -e "\nNext steps for deployment:"
echo -e "1. Copy package to target server:"
echo -e "   ${YELLOW}scp $PACKAGE_NAME user@server:$DEPLOY_DIR/${NC}"
echo -e "2. Extract on target server:"
echo -e "   ${YELLOW}tar xzf $PACKAGE_NAME${NC}"
echo -e "3. Start the application:"
echo -e "   ${YELLOW}./start.sh $DEPLOY_ENV${NC}"
echo -e "4. Configure web server (nginx) to:"
echo -e "   - Serve frontend files from ${YELLOW}$DEPLOY_DIR/frontend${NC}"
echo -e "   - Proxy API requests to ${YELLOW}http://localhost:8080/api${NC}"