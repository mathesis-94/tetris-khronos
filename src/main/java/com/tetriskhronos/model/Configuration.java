package com.tetriskhronos.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;


// singleton, class manages all app config settings
public class Configuration {
    private static Configuration instance;
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());
    private static final String CONFIG_FILE_PATH = "src/main/resources/config.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // field dimensions
    private int fieldWidth = 10;
    private int fieldHeight = 20;

    // window dimensions
    private int windowWidth = 1200;
    private int windowHeight = 800;

    // configs for game per demo video for m1
    private int gameLevel = 1;
    private boolean musicEnabled = true;
    private boolean soundEffectsEnabled = true;
    private boolean aiPlayEnabled = false;
    private boolean extendModeEnabled = false;

    /**
     * prevent direct instantiation
     */
    private Configuration() {
    }

    public static synchronized Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public void load() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);

        if (!configFile.exists()) {
            LOGGER.warning("Config file not found at: " + CONFIG_FILE_PATH + ". Using defaults.");
            return;
        }

        try {
            ConfigData data = MAPPER.readValue(configFile, ConfigData.class);

            this.setFieldWidth(data.fieldWidth);
            this.setFieldHeight(data.fieldHeight);
            this.setGameLevel(data.gameLevel);
            this.setMusicEnabled(data.musicEnabled);
            this.setSoundEffectsEnabled(data.soundEffectsEnabled);
            this.setAiPlayEnabled(data.aiPlayEnabled);
            this.setExtendModeEnabled(data.extendModeEnabled);
            this.setWindowWidth(data.windowWidth);
            this.setWindowHeight(data.windowHeight);

            LOGGER.info("Configuration loaded successfully from: " + CONFIG_FILE_PATH);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load config.json, using defaults", e);
            throw e;
        }
    }

    /**
     * Save current config to config.json
     */
    public void save() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                LOGGER.warning("Failed to create parent directory: " + parentDir.getAbsolutePath());
            }
        }

        try {
            // Serialise current state to ConfigData and write as JSON
            ConfigData data = new ConfigData(
                this.fieldWidth,
                this.fieldHeight,
                this.gameLevel,
                this.musicEnabled,
                this.soundEffectsEnabled,
                this.aiPlayEnabled,
                this.extendModeEnabled,
                this.windowWidth,
                this.windowHeight
            );

            MAPPER.writerWithDefaultPrettyPrinter().writeValue(configFile, data);
            LOGGER.info("Configuration saved successfully to: " + CONFIG_FILE_PATH);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save config.json", e);
            throw e;
        }
    }

    // getters and setters
    public int getFieldWidth() {
        return fieldWidth;
    }

    public void setFieldWidth(int fieldWidth) {
        if (fieldWidth >= 5 && fieldWidth <= 15) {
            this.fieldWidth = fieldWidth;
        } else {
            LOGGER.warning("Invalid fieldWidth: " + fieldWidth + ". Must be 5–15. Keeping current: " + this.fieldWidth);
        }
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public void setFieldHeight(int fieldHeight) {
        if (fieldHeight >= 15 && fieldHeight <= 30) {
            this.fieldHeight = fieldHeight;
        } else {
            LOGGER.warning("Invalid fieldHeight: " + fieldHeight + ". Must be 15–30. Keeping current: " + this.fieldHeight);
        }
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        if (windowWidth >= 600 && windowWidth <= 2560) {
            this.windowWidth = windowWidth;
        } else {
            LOGGER.warning("Invalid windowWidth: " + windowWidth + ". Must be 600–2560. Keeping current: " + this.windowWidth);
        }
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        if (windowHeight >= 400 && windowHeight <= 1440) {
            this.windowHeight = windowHeight;
        } else {
            LOGGER.warning("Invalid windowHeight: " + windowHeight + ". Must be 400–1440. Keeping current: " + this.windowHeight);
        }
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public void setGameLevel(int gameLevel) {
        if (gameLevel >= 1 && gameLevel <= 10) {
            this.gameLevel = gameLevel;
        } else {
            LOGGER.warning("Invalid gameLevel: " + gameLevel + ". Must be 1–10. Keeping current: " + this.gameLevel);
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
        this.windowWidth = 1200;
        this.windowHeight = 800;
        this.gameLevel = 1;
        this.musicEnabled = true;
        this.soundEffectsEnabled = true;
        this.aiPlayEnabled = false;
        this.extendModeEnabled = false;
        LOGGER.info("Configuration reset to defaults");
    }

    /**
     * Data transfer object for JSON serialisation, Jackson uses this to map JSON fields to conf fields
     */
    static class ConfigData {
        public int fieldWidth;
        public int fieldHeight;
        public int windowWidth;
        public int windowHeight;
        public int gameLevel;
        public boolean musicEnabled;
        public boolean soundEffectsEnabled;
        public boolean aiPlayEnabled;
        public boolean extendModeEnabled;

        public ConfigData() {
        }

        public ConfigData(int fieldWidth, int fieldHeight, int gameLevel,
                         boolean musicEnabled, boolean soundEffectsEnabled,
                         boolean aiPlayEnabled, boolean extendModeEnabled,
                         int windowWidth, int windowHeight) {
            this.fieldWidth = fieldWidth;
            this.fieldHeight = fieldHeight;
            this.gameLevel = gameLevel;
            this.musicEnabled = musicEnabled;
            this.soundEffectsEnabled = soundEffectsEnabled;
            this.aiPlayEnabled = aiPlayEnabled;
            this.extendModeEnabled = extendModeEnabled;
            this.windowWidth = windowWidth;
            this.windowHeight = windowHeight;
        }
    }
}
