#!/bin/sh
# Convert Render's DATABASE_URL format (postgres://) to Spring Boot format (jdbc:postgresql://)
if [ -n "$DATABASE_URL" ]; then
    # Convert postgres:// or postgresql:// to jdbc:postgresql://
    JDBC_URL=$(echo "$DATABASE_URL" | sed -E 's|^(postgres(ql)?://)|jdbc:postgresql://|')
    export SPRING_DATASOURCE_URL="$JDBC_URL"
    echo "Original DATABASE_URL: $DATABASE_URL"
    echo "Converted to JDBC URL: $JDBC_URL"
else
    echo "WARNING: DATABASE_URL not set!"
fi

# Start the application
exec java -Dspring.profiles.active=prod -Dserver.port="${PORT:-8080}" -jar app.jar
