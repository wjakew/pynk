# PYNK Docker Container Deployment

## Overview

The PYNK application is containerized using Docker and includes both the ping service (analytics) and web interface components, along with an integrated MongoDB database. The container provides full network diagnostic capabilities including ping, traceroute, and DNS resolution tools.

## Architecture

The Docker container runs three main components:
- **MongoDB 7.0** - Database service for storing ping data and analytics
- **PYNK Service** (`pynk_service.jar`) - Background service handling ping operations and analytics
- **PYNK Web** (`pynk_web.jar`) - Web interface accessible on port 8080

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- Minimum 2GB RAM
- Minimum 1GB disk space

## File Structure

Ensure your project directory contains the following files:

```
pynk-deployment/
├── Dockerfile
├── docker-compose.yml
├── pynk_service.jar
├── pynk_web.jar
└── pynk.properties
```

## Container Features

### Network Capabilities
- **Full internet access** for external ping operations
- **DNS resolution** using Google DNS (8.8.8.8, 8.8.4.4)
- **Network diagnostic tools**: ping, traceroute, dig, nslookup
- **Advanced networking**: netstat, ss, ip, telnet, nmap, tcpdump

### Security & Permissions
- **Java 17 JDK** with full networking permissions
- **Enhanced capabilities**: NET_ADMIN, NET_RAW, SYS_ADMIN
- **Custom security policy** allowing network operations

### Data Persistence
- **MongoDB data** persisted in Docker volume `mongodb_data`
- **Application logs** stored in Docker volume `app_logs`
- **Automatic restart** unless manually stopped

## Deployment Instructions

### Option 1: Docker Compose (Recommended)

1. **Navigate to project directory:**
   ```bash
   cd pynk-deployment/
   ```

2. **Build and start the container:**
   ```bash
   docker-compose up --build -d
   ```

3. **Verify deployment:**
   ```bash
   docker-compose ps
   docker-compose logs -f pynk-app
   ```

### Option 2: Direct Docker Commands

1. **Build the image:**
   ```bash
   docker build -t pynk-app .
   ```

2. **Run the container:**
   ```bash
   docker run -d \
     --name pynk-container \
     -p 8080:8080 \
     -p 27017:27017 \
     --cap-add=NET_ADMIN \
     --cap-add=NET_RAW \
     --cap-add=SYS_ADMIN \
     --dns=8.8.8.8 \
     --dns=8.8.4.4 \
     -v pynk_mongodb_data:/data/db \
     -v pynk_app_logs:/var/log \
     pynk-app
   ```

## Service Access

### Web Interface
- **URL**: `http://localhost:8080`
- **Purpose**: PYNK web application interface

### MongoDB Database
- **Host**: `localhost:27017` (if external access needed)
- **Database**: `pynk`
- **Purpose**: Internal database access for development/debugging

## Monitoring & Management

### View Application Logs
```bash
# All container logs
docker-compose logs -f pynk-app

# Service-specific logs
docker exec pynk-container tail -f /var/log/pynk_service.log
docker exec pynk-container tail -f /var/log/pynk_web.log
docker exec pynk-container tail -f /var/log/mongodb.log
```

### Health Checks
```bash
# Container health status
docker-compose ps

# MongoDB health
docker exec pynk-container mongosh --eval "db.runCommand({ping: 1})"

# Network connectivity test
docker exec pynk-container ping -c 3 google.com
```

### Container Management
```bash
# Stop the application
docker-compose down

# Restart the application
docker-compose restart

# Update and rebuild
docker-compose down
docker-compose up --build -d

# View resource usage
docker stats pynk-container
```

## Startup Sequence

The container follows this startup sequence:

1. **MongoDB Initialization** - Database starts and waits for full readiness
2. **Network Tools Verification** - Tests ping, DNS, and traceroute functionality
3. **PYNK Service Start** - Background ping service and analytics engine (15s initialization)
4. **PYNK Web Start** - Web interface becomes available
5. **Continuous Monitoring** - Health checks every 30 seconds

Expected startup logs:
```
Starting MongoDB...
MongoDB is fully operational and ready!
Testing network tools availability...
Ping test: OK
DNS test: OK
Traceroute available: OK
Network tools ready!
Starting PYNK service (ping service and analytics)...
PYNK service started with PID: XXX
Waiting 15 seconds for PYNK service to initialize...
Starting PYNK web application...
PYNK web started with PID: XXX
All PYNK applications started successfully!
PYNK container is ready and running...
```

## Data Persistence

### MongoDB Data
- **Volume**: `mongodb_data`
- **Mount Point**: `/data/db`
- **Persistence**: Data survives container restarts and updates

### Application Logs
- **Volume**: `app_logs`
- **Mount Point**: `/var/log`
- **Files**: `mongodb.log`, `pynk_service.log`, `pynk_web.log`

### Backup Recommendations
```bash
# Backup MongoDB data
docker run --rm -v pynk_mongodb_data:/data -v $(pwd):/backup ubuntu tar czf /backup/mongodb-backup.tar.gz /data

# Backup application logs
docker run --rm -v pynk_app_logs:/logs -v $(pwd):/backup ubuntu tar czf /backup/logs-backup.tar.gz /logs
```

## Network Configuration

### Port Mapping
- **8080:8080** - PYNK web interface
- **27017:27017** - MongoDB (optional, for external access)

### DNS Configuration
- **Primary DNS**: 8.8.8.8 (Google)
- **Secondary DNS**: 8.8.4.4 (Google)

### Network Capabilities
- **NET_ADMIN** - Network administration
- **NET_RAW** - Raw socket access (required for ping)
- **SYS_ADMIN** - System administration (required for advanced network ops)

## Troubleshooting

### Container Won't Start
```bash
# Check container logs
docker-compose logs pynk-app

# Check system resources
docker system df
docker system prune # Clean up if needed
```

### Network Issues
```bash
# Test container networking
docker exec pynk-container ping -c 3 8.8.8.8
docker exec pynk-container dig google.com
docker exec pynk-container traceroute google.com

# Check DNS resolution
docker exec pynk-container nslookup google.com
```

### Application Issues
```bash
# Check service status
docker exec pynk-container ps aux | grep java
docker exec pynk-container ps aux | grep mongod

# Check application logs
docker exec pynk-container tail -100 /var/log/pynk_service.log
docker exec pynk-container tail -100 /var/log/pynk_web.log
```

### Performance Issues
```bash
# Monitor resource usage
docker stats pynk-container

# Check MongoDB performance
docker exec pynk-container mongosh --eval "db.stats()"
```

## Environment Variables

The container supports the following environment variables:

- **MONGO_INITDB_DATABASE**: `pynk` (MongoDB initial database name)

Additional environment variables can be added to the docker-compose.yml file as needed.

## Security Considerations

- The container runs with elevated network privileges for ping operations
- MongoDB is accessible internally to the application
- External MongoDB access (port 27017) is optional and can be disabled
- All network traffic is logged for monitoring
- Container uses official MongoDB base image with security updates

## Updates and Maintenance

### Updating Application
1. Replace JAR files in the project directory
2. Rebuild and restart:
   ```bash
   docker-compose down
   docker-compose up --build -d
   ```

### Updating Base Image
1. Update the MongoDB version in Dockerfile if needed
2. Rebuild:
   ```bash
   docker-compose build --no-cache
   docker-compose up -d
   ```

## Support

For deployment issues:
1. Check container logs: `docker-compose logs -f pynk-app`
2. Verify network connectivity from container
3. Ensure all required files are present in the deployment directory
4. Confirm Docker and Docker Compose versions meet requirements
