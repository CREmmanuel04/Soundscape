#!/bin/bash
# Railway deployment script

# This file tells Railway how to build and run your application
# Railway automatically detects Spring Boot apps, but this provides explicit instructions

# Build the application
./mvnw clean package -DskipTests

# Run the application
java -jar target/soundscape-0.0.1-SNAPSHOT.jar