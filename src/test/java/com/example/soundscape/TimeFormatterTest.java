package com.example.soundscape;

import com.example.soundscape.controllers.PostController;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure Unit Tests for the TimeFormatter utility class.
 * This ensures the time display logic remains correct.
 */
public class TimeFormatterTest {

    // Instantiate the inner class TimeFormatter from PostController
    private final PostController.TimeFormatter formatter = new PostController.TimeFormatter();

    @Test
    void format_justNow() {
        // Current time, should return "Just now"
        Instant now = Instant.now();
        String result = formatter.format(now);
        assertEquals("Just now", result, "Should return 'Just now' for very recent times.");
    }

    @Test
    void format_secondsAgo() {
        // 10 seconds ago
        Instant tenSecondsAgo = Instant.now().minus(10, ChronoUnit.SECONDS);
        String result = formatter.format(tenSecondsAgo);
        assertEquals("10s ago", result, "Should return 'Xs ago' for times under 60 seconds.");
    }

    @Test
    void format_minutesAgo() {
        // 15 minutes ago
        Instant fifteenMinutesAgo = Instant.now().minus(15, ChronoUnit.MINUTES);
        String result = formatter.format(fifteenMinutesAgo);
        assertEquals("15m ago", result, "Should return 'Xm ago' for times under 60 minutes.");
    }

    @Test
    void format_hoursAgo() {
        // 10 hours ago
        Instant tenHoursAgo = Instant.now().minus(10, ChronoUnit.HOURS);
        String result = formatter.format(tenHoursAgo);
        assertEquals("10h ago", result, "Should return 'Xh ago' for times under 24 hours.");
    }

    @Test
    void format_yesterday() {
        // 25 hours ago (falls into the Yesterday category)
        Instant yesterday = Instant.now().minus(25, ChronoUnit.HOURS);
        String result = formatter.format(yesterday);
        assertEquals("Yesterday", result, "Should return 'Yesterday' for times between 24 and 48 hours.");
    }

    @Test
    void format_daysAgo() {
        // 3 days ago
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
        String result = formatter.format(threeDaysAgo);
        assertEquals("3d ago", result, "Should return 'Xd ago' for times under 7 days but over 1 day.");
    }
}