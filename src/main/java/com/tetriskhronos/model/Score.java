package com.tetriskhronos.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/* score record */
public record Score(
    @JsonProperty("playerName")
    String playerName,

    @JsonProperty("points")
    int points,

    @JsonProperty("timestamp")
    LocalDateTime timestamp
) {
    /*  constructor - validation */
    public Score {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be null or blank");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
    }

    /* Create timestamped score */
    public Score(String playerName, int points) {
        this(playerName, points, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "%s - %d points".formatted(playerName, points);
    }
}
