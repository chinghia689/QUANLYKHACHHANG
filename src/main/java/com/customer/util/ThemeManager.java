package com.customer.util;

import javafx.scene.Scene;

import java.util.prefs.Preferences;

/**
 * Singleton class to manage theme switching between Dark and Light modes.
 */
public class ThemeManager {

    private static ThemeManager instance;
    private static final String THEME_PREF_KEY = "app_theme";
    private static final String DARK_THEME = "dark";
    private static final String LIGHT_THEME = "light";

    private final Preferences prefs;
    private String currentTheme;
    private Scene scene;

    private ThemeManager() {
        prefs = Preferences.userNodeForPackage(ThemeManager.class);
        currentTheme = prefs.get(THEME_PREF_KEY, DARK_THEME);
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Set the scene to apply themes to.
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Apply the current theme to the scene.
     */
    public void applyTheme() {
        if (scene == null) return;

        scene.getStylesheets().clear();

        // Always add common styles first
        String commonCss = getClass().getResource("/styles/common.css").toExternalForm();
        scene.getStylesheets().add(commonCss);

        // Add theme-specific styles
        String themeCss;
        if (LIGHT_THEME.equals(currentTheme)) {
            themeCss = getClass().getResource("/styles/light-theme.css").toExternalForm();
        } else {
            themeCss = getClass().getResource("/styles/dark-theme.css").toExternalForm();
        }
        scene.getStylesheets().add(themeCss);
    }

    /**
     * Toggle between dark and light themes.
     */
    public void toggleTheme() {
        if (DARK_THEME.equals(currentTheme)) {
            currentTheme = LIGHT_THEME;
        } else {
            currentTheme = DARK_THEME;
        }

        // Save preference
        prefs.put(THEME_PREF_KEY, currentTheme);

        // Apply the new theme
        applyTheme();
    }

    /**
     * Set a specific theme.
     */
    public void setTheme(String theme) {
        if (DARK_THEME.equals(theme) || LIGHT_THEME.equals(theme)) {
            currentTheme = theme;
            prefs.put(THEME_PREF_KEY, currentTheme);
            applyTheme();
        }
    }

    /**
     * Check if current theme is dark.
     */
    public boolean isDarkTheme() {
        return DARK_THEME.equals(currentTheme);
    }

    /**
     * Get current theme name.
     */
    public String getCurrentTheme() {
        return currentTheme;
    }
}
