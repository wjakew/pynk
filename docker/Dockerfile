# Dockerfile
FROM mongo:7.0

# Install Java Runtime and networking tools
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk-headless \
    curl \
    wget \
    ca-certificates \
    iputils-ping \
    traceroute \
    dnsutils \
    net-tools \
    iproute2 \
    telnet \
    nmap \
    tcpdump \
    && rm -rf /var/lib/apt/lists/*

# Update CA certificates for HTTPS connections
RUN update-ca-certificates

# Create app directory
RUN mkdir -p /app

# Set working directory
WORKDIR /app

# Copy your files
COPY pynk_service.jar /app/
COPY pynk_web.jar /app/
COPY pynk.properties /app/

# Create Java security policy for internet access
RUN echo 'grant {' > /app/all.policy \
    && echo '  permission java.security.AllPermission;' >> /app/all.policy \
    && echo '};' >> /app/all.policy

# Create startup script
RUN echo '#!/bin/bash' > /app/startup.sh \
    && echo 'set -e' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Check if services are already running (prevent restart loop)' >> /app/startup.sh \
    && echo 'if pgrep -f "pynk_service.jar" > /dev/null; then' >> /app/startup.sh \
    && echo '  echo "PYNK services already running, monitoring..."' >> /app/startup.sh \
    && echo '  while true; do' >> /app/startup.sh \
    && echo '    sleep 30' >> /app/startup.sh \
    && echo '    if ! pgrep -f "pynk_service.jar" > /dev/null || ! pgrep -f "pynk_web.jar" > /dev/null; then' >> /app/startup.sh \
    && echo '      echo "One of the PYNK services died, exiting..."' >> /app/startup.sh \
    && echo '      exit 1' >> /app/startup.sh \
    && echo '    fi' >> /app/startup.sh \
    && echo '  done' >> /app/startup.sh \
    && echo 'fi' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Start MongoDB in background if not running' >> /app/startup.sh \
    && echo 'if ! pgrep mongod > /dev/null; then' >> /app/startup.sh \
    && echo '  echo "Starting MongoDB..."' >> /app/startup.sh \
    && echo '  mongod --bind_ip_all --fork --logpath /var/log/mongodb.log' >> /app/startup.sh \
    && echo 'else' >> /app/startup.sh \
    && echo '  echo "MongoDB already running"' >> /app/startup.sh \
    && echo 'fi' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Wait for MongoDB to be fully operational' >> /app/startup.sh \
    && echo 'echo "Waiting for MongoDB to be fully operational..."' >> /app/startup.sh \
    && echo 'until mongosh --eval "db.runCommand({ping: 1})" --quiet > /dev/null 2>&1; do' >> /app/startup.sh \
    && echo '  sleep 2' >> /app/startup.sh \
    && echo '  echo "Still waiting for MongoDB..."' >> /app/startup.sh \
    && echo 'done' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Additional check - ensure MongoDB can create collections' >> /app/startup.sh \
    && echo 'echo "Testing MongoDB write operations..."' >> /app/startup.sh \
    && echo 'mongosh --eval "db.healthcheck.insertOne({timestamp: new Date(), status: \"ready\"})" --quiet > /dev/null 2>&1 || {' >> /app/startup.sh \
    && echo '  echo "MongoDB not ready for operations, waiting..."' >> /app/startup.sh \
    && echo '  sleep 5' >> /app/startup.sh \
    && echo '}' >> /app/startup.sh \
    && echo 'echo "MongoDB is fully operational and ready!"' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Test network connectivity tools' >> /app/startup.sh \
    && echo 'echo "Testing network tools availability..."' >> /app/startup.sh \
    && echo 'echo "Ping test: $(ping -c 1 8.8.8.8 >/dev/null 2>&1 && echo "OK" || echo "FAILED")"' >> /app/startup.sh \
    && echo 'echo "DNS test: $(dig google.com >/dev/null 2>&1 && echo "OK" || echo "FAILED")"' >> /app/startup.sh \
    && echo 'echo "Traceroute available: $(which traceroute >/dev/null 2>&1 && echo "OK" || echo "FAILED")"' >> /app/startup.sh \
    && echo 'echo "Network tools ready!"' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Start PYNK service (ping service and analytics) in background' >> /app/startup.sh \
    && echo 'echo "Starting PYNK service (ping service and analytics)..."' >> /app/startup.sh \
    && echo 'java -Djava.net.useSystemProxies=true -Djava.security.policy=all.policy -jar /app/pynk_service.jar > /var/log/pynk_service.log 2>&1 &' >> /app/startup.sh \
    && echo 'SERVICE_PID=$!' >> /app/startup.sh \
    && echo 'echo "PYNK service started with PID: $SERVICE_PID"' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Wait for service to initialize and become ready' >> /app/startup.sh \
    && echo 'echo "Waiting 15 seconds for PYNK service to initialize..."' >> /app/startup.sh \
    && echo 'sleep 15' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Start PYNK web application' >> /app/startup.sh \
    && echo 'echo "Starting PYNK web application..."' >> /app/startup.sh \
    && echo 'java -Djava.net.useSystemProxies=true -Djava.security.policy=all.policy -jar /app/pynk_web.jar > /var/log/pynk_web.log 2>&1 &' >> /app/startup.sh \
    && echo 'WEB_PID=$!' >> /app/startup.sh \
    && echo 'echo "PYNK web started with PID: $WEB_PID"' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Function to handle shutdown' >> /app/startup.sh \
    && echo 'cleanup() {' >> /app/startup.sh \
    && echo '  echo "Shutting down PYNK applications..."' >> /app/startup.sh \
    && echo '  kill $SERVICE_PID $WEB_PID 2>/dev/null || true' >> /app/startup.sh \
    && echo '  mongod --shutdown' >> /app/startup.sh \
    && echo '  exit 0' >> /app/startup.sh \
    && echo '}' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Set up signal handlers' >> /app/startup.sh \
    && echo 'trap cleanup SIGTERM SIGINT' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Keep container running and monitor processes' >> /app/startup.sh \
    && echo 'echo "All PYNK applications started successfully!"' >> /app/startup.sh \
    && echo 'echo "MongoDB PID: $(pgrep mongod)"' >> /app/startup.sh \
    && echo 'echo "PYNK Service PID: $SERVICE_PID"' >> /app/startup.sh \
    && echo 'echo "PYNK Web PID: $WEB_PID"' >> /app/startup.sh \
    && echo 'echo "PYNK container is ready and running..."' >> /app/startup.sh \
    && echo '' >> /app/startup.sh \
    && echo '# Monitor processes indefinitely' >> /app/startup.sh \
    && echo 'while true; do' >> /app/startup.sh \
    && echo '  if ! kill -0 $SERVICE_PID 2>/dev/null || ! kill -0 $WEB_PID 2>/dev/null; then' >> /app/startup.sh \
    && echo '    echo "One of the PYNK services has stopped, exiting..."' >> /app/startup.sh \
    && echo '    cleanup' >> /app/startup.sh \
    && echo '  fi' >> /app/startup.sh \
    && echo '  sleep 30' >> /app/startup.sh \
    && echo 'done' >> /app/startup.sh \
    && chmod +x /app/startup.sh

# Expose port 8080
EXPOSE 8080

# Use custom startup script
CMD ["/app/startup.sh"]
