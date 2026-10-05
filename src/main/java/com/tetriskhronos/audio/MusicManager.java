package com.tetriskhronos.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class MusicManager {
    private static final MusicManager INSTANCE = new MusicManager();
    private MediaPlayer mediaPlayer;
    private boolean muted = false;
    private static final String MUSIC_FILE = "music/background.mp3";

    private MusicManager() {
        loadMusic();
    }

    public static MusicManager getInstance() {
        return INSTANCE;
    }

    private void loadMusic() {
        try {
            URL musicUrl = getClass().getClassLoader().getResource(MUSIC_FILE);
            if (musicUrl == null) {
                System.err.println("Music file not found: " + MUSIC_FILE);
                return;
            }
            Media media = new Media(musicUrl.toExternalForm());
            this.mediaPlayer = new MediaPlayer(media);
            this.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            this.mediaPlayer.setVolume(0.5);
        } catch (Exception e) {
            System.err.println("Error loading music: " + e.getMessage());
        }
    }

    public void play() {
        if (mediaPlayer != null && !muted) {
            mediaPlayer.play();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    // mute state toggle
    public void toggleMute() {
        muted = !muted;
        if (muted) {
            stop();
        } else {
            play();
        }
        System.out.println("Music: " + (muted ? "MUTED" : "UNMUTED"));
    }
    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(Math.max(0.0, Math.min(1.0, volume)));
        }
    }
    public boolean isMuted() {
        return muted;
    }
}
