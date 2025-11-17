# Deploying Soundscape to Render

This guide will help you deploy your Soundscape application to Render.

## Prerequisites

1. A Render account (sign up at https://render.com)
2. Your Soundscape repository pushed to GitHub
3. Spotify Developer credentials (Client ID and Client Secret)

## Deployment Steps

### 1. Prepare Spotify Configuration

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Open your app settings
3. Add Render redirect URI: `https://YOUR-APP-NAME.onrender.com/login/oauth2/code/spotify`
   - Replace `YOUR-APP-NAME` with your actual Render service name
4. Keep your Client ID and Client Secret handy

### 2. Deploy to Render

#### Option A: Using Render Blueprint (Automated)

1. Push your code to GitHub (including the `render.yaml` file)
2. Go to [Render Dashboard](https://dashboard.render.com)
3. Click "New" → "Blueprint"
4. Connect your GitHub repository
5. Render will detect `render.yaml` and create:
   - PostgreSQL database
   - Web service with Docker

#### Option B: Manual Deployment

1. **Create PostgreSQL Database**
   - Dashboard → New → PostgreSQL
   - Name: `soundscape-db`
   - Plan: Free
   - Create Database

2. **Create Web Service**
   - Dashboard → New → Web Service
   - Connect your repository
   - Name: `soundscape` (or your choice)
   - Runtime: Docker
   - Plan: Free
   - Build Command: (auto-detected from Dockerfile)
   - Start Command: (auto-detected from Dockerfile)

### 3. Configure Environment Variables

In your Web Service settings, add these environment variables:

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPOTIFY_CLIENT_ID` | Your Spotify Client ID |
| `SPOTIFY_CLIENT_SECRET` | Your Spotify Client Secret |
| `SPOTIFY_REDIRECT_URI` | `https://YOUR-APP-NAME.onrender.com/login/oauth2/code/spotify` |
| `DATABASE_URL` | (Auto-set by Render from database connection) |

### 4. Update Spotify Redirect URI

After deployment, update your Spotify app settings with the actual URL:
- `https://YOUR-ACTUAL-RENDER-URL.onrender.com/login/oauth2/code/spotify`

## Important Notes

### Free Tier Limitations
- **Cold starts**: Free tier services spin down after 15 minutes of inactivity
- **First request**: May take 30-60 seconds to wake up
- **Database**: 90 days of inactivity limit

### Custom Domain (Optional)
1. Go to your Web Service → Settings
2. Add your custom domain
3. Update Spotify redirect URI accordingly

### Monitoring
- View logs: Web Service → Logs
- Check health: `https://your-app.onrender.com/actuator/health`

## Troubleshooting

### Build Fails
- Check logs in Render dashboard
- Verify `Dockerfile` is present
- Ensure all dependencies are in `pom.xml`

### Database Connection Issues
- Verify `DATABASE_URL` environment variable is set
- Check database is running in Render dashboard
- Review application logs

### Spotify OAuth Errors
- Verify redirect URI matches exactly (including https)
- Check environment variables are set correctly
- Ensure Spotify app is not in development mode restrictions

## Updating Your Application

1. Push changes to GitHub
2. Render auto-deploys from main/master branch
3. Monitor deployment in Render dashboard

## Cost Estimate

With free tier:
- Web Service: Free (750 hours/month)
- PostgreSQL: Free
- **Total: $0/month**

For production use, consider upgrading to paid plans for:
- No cold starts
- More resources
- Better performance
- Database backups

## Support

- Render Documentation: https://render.com/docs
- Spring Boot on Render: https://render.com/docs/deploy-spring-boot
