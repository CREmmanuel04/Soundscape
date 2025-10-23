# Music Analysis Feature Documentation

## Overview
The Music Analysis page provides comprehensive insights into a user's Spotify listening habits, including:

- **Top Genres Analysis**: Visual breakdown of most listened-to genres with interactive charts
- **Top Artists by Time Range**: Artists from last 4 weeks, 6 months, and all time
- **Top Tracks by Time Range**: Tracks from different time periods
- **Recently Played**: Latest listening activity
- **Listening Statistics**: Overview of user's music consumption patterns

## Features Implemented

### 🎵 **Genre Analysis**
- Extracts genres from user's top artists
- Combines data from different time ranges with weighted scoring:
  - Recent (4 weeks): 3x weight
  - Medium (6 months): 2x weight  
  - All time: 1x weight
- Interactive doughnut chart visualization
- Top 10 genres displayed with frequency counts

### 🎤 **Artist Analysis**
- Three time ranges: Last 4 weeks, Last 6 months, All time
- Artist popularity scores
- Profile images
- Genre information for each artist
- Interactive tabs to switch between time ranges

### 🎧 **Track Analysis**
- Top tracks across different time periods
- Album artwork
- Artist information
- Track popularity and duration data
- Interactive time range selection

### ⏰ **Recently Played**
- Last 50 recently played tracks
- Real-time listening activity
- Played timestamps
- Album artwork and artist info

### 📊 **Listening Statistics**
- Number of top artists/tracks
- Genre diversity metrics
- Most popular genre identification

## Technical Implementation

### Backend (Spring Boot)

#### New Controller: `MusicAnalysisController.java`
```java
@GetMapping("/music-analysis") // Main page
@GetMapping("/api/music-analysis") // AJAX endpoint for data
@GetMapping("/api/recently-played") // Recently played tracks
```

#### Enhanced Service: `SpotifyService.java`
New methods added:
- `getUserTopTracks()` - Detailed track information
- `getUserTopArtistsDetailed()` - Artists with genre analysis
- `getUserMusicAnalysis()` - Comprehensive analysis combining all data
- `getRecentlyPlayed()` - Recent listening activity

#### Spotify API Endpoints Used:
- `/v1/me/top/artists` - Top artists (short_term, medium_term, long_term)
- `/v1/me/top/tracks` - Top tracks (short_term, medium_term, long_term)  
- `/v1/me/player/recently-played` - Recently played tracks

### Frontend (HTML/CSS/JavaScript)

#### Template: `music-analysis.html`
- Modern dark theme consistent with app design
- Responsive grid layout
- Interactive charts using Chart.js
- AJAX data loading for smooth user experience
- Tab-based navigation for different time ranges

#### Key JavaScript Functions:
- `loadMusicAnalysis()` - Fetches comprehensive user data
- `displayGenres()` - Shows genre breakdown with chart
- `createGenreChart()` - Chart.js doughnut chart
- `displayArtists(timeRange)` - Artist grid for selected period
- `displayTracks(timeRange)` - Track list for selected period
- `displayRecentlyPlayed()` - Recent activity feed

## Data Analysis Logic

### Genre Scoring Algorithm
```javascript
// Weight recent listening more heavily
shortTerm genres × 3 + mediumTerm genres × 2 + longTerm genres × 1
// Results in more accurate current taste representation
```

### Time Range Definitions (Spotify API)
- **short_term**: Last 4 weeks
- **medium_term**: Last 6 months  
- **long_term**: All time (several years)

## User Experience

### Navigation
- Added "Music Analysis" link to main navigation
- Accessible from any page when logged in with Spotify connected

### Error Handling
- Graceful fallback when Spotify not connected
- Loading states during data fetch
- Error messages for API failures
- Image fallbacks for missing album/artist artwork

### Responsive Design
- Works on desktop and mobile devices
- Grid layouts adapt to screen size
- Touch-friendly interactive elements

## Spotify Permissions Required
The following scopes are needed (already configured):
- `user-read-private` - Basic profile access
- `user-read-email` - Email access
- `user-top-read` - Top artists and tracks
- `user-read-recently-played` - Recent listening history

## Usage Instructions

### For Users:
1. **Connect Spotify**: Link your Spotify account from the profile page
2. **Navigate**: Click "Music Analysis" in the main navigation
3. **Explore**: Use tabs to switch between different time ranges
4. **Analyze**: View your top genres chart and listening patterns

### For Developers:
1. **API Endpoints**: Use `/api/music-analysis` and `/api/recently-played` for custom integrations
2. **Data Format**: JSON responses with structured music data
3. **Caching**: Consider implementing caching for better performance
4. **Rate Limits**: Spotify API has rate limits - handle gracefully

## File Structure
```
src/main/java/com/example/soundscape/
├── controllers/MusicAnalysisController.java (NEW)
├── services/SpotifyService.java (ENHANCED)

src/main/resources/templates/
├── music-analysis.html (NEW)
├── fragments/header.html (NEW)
├── home.html (UPDATED - navigation)
```

## Future Enhancements

### Potential Features:
- **Music Taste Comparison**: Compare with friends' listening habits
- **Playlist Generation**: Auto-create playlists based on analysis
- **Listening Goals**: Track listening diversity goals
- **Mood Analysis**: Analyze audio features (valence, energy, etc.)
- **Export Data**: Download listening reports
- **Social Features**: Share music taste insights

### Technical Improvements:
- **Caching**: Redis cache for API responses
- **Pagination**: Handle large datasets efficiently
- **Real-time Updates**: WebSocket for live listening activity
- **Advanced Analytics**: Machine learning for music recommendations

## Testing Notes

### Manual Testing Checklist:
- [ ] Page loads correctly when Spotify connected
- [ ] Shows "connect Spotify" prompt when not connected
- [ ] Genre chart displays and updates
- [ ] Time range tabs work correctly
- [ ] Recently played tracks load
- [ ] Error handling works for API failures
- [ ] Responsive design works on mobile
- [ ] Navigation link works from all pages

### Test Data Requirements:
- User account with active Spotify Premium subscription
- Sufficient listening history for meaningful analysis
- Recent listening activity for "recently played" section

## Performance Considerations

### Optimization Strategies:
- **Lazy Loading**: Load charts only when tab is active
- **Data Compression**: Minimize API response sizes
- **Client-side Caching**: Cache analysis data for session
- **Progressive Loading**: Show partial data while loading complete analysis

### Spotify API Limits:
- Rate limit: 100 requests per minute per user
- Data freshness: Updates may take time to reflect in API
- Historical data: Limited to user's Spotify listening history

## Security Notes

### Data Privacy:
- No listening data stored permanently
- Real-time API calls ensure fresh data
- User controls Spotify connection/disconnection
- Complies with Spotify Developer Terms of Service

### Access Control:
- Requires user authentication
- Spotify token validation
- Proper error handling for expired tokens

This feature significantly enhances the Soundscape application by providing users with deep insights into their music consumption patterns and preferences.