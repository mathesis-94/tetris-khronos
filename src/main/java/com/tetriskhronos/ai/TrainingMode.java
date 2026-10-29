package com.tetriskhronos.ai;

import com.tetriskhronos.model.Game;
import com.tetriskhronos.model.Board;
import com.tetriskhronos.model.Configuration;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class TrainingMode {
    private final AIPlayer aiPlayer;
    private final int episodesToRun;
    private final int saveInterval;
    private final Game gameTemplate;

    private int episodeCount = 0;
    private final Deque<Double> recentScores;
    private long startTime;

    public TrainingMode(AIPlayer aiPlayer, int episodesToRun, int saveInterval, Game gameTemplate) {
        this.aiPlayer = aiPlayer;
        this.episodesToRun = episodesToRun;
        this.saveInterval = saveInterval;
        this.gameTemplate = gameTemplate;
        this.recentScores = new ArrayDeque<>(saveInterval);
    }

    public void runTrainingSession() throws IOException {
        startTime = System.currentTimeMillis();
        System.out.println("========================================");
        System.out.println("Starting Training Mode: " + episodesToRun + " episodes");
        System.out.println("Save interval: " + saveInterval);
        System.out.println("========================================");

        for (int i = 0; i < episodesToRun; i++) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("\nTraining interrupted.");
                break;
            }

            episodeCount++;
            int finalScore = runEpisode();
            recentScores.addLast((double) finalScore);

            if (recentScores.size() > saveInterval) {
                recentScores.removeFirst();
            }

            if (episodeCount % saveInterval == 0) {
                logProgress();
                saveWeights();
            }
        }

        logFinalStats();
    }

    private int runEpisode() {
        gameTemplate.reset();
        aiPlayer.startEpisode(gameTemplate.getBoard());

        int moveCount = 0;
        while (!gameTemplate.isGameOver()) {
            moveCount++;
            if (moveCount % 100 == 0) {
                System.out.println("[Episode " + episodeCount + "] Move " + moveCount);
            }
            gameTemplate.update();
        }

        int finalScore = gameTemplate.getScore();
        aiPlayer.endGame(finalScore);
        System.out.println("[Episode " + episodeCount + "] Completed - Moves: " + moveCount + ", Score: " + finalScore);

        return finalScore;
    }

    private void logProgress() {
        double avgScore = getAverageScore();
        double maxScore = getMaxScore();
        double minScore = getMinScore();
        long elapsed = System.currentTimeMillis() - startTime;
        double epsPerSecond = (episodeCount / (double) elapsed) * 1000.0;

        System.out.printf(
            "[%d/%d] Avg: %.0f | Min: %.0f | Max: %.0f | EPS: %.1f | Time: %dmin%n",
            episodeCount, episodesToRun, avgScore, minScore, maxScore, epsPerSecond, elapsed / 60000
        );
    }

    private void logFinalStats() {
        long elapsed = System.currentTimeMillis() - startTime;
        double totalEps = (episodeCount / (double) elapsed) * 1000.0;

        System.out.println("========================================");
        System.out.printf("Training Complete!%n");
        System.out.printf("Episodes: %d | Duration: %.1f minutes%n", episodeCount, elapsed / 60000.0);
        System.out.printf("Final Avg Score: %.0f%n", getAverageScore());
        System.out.printf("Best Score: %.0f%n", getMaxScore());
        System.out.printf("Avg EPS: %.1f%n", totalEps);
        System.out.println("========================================");
    }

    private void saveWeights() {
        aiPlayer.saveWeights();
    }

    private double getAverageScore() {
        return recentScores.isEmpty() ? 0 :
            recentScores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double getMaxScore() {
        return recentScores.isEmpty() ? 0 :
            recentScores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    private double getMinScore() {
        return recentScores.isEmpty() ? 0 :
            recentScores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }
}
