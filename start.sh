#!/bin/sh
# Convert Render's DATABASE_URL format (postgres://) to Spring Boot format (jdbc:postgresql://)
if [ -n "$DATABASE_URL" ]; then
    export JDBC_DATABASE_URL=$(echo "$DATABASE_URL" | sed 's/^postgres:/jdbc:postgresql:/')
    echo "Converted DATABASE_URL to JDBC format"
fi

# Start the application
exec java -Dspring.profiles.active=prod -Dserver.port="${PORT:-8080}" -jar app.jar
