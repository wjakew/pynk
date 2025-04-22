# Pynk Documentation

## Overview
Pynk is a service for generating network statistics. It's a Java-based application (version 1.1.1) that monitors hosts and collects network-related data through a job-based system.

## Key Features
- Multi-database support (SQLite and MongoDB)
- Automated host monitoring
- Concurrent job management
- Real-time host status tracking
- Automatic database reconnection (MongoDB)

## Architecture

### Database Support
The application supports two database types:
1. **SQLite**
   - Used for local storage
   - Configured through `databasePath` in properties file
2. **MongoDB**
   - Used for distributed storage
   - Configured through `databaseUrl` in properties file

### Core Components

#### 1. Host Management
- Hosts are the primary entities being monitored
- Each host has:
  - Unique identifier (ID)
  - Name
  - Status (active/inactive)
  - Job execution time interval

#### 2. Thread Management System
The application implements three main thread management systems:

##### a. Host Refresh Thread
- Periodically refreshes host data from the database
- Default refresh interval: 30 seconds
- Maintains an active hosts list in a thread-safe `ConcurrentHashMap`
- Updates host statuses and removes inactive hosts

##### b. Job Manager Thread (SQLite)
- Manages individual job threads for each active host
- Creates new job threads for newly active hosts
- Removes threads for inactive hosts
- Checks host status every 5 seconds

##### c. MongoDB Host Manager Thread
- Specific implementation for MongoDB database
- Manages host monitoring in MongoDB environment
- Includes automatic database reconnection every 6 hours
- Maintains separate job threads for each active host

### Job System
- Each host has its own dedicated job thread
- Jobs run at intervals specified by `hostJobTime`
- Two types of jobs:
  1. `Job` class for SQLite implementation
  2. `DocumentJob` class for MongoDB implementation

## Configuration

### Properties File
- Location: `./pynk.properties`
- Key configurations:
  - `databaseType`: "sqlite" or "mongodb"
  - `databasePath`: Path for SQLite database
  - `databaseUrl`: URL for MongoDB connection

### Initialization Process
1. Application checks for properties file
2. If not found, creates a new properties file template
3. Connects to specified database
4. Initializes appropriate thread management system
5. Begins host monitoring and job execution

## Logging
- Comprehensive logging system
- Logs include:
  - Thread management events
  - Host status changes
  - Job execution details
  - Error messages
- Color-coded log entries for better visibility

## Error Handling
- Robust error handling throughout the application
- Automatic recovery mechanisms
- Thread interruption handling
- Database connection error management
- Graceful degradation on failures

## Debug Mode
- Debug mode available (controlled by `debug` constant)
- When enabled in MongoDB mode, runs `PynkTest` instead of normal operation
- Useful for testing and development

## System Requirements
- Java Runtime Environment
- Access to either SQLite or MongoDB database
- Sufficient permissions to create and manage files
- Network access for MongoDB implementation

## Best Practices
1. Configure the properties file before first run
2. Monitor log output for system health
3. Ensure database connectivity before starting
4. Maintain proper host configurations
5. Regular monitoring of active hosts

## Technical Details
- Version: 1.1.1
- Build: pynk22042025REV01
- Thread-safe implementation using `ConcurrentHashMap`
- Daemon threads for background operations
- Automatic resource management

---
*This documentation provides a high-level overview of the Pynk application. For specific implementation details or customization options, please refer to the source code or contact the developer at kubawawak@gmail.com.* 