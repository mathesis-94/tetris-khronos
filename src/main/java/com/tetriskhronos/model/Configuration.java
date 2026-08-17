package com.tetriskhronos.model;
 // singleton, class manages all app config settings
public class Configuration {
    private static Configuration instance;
    private int fieldWidth = 10;
    private int fieldHeight = 20;

        // configs for game per demo video for m1
    private int gameLevel = 1;
    private boolean musicEnabled = true;
    private boolean soundEffectsEnabled = true;
    private boolean aiPlayEnabled = false;
    private boolean extendModeEnabled = false;

        /**
         * Private constructor to prevent direct instantiation.
         */
    private Configuration() {
    }

        /**
         * Gets the singleton instance of Configuration.
         * @return The Configuration instance.
         */
    public static synchronized Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public void setFieldWidth(int fieldWidth) {
        if (fieldWidth >= 5 && fieldWidth <= 15) {
            this.fieldWidth = fieldWidth;
        }
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public void setFieldHeight(int fieldHeight) {
        if (fieldHeight >= 15 && fieldHeight <= 30) {
            this.fieldHeight = fieldHeight;
        }
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public void setGameLevel(int gameLevel) {
        if (gameLevel >= 1 && gameLevel <= 10) {
            this.gameLevel = gameLevel;
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public boolean isSoundEffectsEnabled() {
        return soundEffectsEnabled;
    }

    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        this.soundEffectsEnabled = soundEffectsEnabled;
    }

    public boolean isAiPlayEnabled() {
        return aiPlayEnabled;
    }

    public void setAiPlayEnabled(boolean aiPlayEnabled) {
        this.aiPlayEnabled = aiPlayEnabled;
    }

    public boolean isExtendModeEnabled() {
        return extendModeEnabled;
    }

    public void setExtendModeEnabled(boolean extendModeEnabled) {
        this.extendModeEnabled = extendModeEnabled;
    }

    // reset configs to defaults
    public void resetToDefaults() {
        this.fieldWidth = 10;
        this.fieldHeight = 20;
        this.gameLevel = 1;
        this.musicEnabled = true;
        this.soundEffectsEnabled = true;
        this.aiPlayEnabled = false;
        this.extendModeEnabled = false;
    }
}
