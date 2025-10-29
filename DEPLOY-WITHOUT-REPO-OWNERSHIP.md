# Deploy Soundscape Without Owning the Repo

## Option 1: Heroku (Recommended - Most Reliable)

### Step 1: Install Heroku CLI
1. Download from: https://devcenter.heroku.com/articles/heroku-cli
2. Install and restart your terminal

### Step 2: Deploy Your App
```bash
# Navigate to your project
cd "C:\Users\jjzal\OneDrive\Documents\SoundScapeVS\Soundscape"

# Login to Heroku
heroku login

# Create a new Heroku app
heroku create your-soundscape-app

# Add PostgreSQL database
heroku addons:create heroku-postgresql:essential-0

# Set environment variables
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set SPOTIFY_CLIENT_ID=ba12d0b2c6524b2e9cec5387458b45eb
heroku config:set SPOTIFY_CLIENT_SECRET=51aada2b46184bf79bde2ddb8dcde57c
heroku config:set SPOTIFY_REDIRECT_URI=https://your-soundscape-app.herokuapp.com/login/oauth2/code/spotify

# Deploy your app
git add .
git commit -m "Prepare for Heroku deployment"
heroku git:remote -a your-soundscape-app
git push heroku Joshua-Zallo:main
```

**Cost**: $7/month (app) + $9/month (database) = $16/month

---

## Option 2: Railway CLI (Free Option)

### Step 1: Install Railway CLI
```bash
# Install via npm (requires Node.js)
npm install -g @railway/cli

# Or download from: https://railway.app/cli
```

### Step 2: Deploy
```bash
# Navigate to your project
cd "C:\Users\jjzal\OneDrive\Documents\SoundScapeVS\Soundscape"

# Login to Railway
railway login

# Create new project
railway init

# Add PostgreSQL
railway add postgresql

# Set environment variables
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set SPOTIFY_CLIENT_ID=ba12d0b2c6524b2e9cec5387458b45eb
railway variables set SPOTIFY_CLIENT_SECRET=51aada2b46184bf79bde2ddb8dcde57c
railway variables set SPOTIFY_REDIRECT_URI=https://your-app.railway.app/login/oauth2/code/spotify

# Deploy
railway up
```

**Cost**: FREE (with $5/month credit)

---

## Option 3: Render (ZIP Upload)

### Step 1: Create ZIP file
1. Compress your entire Soundscape folder into a ZIP file
2. Make sure all files are included

### Step 2: Deploy on Render
1. Go to https://render.com
2. Sign up for free account
3. Click "New +" → "Web Service"
4. Choose "Build and deploy from a Git repository" → "Public Git repository"
5. Enter: `https://github.com/CREmmanuel04/Soundscape`
6. Or use "Upload from computer" and select your ZIP

### Step 3: Configure
- **Build Command**: `./mvnw clean package -DskipTests`
- **Start Command**: `java -jar target/soundscape-0.0.1-SNAPSHOT.jar`
- **Environment Variables**: Add the same ones as above

**Cost**: FREE tier available

---

## Option 4: Simple ZIP Upload Services

### Surge.sh (For static sites only - won't work for your Spring Boot app)
### Netlify (For static sites only - won't work for your Spring Boot app)

These won't work because your app needs a Java server.

---

## Recommendation

**For beginners**: Use **Railway CLI** (Option 2)
- Free tier
- Simple CLI commands
- Automatic database setup

**For reliability**: Use **Heroku** (Option 1)
- Most stable platform
- Best documentation
- Costs money but very reliable

**For completely free**: Use **Render** (Option 3)
- Free tier available
- Can upload ZIP directly
- Good for testing

Would you like me to help you with any of these specific deployment methods?