# Railway Deployment Guide for Soundscape

## Prerequisites
- GitHub repository (✅ You have this!)
- Railway account (free at railway.app)

## Step 1: Prepare Your Application for Production

### 1.1 Update application.properties for production
Create `src/main/resources/application-prod.properties`:

```properties
# Database Configuration
spring.datasource.url=${DATABASE_URL}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Spotify Configuration
spotify.client-id=${SPOTIFY_CLIENT_ID}
spotify.client-secret=${SPOTIFY_CLIENT_SECRET}
spotify.redirect-uri=${SPOTIFY_REDIRECT_URI}

# Server Configuration
server.port=${PORT:8080}
```

### 1.2 Update your main application.properties
```properties
# Default profile
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}

# Development Database (H2)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop

# Spotify Configuration (Development)
spotify.client-id=your-spotify-client-id
spotify.client-secret=your-spotify-client-secret
spotify.redirect-uri=http://localhost:8080/login/oauth2/code/spotify
```

## Step 2: Add PostgreSQL Support

### 2.1 Add PostgreSQL dependency to pom.xml
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Step 3: Deploy to Railway

### 3.1 Create Railway Account
1. Go to [railway.app](https://railway.app)
2. Sign up with GitHub

### 3.2 Deploy Your App
1. Click "New Project"
2. Select "Deploy from GitHub repo"
3. Choose your Soundscape repository
4. Railway automatically detects Spring Boot and starts deployment

### 3.3 Add Database
1. In your Railway project dashboard
2. Click "New" → "Database" → "PostgreSQL"
3. Railway automatically connects it to your app

### 3.4 Set Environment Variables
In Railway dashboard, go to your app → Variables:
```
SPRING_PROFILES_ACTIVE=prod
SPOTIFY_CLIENT_ID=your-actual-client-id
SPOTIFY_CLIENT_SECRET=your-actual-client-secret
SPOTIFY_REDIRECT_URI=https://your-app-name.railway.app/login/oauth2/code/spotify
```

## Step 4: Update Spotify App Settings
1. Go to [Spotify Developer Console](https://developer.spotify.com/dashboard)
2. Update your Spotify app's redirect URI to: `https://your-app-name.railway.app/login/oauth2/code/spotify`

## Step 5: Test Your Live Website
Your app will be available at: `https://your-app-name.railway.app`

## Costs
- Railway: Free tier includes $5/month credit (sufficient for small apps)
- Database: Included in Railway free tier
- Domain: Free .railway.app subdomain (custom domain costs extra)