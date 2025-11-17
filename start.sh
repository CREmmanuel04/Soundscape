#!/bin/sh
# Convert Render's DATABASE_URL format (postgres://) to Spring Boot format (jdbc:postgresql://)
if [ -n "$DATABASE_URL" ]; then
    # Convert postgres:// or postgresql:// to jdbc:postgresql://
    JDBC_URL=$(echo "$DATABASE_URL" | sed -E 's|^(postgres(ql)?://)|jdbc:postgresql://|')
    
    # Fix Render's internal hostname format (add .render.com if missing)
    # Render internal hostnames look like: dpg-xxxxx-a (missing domain)
    JDBC_URL=$(echo "$JDBC_URL" | sed -E 's|@(dpg-[a-z0-9]+-[a-z])(/)|@\1.oregon-postgres.render.com\2|')
    
    export SPRING_DATASOURCE_URL="$JDBC_URL"
    echo "Original DATABASE_URL: $DATABASE_URL"
    echo "Converted to JDBC URL: $JDBC_URL"
else
    echo "WARNING: DATABASE_URL not set!"
fi

# Start the application
exec java -Dspring.profiles.active=prod -Dserver.port="${PORT:-8080}" -jar app.jar
