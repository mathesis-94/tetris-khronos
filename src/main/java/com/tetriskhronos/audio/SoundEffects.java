package com.tetriskhronos.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundEffects {
    private static final SoundEffects INSTANCE = new SoundEffects();
    private final Map<String, Media> soundCache = new HashMap<>();
    private boolean muted = false;

    private SoundEffects() {
        loadSounds();
    }

    public static SoundEffects getInstance() {
        return INSTANCE;
    }


    private void loadSounds() {
        String[] soundFiles = {
            "sounds/clear_line.wav",
            "sounds/rotate.wav",
            "sounds/hard_drop.wav",
            "sounds/game_over.wav"
        };

        for (String soundFile : soundFiles) {
            try {
                URL soundUrl = getClass().getClassLoader().getResource(soundFile);
                if (soundUrl != null) {
                    Media media = new Media(soundUrl.toExternalForm());
                    soundCache.put(soundFile, media);
                } else {
                    System.err.println("Sound file not found: " + soundFile);
                }
            } catch (Exception e) {
                System.err.println("Error loading sound " + soundFile + ": " + e.getMessage());
            }
        }
    }
    private void playSound(String soundFile) {
        if (muted || !soundCache.containsKey(soundFile)) {
            return;
        }
        try {
            Media media = soundCache.get(soundFile);
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(0.7);
            player.play();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    public void playClearLines() {
        playSound("sounds/clear_line.wav");
    }

    public void playRotate() {
        playSound("sounds/rotate.wav");
    }
    public void playHardDrop() {
        playSound("sounds/hard_drop.wav");
    }

    public void playGameOver() {
        playSound("sounds/game_over.wav");
    }
    public void toggleMute() {
        muted = !muted;
        System.out.println("Sound Effects: " + (muted ? "MUTED" : "UNMUTED"));
    }
    public boolean isMuted() {
        return muted;
    }
}
