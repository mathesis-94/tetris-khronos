package com.tetriskhronos.controller;

import com.tetriskhronos.ai.TrainingMode;
import com.tetriskhronos.model.Game;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameLoopThread extends Thread {
    private final Game game;
    private final Runnable renderCallback;
    private final AtomicBoolean running;
    private final long frameTime;

    private TrainingMode trainingMode;
    private boolean isTraining = false;

    public GameLoopThread(Game game, Runnable renderCallback) {
        this.game = game;
        this.renderCallback = renderCallback;
        this.running = new AtomicBoolean(false);
        this.frameTime = 1_000_000_000L / 60; // 60 FPS in nanoseconds
        setDaemon(true);
        setName("GameLoopThread");
    }

    /**
     * Start the game loop in normal mode (with rendering at 60 FPS)
     */
    public void startGameLoop() {
        isTraining = false;
        start();
    }

    /**
     * Start the game loop in training mode (headless, no rendering)
     */
    public void startTrainingSession(int episodes, int saveInterval) {
        isTraining = true;
        this.trainingMode = new TrainingMode(
            game.getAIPlayer(),
            episodes,
            saveInterval,
            game
        );
        start();
    }

    @Override
    public void run() {
        running.set(true);

        if (isTraining) {
            runTrainingMode();
        } else {
            runGameLoop();
        }

        running.set(false);
    }

    private void runGameLoop() {
        long lastFrameTime = System.nanoTime();

        while (running.get()) {
            long currentTime = System.nanoTime();
            long deltaTime = currentTime - lastFrameTime;

            if (deltaTime >= frameTime) {
                game.update();

                if (renderCallback != null) {
                    renderCallback.run();
                }

                lastFrameTime = currentTime;
            } else {
                try {
                    long sleepTime = (frameTime - deltaTime) / 1_000_000;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void runTrainingMode() {
        try {
            trainingMode.runTrainingSession();
        } catch (Exception e) {
            System.err.println("Training mode error:");
            e.printStackTrace();
        }
    }

    public void stopLoop() {
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isInTrainingMode() {
        return isTraining;
    }
}
