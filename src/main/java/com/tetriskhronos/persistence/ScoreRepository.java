package com.tetriskhronos.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tetriskhronos.model.Score;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScoreRepository {
    private static ScoreRepository instance;
    private final ObjectMapper objectMapper;
    private final String scoresFilePath;
    private List<Score> scores;

    private ScoreRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Build path safely at runtime, not at class load time
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isEmpty()) {
            userHome = System.getProperty("user.dir");
        }
        this.scoresFilePath = userHome + "/.tetriskhronos/scores.json";

        // Ensure directory exists
        File dir = new File(userHome + "/.tetriskhronos");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        loadScores();
    }


    public static ScoreRepository getInstance() {
        if (instance == null) {
            synchronized (ScoreRepository.class) {
                if (instance == null) {
                    try {
                        instance = new ScoreRepository();
                    } catch (Exception e) {
                        System.err.println("❌ Failed to initialize ScoreRepository: " + e.getMessage());
                        e.printStackTrace();
                        // Create a fallback instance with empty scores
                        instance = new ScoreRepository();
                    }
                }
            }
        }
        return instance;
    }

    private void loadScores() {
        File file = new File(scoresFilePath);
        if (file.exists()) {
            try {
                Score[] scoresArray = objectMapper.readValue(file, Score[].class);
                this.scores = new ArrayList<>(Arrays.asList(scoresArray));
                this.scores.sort((a, b) -> Integer.compare(b.points(), a.points()));
                System.out.println("Loaded " + this.scores.size() + " scores from file");
            } catch (IOException e) {
                System.err.println("❌ Error loading scores: " + e.getMessage());
                System.err.println("🔄 Deleting corrupted scores file...");
                file.delete();
                this.scores = new ArrayList<>();
            }
        } else {
            System.out.println("📝 No existing scores file found, starting fresh");
            this.scores = new ArrayList<>();
        }
    }


    private void saveScores() {
        try {
            File file = new File(scoresFilePath);
            objectMapper.writeValue(file, scores);
            System.out.println("Scores saved to: " + scoresFilePath);
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveScore(Score score) {
        this.scores.add(score);
        this.scores.sort((a, b) -> Integer.compare(b.points(), a.points()));
        if (this.scores.size() > 10) {
            this.scores = this.scores.subList(0, 10);
        }
        saveScores();
    }

    public void saveScore(String playerName, int points) {
        saveScore(new Score(playerName, points));
    }

    public List<Score> getScores() {
        return new ArrayList<>(scores);
    }

    public List<Score> getTopScores(int limit) {
        return scores.stream()
            .limit(limit)
            .toList();
    }

    public List<Score> getTopScores() {
        return getTopScores(10);
    }

    public void clearScores() {
        this.scores.clear();
        saveScores();
    }
}
