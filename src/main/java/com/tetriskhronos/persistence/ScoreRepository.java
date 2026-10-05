package com.tetriskhronos.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tetriskhronos.model.Score;

import java.io.File;
import java.io.IOException;
import java.util.*;

// Singleton repo - persisting, retrieving game scores (scores.json - jackson)

public class ScoreRepository {
    private static final ScoreRepository INSTANCE = new ScoreRepository();
    private static final String SCORES_FILE = "scores.json";
    private final ObjectMapper objectMapper;
    private List<Score> scores;

    private ScoreRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.findAndRegisterModules();
        loadScores();
    }

    public static ScoreRepository getInstance() {
        return INSTANCE;
    }

    private void loadScores() {
        File file = new File(SCORES_FILE);
        if (file.exists()) {
            try {
                Score[] scoresArray = objectMapper.readValue(file, Score[].class);
                this.scores = new ArrayList<>(Arrays.asList(scoresArray));
                this.scores.sort((a, b) -> Integer.compare(b.points(), a.points()));
            } catch (IOException e) {
                System.err.println("Error loading scores: " + e.getMessage());
                this.scores = new ArrayList<>();
            }
        } else {
            this.scores = new ArrayList<>();
        }
    }


    private void saveScores() {
        try {
            File file = new File(SCORES_FILE);
            objectMapper.writeValue(file, scores);
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
        }
    }

    // score file persistence
    public void saveScore(Score score) {
        this.scores.add(score);
        this.scores.sort((a, b) -> Integer.compare(b.points(), a.points()));
        if (this.scores.size() > 10) {
            this.scores = this.scores.subList(0, 10);
        }
        saveScores();
    }

    // score getters
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

    // Clear all - testing!!
    public void clearScores() {
        this.scores.clear();
        saveScores();
    }
}
