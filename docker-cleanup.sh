#!/bin/bash

# Utility script for Docker cleanup

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

echo "Docker Cleanup Utility"
echo "====================="

# Show current disk usage
echo ""
print_status "Current Docker disk usage:"
docker system df
echo ""

# Clean up stopped containers
print_status "Removing stopped containers..."
docker container prune -f

# Clean up unused images
print_status "Removing unused images..."
docker image prune -a -f

# Clean up unused volumes
print_status "Removing unused volumes..."
docker volume prune -f

# Clean up unused networks
print_status "Removing unused networks..."
docker network prune -f

# Show disk usage after cleanup
echo ""
print_status "Docker disk usage after cleanup:"
docker system df
echo ""

print_status "Cleanup complete!"