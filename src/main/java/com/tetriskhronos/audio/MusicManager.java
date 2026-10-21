package com.tetriskhronos.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class MusicManager {
    private static final MusicManager INSTANCE = new MusicManager();
    private MediaPlayer mediaPlayer;
    private boolean muted = false;
    private static final String MUSIC_FILE = "sounds/background.mp3";

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
                System.err.println("❌ Music file not found: " + MUSIC_FILE);
                tryLoadFallback();
                return;
            }

            Media media = new Media(musicUrl.toExternalForm());
            this.mediaPlayer = new MediaPlayer(media);
            this.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            this.mediaPlayer.setVolume(0.5);

            // Add error handler
            mediaPlayer.setOnError(() -> {
                System.err.println("❌ Media error: " + mediaPlayer.getError());
                tryLoadFallback();
            });

            System.out.println("✅ Music loaded: " + MUSIC_FILE);
        } catch (Exception e) {
            System.err.println("❌ Error loading music: " + e.getMessage());
            tryLoadFallback();
        }
    }

    private void tryLoadFallback() {
        try {
            URL fallbackUrl = getClass().getClassLoader().getResource("clear_line.wav");
            if (fallbackUrl != null) {
                Media fallback = new Media(fallbackUrl.toExternalForm());
                this.mediaPlayer = new MediaPlayer(fallback);
                this.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                this.mediaPlayer.setVolume(0.3);
                System.out.println("✅ Fallback audio loaded: clear_line.wav");
            }
        } catch (Exception e) {
            System.err.println("❌ Fallback also failed: " + e.getMessage());
        }
    }

    public void play() {
        if (mediaPlayer != null && !muted) {
            try {
                mediaPlayer.play();
                System.out.println("▶️ Music playing...");
            } catch (Exception e) {
                System.err.println("❌ Error playing music: " + e.getMessage());
            }
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                System.out.println("⏹️ Music stopped");
            } catch (Exception e) {
                System.err.println("❌ Error stopping music: " + e.getMessage());
            }
        }
    }

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            stop();
            System.out.println("🔇 Music: MUTED");
        } else {
            play();
            System.out.println("🔊 Music: UNMUTED");
        }
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
