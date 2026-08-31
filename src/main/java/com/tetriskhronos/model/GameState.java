package com.tetriskhronos.model;

public enum GameState {
    IDLE("Idle"),
    RUNNING("Running"),
    PAUSED("Paused"),
    GAME_OVER("Game Over");

    private final String displayName;

    GameState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPlayableState() {
        return switch (this) {
            case RUNNING, PAUSED -> true;
            default -> false;
        };
    }
}
