package com.tetriskhronos.controller;

import com.tetriskhronos.model.Game;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameLoopThread extends Thread {
    private final Game game;
    private final Runnable renderCallback;
    private final AtomicBoolean running;
    private final long frameTime;

    public GameLoopThread(Game game, Runnable renderCallback) {
        this.game = game;
        this.renderCallback = renderCallback;
        this.running = new AtomicBoolean(false);
        this.frameTime = 1_000_000_000L / 60; // 60 FPS in nanoseconds
        setDaemon(true);
        setName("GameLoopThread");
    }

    @Override
    public void run() {
        running.set(true);
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

    public void stopLoop() {
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }
}
