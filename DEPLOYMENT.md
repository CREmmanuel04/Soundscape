# Soundscape Live Website Deployment Checklist

## ✅ Pre-Deployment Checklist

### 1. **Code Preparation** ✅
- [x] PostgreSQL dependency added to pom.xml
- [x] Production configuration created (application-prod.properties)
- [x] Environment variables setup for production
- [x] Security configurations updated

### 2. **Spotify App Configuration** ⚠️ (IMPORTANT!)
Before deploying, you MUST update your Spotify app settings:

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Find your app (with client ID: ba12d0b2c6524b2e9cec5387458b45eb)
3. Edit Settings → Redirect URIs
4. Add your production URL: `https://YOUR-APP-NAME.railway.app/login/oauth2/code/spotify`

### 3. **Environment Variables for Production**
Set these in your deployment platform:

```
SPRING_PROFILES_ACTIVE=prod
SPOTIFY_CLIENT_ID=ba12d0b2c6524b2e9cec5387458b45eb
SPOTIFY_CLIENT_SECRET=51aada2b46184bf79bde2ddb8dcde57c
SPOTIFY_REDIRECT_URI=https://YOUR-APP-NAME.railway.app/login/oauth2/code/spotify
```

## 🚀 Deployment Options (Choose One)

### Option 1: Railway (Recommended - Easiest)
**Cost**: Free tier ($5/month credit)

**Steps**:
1. Go to [railway.app](https://railway.app)
2. Sign up with GitHub
3. "New Project" → "Deploy from GitHub repo" → Select "Soundscape"
4. Add PostgreSQL database: "New" → "Database" → "PostgreSQL"
5. Set environment variables (see above)
6. Deploy!

**Pros**: Automatic, handles database, simple
**Cons**: Limited free tier

### Option 2: Heroku
**Cost**: $7/month minimum

**Steps**:
1. Install Heroku CLI
2. `heroku create your-app-name`
3. `heroku addons:create heroku-postgresql:mini`
4. Set environment variables: `heroku config:set VARIABLE=value`
5. `git push heroku main`

**Pros**: Very reliable, great documentation
**Cons**: No free tier anymore

### Option 3: Render
**Cost**: Free tier available

**Steps**:
1. Go to [render.com](https://render.com)
2. Connect GitHub repository
3. Create PostgreSQL database
4. Set environment variables
5. Deploy

**Pros**: Good free tier
**Cons**: Slower than Railway

## 📋 Post-Deployment Steps

### 1. Test Your Live Website
- [ ] Visit your live URL
- [ ] Test user registration
- [ ] Test Spotify login
- [ ] Test song playback
- [ ] Test all features

### 2. Domain Setup (Optional)
- [ ] Purchase custom domain
- [ ] Configure DNS settings
- [ ] Update Spotify redirect URI to custom domain

### 3. Monitoring Setup
- [ ] Set up application monitoring
- [ ] Configure error alerts
- [ ] Monitor database usage

## 🔧 Troubleshooting Common Issues

### Spotify Login Issues
- **Problem**: "Invalid redirect URI"
- **Solution**: Make sure redirect URI in Spotify app matches your live URL exactly

### Database Connection Issues
- **Problem**: App can't connect to database
- **Solution**: Check DATABASE_URL environment variable is set correctly

### Port Issues
- **Problem**: App won't start
- **Solution**: Make sure PORT environment variable is set (Railway sets this automatically)

## 💰 Cost Breakdown

### Railway (Recommended for beginners)
- **Free tier**: $5/month credit (sufficient for small apps)
- **Paid**: $0.000463 per GB-hour after free credits

### Heroku
- **Eco Dyno**: $7/month
- **Database**: $9/month for Basic PostgreSQL

### Render
- **Free tier**: 750 hours/month (limited resources)
- **Paid**: $7/month for better performance

## 🎯 Recommendation

**For your first deployment, use Railway**:
1. Easiest setup process
2. Automatic database provisioning
3. Free tier sufficient for development/testing
4. Simple environment variable management
5. GitHub integration

Once you're comfortable and need more control or better pricing, consider migrating to other platforms.

## 📞 Need Help?

If you run into issues:
1. Check Railway/Heroku logs for error messages
2. Verify all environment variables are set correctly
3. Test Spotify OAuth with the production redirect URI
4. Make sure your database is properly connected

Your Soundscape app is ready for deployment! 🎵