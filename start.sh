#!/bin/sh
# Database connection info will be provided via individual environment variables
# by Render (DATABASE_HOST, DATABASE_NAME, DATABASE_USER, DATABASE_PASSWORD)

echo "Connecting to database:"
echo "  Host: ${DATABASE_HOST}"
echo "  Database: ${DATABASE_NAME}"
echo "  User: ${DATABASE_USER}"

# Start the application
exec java -Dspring.profiles.active=prod -Dserver.port="${PORT:-8080}" -jar app.jar
