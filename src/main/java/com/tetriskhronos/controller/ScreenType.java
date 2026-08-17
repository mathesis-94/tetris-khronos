package com.tetriskhronos.controller;
// enum available screens
public enum ScreenType {
    SPLASH("Splash Screen"),
    MAIN_MENU("Main Menu"),
    CONFIG("Configuration"),
    HIGH_SCORES("High Scores"),
    PLAYING("Game Play");

    private final String displayName;
    ScreenType(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
