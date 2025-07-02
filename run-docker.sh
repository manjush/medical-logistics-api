#!/bin/bash

# Script to build and run Medical Logistics API Docker container

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="medical-logistics-api"
IMAGE_NAME="medical-logistics-api"
CONTAINER_NAME="medical-logistics-container"
PORT=8080
JAR_NAME="medical-logistics-api-1.0.0.jar"

# Parse command line arguments
SKIP_BUILD=false
FORCE_CLEAN=false
FOLLOW_LOGS=true

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --force-clean)
            FORCE_CLEAN=true
            shift
            ;;
        --no-logs)
            FOLLOW_LOGS=false
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --skip-build    Skip Maven build (use existing JAR)"
            echo "  --force-clean   Remove all project images and containers"
            echo "  --no-logs       Don't show logs after starting"
            echo "  --help          Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Force clean if requested
if [ "$FORCE_CLEAN" = true ]; then
    print_warning "Force cleaning all project containers and images..."

    # Stop and remove all containers with our image
    docker ps -a --filter "ancestor=$IMAGE_NAME" -q | xargs -r docker stop
    docker ps -a --filter "ancestor=$IMAGE_NAME" -q | xargs -r docker rm

    # Remove all project images
    docker images "$IMAGE_NAME" -q | xargs -r docker rmi -f

    print_status "Cleanup complete. Exiting."
    exit 0
fi

# Stop and remove existing container if it exists
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    print_warning "Stopping and removing existing container..."
    docker stop $CONTAINER_NAME >/dev/null 2>&1
    docker rm $CONTAINER_NAME >/dev/null 2>&1
fi

# Build the application (unless skipped)
if [ "$SKIP_BUILD" = false ]; then
    print_status "Building Maven application..."
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        print_error "Maven build failed!"
        exit 1
    fi
else
    print_info "Skipping Maven build (--skip-build flag used)"
fi

# Check if JAR file exists
if [ ! -f "target/$JAR_NAME" ]; then
    print_error "JAR file not found: target/$JAR_NAME"
    print_info "Run without --skip-build flag to build the application"
    exit 1
fi

# Remove existing image to ensure fresh build
print_status "Removing existing Docker image to ensure fresh build..."
docker rmi -f $IMAGE_NAME >/dev/null 2>&1

# Build Docker image
print_status "Building new Docker image..."
docker build -t $IMAGE_NAME . --no-cache
if [ $? -ne 0 ]; then
    print_error "Docker build failed!"
    exit 1
fi

# Clean up dangling images automatically
print_info "Cleaning up dangling images..."
docker image prune -f >/dev/null 2>&1

# Run Docker container
print_status "Starting Docker container..."
docker run -d \
    --name $CONTAINER_NAME \
    -p $PORT:8080 \
    -e SPRING_PROFILES_ACTIVE=dev \
    --restart unless-stopped \
    $IMAGE_NAME

if [ $? -ne 0 ]; then
    print_error "Failed to start container!"
    exit 1
fi

# Show initial logs while waiting for startup
print_status "Container started. Showing startup logs..."
echo -e "${YELLOW}════════════════════ STARTUP LOGS ════════════════════${NC}"

# Start showing logs in background
docker logs -f $CONTAINER_NAME 2>&1 &
LOGS_PID=$!

# Wait for application to start
print_status "Waiting for application health check..."
MAX_ATTEMPTS=30
ATTEMPT=0
HEALTH_CHECK_PASSED=false

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s -f http://localhost:$PORT/actuator/health >/dev/null 2>&1; then
        HEALTH_CHECK_PASSED=true
        break
    fi
    ATTEMPT=$((ATTEMPT + 1))
    sleep 1
done

# Stop the background logs
sleep 2  # Give a moment to see "Started" message
kill $LOGS_PID 2>/dev/null
wait $LOGS_PID 2>/dev/null

echo -e "${YELLOW}══════════════════════════════════════════════════════${NC}"

# Check if health check passed
if [ "$HEALTH_CHECK_PASSED" = false ]; then
    print_error "Application failed to start within 30 seconds"
    print_info "Showing recent logs:"
    docker logs --tail 50 $CONTAINER_NAME
    exit 1
fi

# Display status
print_status "Application started successfully!"
echo ""
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Application URLs:${NC}"
echo -e "  Main:        http://localhost:$PORT"
echo -e "  Health:      http://localhost:$PORT/actuator/health"
echo -e "  API Docs:    http://localhost:$PORT/swagger-ui.html"
echo ""
echo -e "${GREEN}Useful commands:${NC}"
echo -e "  View logs:        docker logs -f $CONTAINER_NAME"
echo -e "  Tail logs:        docker logs -f --tail 100 $CONTAINER_NAME"
echo -e "  Stop container:   docker stop $CONTAINER_NAME"
echo -e "  Restart:          $0 --skip-build"
echo -e "  Full cleanup:     $0 --force-clean"
echo ""

# Show current Docker images for this project
print_info "Current project images:"
docker images | grep -E "REPOSITORY|$IMAGE_NAME" | head -5
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"

# Continue showing logs if requested
if [ "$FOLLOW_LOGS" = true ]; then
    echo ""
    print_info "Continuing to show application logs (Ctrl+C to stop)..."
    echo -e "${YELLOW}════════════════════ APPLICATION LOGS ════════════════════${NC}"
    docker logs -f --tail 0 $CONTAINER_NAME
else
    echo ""
    print_info "Container is running in background. Use 'docker logs -f $CONTAINER_NAME' to view logs."
fi