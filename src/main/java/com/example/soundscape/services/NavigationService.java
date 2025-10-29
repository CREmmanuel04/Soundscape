package com.example.soundscape.services;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * NavigationService - Demonstrates OOP principles for the navigation system
 * 
 * OOP Principles Demonstrated:
 * 1. Encapsulation - Private fields with public methods
 * 2. Abstraction - Abstract navigation behavior through interfaces
 * 3. Single Responsibility - Handles only navigation-related logic
 */
@Service
public class NavigationService {
    
    // ENCAPSULATION: Private fields with controlled access
    private final Map<String, NavigationItem> navigationItems;
    private final Map<String, String> routeMappings;
    
    public NavigationService() {
        this.navigationItems = new HashMap<>();
        this.routeMappings = new HashMap<>();
        initializeNavigationItems();
        initializeRouteMappings();
    }
    
    /**
     * ENCAPSULATION: Private method to initialize navigation items
     */
    private void initializeNavigationItems() {
        navigationItems.put("home", new NavigationItem("home", "Soundscape", "fas fa-headphones-alt", "/", "Navigate to home page"));
        navigationItems.put("feed", new NavigationItem("feed", "Feed", "fas fa-stream", "/posts", "View posts feed"));
        navigationItems.put("messages", new NavigationItem("messages", "Messages", "fas fa-envelope", "/messages", "View messages"));
        navigationItems.put("logout", new NavigationItem("logout", "Logout", "fas fa-sign-out-alt", "/logout", "Sign out of application"));
    }
    
    /**
     * ENCAPSULATION: Private method to initialize route mappings
     */
    private void initializeRouteMappings() {
        routeMappings.put("/", "home");
        routeMappings.put("/posts", "feed");
        routeMappings.put("/messages", "messages");
        routeMappings.put("/logout", "logout");
    }
    
    /**
     * ABSTRACTION: Public interface for getting navigation items
     */
    public NavigationItem getNavigationItem(String key) {
        return navigationItems.get(key);
    }
    
    /**
     * ABSTRACTION: Public interface for getting all navigation items
     */
    public Map<String, NavigationItem> getAllNavigationItems() {
        return new HashMap<>(navigationItems); // Defensive copy
    }
    
    /**
     * ABSTRACTION: Public interface for route validation
     */
    public boolean isValidRoute(String route) {
        return routeMappings.containsKey(route);
    }
    
    /**
     * ABSTRACTION: Public interface for getting current page identifier
     */
    public String getCurrentPageIdentifier(String route) {
        return routeMappings.getOrDefault(route, "unknown");
    }
    
    /**
     * ABSTRACTION: Public interface for getting navigation context
     */
    public NavigationContext getNavigationContext(String currentRoute) {
        String currentPage = getCurrentPageIdentifier(currentRoute);
        return new NavigationContext(currentPage, navigationItems, isValidRoute(currentRoute));
    }
    
    /**
     * Inner class demonstrating ENCAPSULATION and data structure
     */
    public static class NavigationItem {
        private final String id;
        private final String label;
        private final String iconClass;
        private final String route;
        private final String description;
        
        public NavigationItem(String id, String label, String iconClass, String route, String description) {
            this.id = id;
            this.label = label;
            this.iconClass = iconClass;
            this.route = route;
            this.description = description;
        }
        
        // ENCAPSULATION: Getter methods for controlled access
        public String getId() { return id; }
        public String getLabel() { return label; }
        public String getIconClass() { return iconClass; }
        public String getRoute() { return route; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() {
            return String.format("NavigationItem{id='%s', label='%s', route='%s'}", id, label, route);
        }
    }
    
    /**
     * Inner class demonstrating COMPOSITION and ENCAPSULATION
     */
    public static class NavigationContext {
        private final String currentPage;
        private final Map<String, NavigationItem> availableItems;
        private final boolean isValidRoute;
        
        public NavigationContext(String currentPage, Map<String, NavigationItem> availableItems, boolean isValidRoute) {
            this.currentPage = currentPage;
            this.availableItems = new HashMap<>(availableItems); // Defensive copy
            this.isValidRoute = isValidRoute;
        }
        
        // ENCAPSULATION: Controlled access to internal state
        public String getCurrentPage() { return currentPage; }
        public Map<String, NavigationItem> getAvailableItems() { return new HashMap<>(availableItems); }
        public boolean isValidRoute() { return isValidRoute; }
        
        public boolean isCurrentPage(String pageId) {
            return currentPage.equals(pageId);
        }
    }
}