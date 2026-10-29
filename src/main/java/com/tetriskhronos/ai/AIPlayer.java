package com.tetriskhronos.ai;

import com.tetriskhronos.model.Board;
import com.tetriskhronos.model.Tetromino;
import java.io.IOException;
import java.util.Random;

public class AIPlayer {
    private static final double INITIAL_EPSILON = 0.3;
    private static final double EPSILON_DECAY = 0.9995;
    private static final double MIN_EPSILON = 0.01;
    private static final double LEARNING_RATE = 0.01;
    private static final double DISCOUNT_FACTOR = 0.99;
    private static final double LAMBDA = 0.95;
    private static final double GRADIENT_CLIP = 10.0;

    private static final double LINE_REWARD = 100.0;
    private static final double TETRIS_BONUS = 200.0;
    private static final double HEIGHT_PENALTY = 5.0;
    private static final double HOLE_PENALTY = 10.0;
    private static final double BUMPINESS_PENALTY = 1.0;
    private static final double SURVIVAL_BONUS = 1.0;
    private static final int SAFE_HEIGHT = 15;

    private static final int SAVE_INTERVAL = 10;
    private static final int LOG_THRESHOLD = 3;

    private static final String WEIGHTS_PATH = System.getProperty("user.home") + "/.tetriskhronos/neural_weights.dat";
    private static final String STATS_PATH = System.getProperty("user.home") + "/.tetriskhronos/feature_stats.dat";

    private final NeuralNetworkEvaluator evaluator;
    private final TemporalDifferenceLearner tdLearner;
    private final Random random;

    private BoardFeatures boardFeatures;
    private double[] previousFeatures;
    private double previousStateValue;
    private double previousHeightRaw;
    private double previousHoleCountRaw;

    private double epsilon;
    private int gameCount;
    private boolean isEpisodeActive;

    public AIPlayer() {
        this.evaluator = new NeuralNetworkEvaluator();
        this.tdLearner = new TemporalDifferenceLearner(evaluator, LEARNING_RATE, DISCOUNT_FACTOR, LAMBDA, GRADIENT_CLIP);
        this.random = new Random();
        this.epsilon = INITIAL_EPSILON;
        this.gameCount = 0;
        this.isEpisodeActive = false;

        loadWeightsAndStatistics();
    }

    private void loadWeightsAndStatistics() {
        try {
            evaluator.loadWeights(WEIGHTS_PATH);
            log("Loaded pre-trained weights");
        } catch (IOException | ClassNotFoundException e) {
            log("Starting with fresh neural network weights");
        }

        this.boardFeatures = new BoardFeatures(null);
        try {
            boardFeatures.loadStatistics(STATS_PATH);
            log("Loaded feature statistics");
        } catch (IOException | ClassNotFoundException e) {
            log("Starting with fresh feature statistics");
        }
    }

    public void startEpisode(Board board) {
        if (isEpisodeActive) {
            return;
        }

        this.boardFeatures = new BoardFeatures(board);
        double[] rawFeatures = boardFeatures.getRawFeatureVector();
        double[] normalizedFeatures = boardFeatures.normalizeFeatures(rawFeatures);

        this.previousFeatures = normalizedFeatures;
        this.previousStateValue = evaluator.evaluate(normalizedFeatures);
        this.previousHeightRaw = rawFeatures[0];
        this.previousHoleCountRaw = rawFeatures[4];

        tdLearner.initializeEpisode(normalizedFeatures);
        this.isEpisodeActive = true;

        log("Episode started. Initial state value: " + formatValue(previousStateValue));
    }

    public Move selectMove(Board board, Tetromino currentPiece) {
        if (!isEpisodeActive) {
            startEpisode(board);
        }

        return random.nextDouble() < epsilon ?
            selectRandomMove(board, currentPiece) :
            selectOptimalMove(board, currentPiece);
    }

    public void updateAfterMove(Board board, int linesCleared) {
        if (!isEpisodeActive) {
            return;
        }

        this.boardFeatures = new BoardFeatures(board);
        double[] rawFeatures = boardFeatures.getRawFeatureVector();
        double[] normalizedFeatures = boardFeatures.normalizeFeatures(rawFeatures);
        double currentStateValue = evaluator.evaluate(normalizedFeatures);

        double reward = calculateReward(rawFeatures, linesCleared);
        tdLearner.processTransition(previousFeatures, reward, normalizedFeatures, false);

        this.previousFeatures = normalizedFeatures;
        this.previousStateValue = currentStateValue;
        this.previousHeightRaw = rawFeatures[0];
        this.previousHoleCountRaw = rawFeatures[4];

        if (linesCleared > 0) {
            log("Cleared " + linesCleared + " line(s), reward: " + formatValue(reward));
        }
    }

    public void endGame(int finalScore) {
        if (!isEpisodeActive) {
            return;
        }

        double[] terminalFeatures = new double[28];
        tdLearner.processTransition(previousFeatures, 0.0, terminalFeatures, true);

        gameCount++;
        epsilon = Math.max(MIN_EPSILON, epsilon * EPSILON_DECAY);

        log("Game " + gameCount + " ended. Score: " + finalScore + ", Epsilon: " + formatValue(epsilon));

        if (gameCount % SAVE_INTERVAL == 0) {
            saveWeights();
        }

        this.isEpisodeActive = false;
    }

    public void saveWeights() {
        try {
            evaluator.saveWeights(WEIGHTS_PATH);
            if (boardFeatures != null) {
                boardFeatures.saveStatistics(STATS_PATH);
            }
            log("Saved weights and statistics after " + gameCount + " games");
        } catch (IOException e) {
            logError("Failed to save weights/stats: " + e.getMessage());
        }
    }

    private Move selectRandomMove(Board board, Tetromino piece) {
        int column = random.nextInt(board.getWidth());
        int rotation = random.nextInt(4);
        log("Random move selected: rotation=" + rotation + ", column=" + column);
        return new Move(rotation, column);
    }

    private Move selectOptimalMove(Board board, Tetromino piece) {
        MoveEvaluation bestEval = new MoveEvaluation();

        for (int rotation = 0; rotation < 4; rotation++) {
            for (int column = 0; column < board.getWidth(); column++) {
                evaluateCandidateMove(board, piece, rotation, column, bestEval);
            }
        }

        if (bestEval.validCount == 0) {
            log("WARNING: No valid moves found! Using fallback");
            return new Move(0, piece.getX());
        }

        log("Best move: rotation=" + bestEval.bestRotation +
            ", column=" + bestEval.bestColumn +
            ", value=" + formatValue(bestEval.bestValue) +
            ", valid=" + bestEval.validCount);

        return new Move(bestEval.bestRotation, bestEval.bestColumn);
    }

    private void evaluateCandidateMove(Board board, Tetromino piece, int rotation, int column, MoveEvaluation eval) {
        try {
            Board simulation = new Board(board);
            Tetromino testPiece = clonePiece(piece);

            if (!rotatePiece(testPiece, rotation, simulation)) {
                return;
            }

            movePieceToColumn(testPiece, column, simulation);
            dropPieceToBottom(testPiece, simulation);

            if (!testPiece.canPlaceAt(testPiece.getX(), testPiece.getY(), simulation)) {
                return;
            }

            simulation.placePiece(testPiece);

            BoardFeatures features = new BoardFeatures(simulation);
            double[] rawFeatures = features.getRawFeatureVector();
            double[] normalizedFeatures = boardFeatures.normalizeFeatures(rawFeatures);
            double value = evaluator.evaluate(normalizedFeatures);

            if (validateValue(value, rotation, column)) {
                eval.updateIfBetter(rotation, column, value);
            }
        } catch (Exception e) {
            logError("Exception at rotation=" + rotation + ", column=" + column + ": " + e.getMessage());
        }
    }

    private boolean rotatePiece(Tetromino piece, int rotations, Board board) {
        for (int i = 0; i < rotations; i++) {
            if (!piece.canPlaceAt(piece.getX(), piece.getY(), board)) {
                return false;
            }
            piece.rotate();
        }
        return true;
    }

    private void movePieceToColumn(Tetromino piece, int targetColumn, Board board) {
        int distance = targetColumn - piece.getX();
        if (distance > 0) {
            for (int i = 0; i < distance; i++) {
                if (piece.canPlaceAt(piece.getX() + 1, piece.getY(), board)) {
                    piece.moveRight();
                }
            }
        } else if (distance < 0) {
            for (int i = 0; i < Math.abs(distance); i++) {
                if (piece.canPlaceAt(piece.getX() - 1, piece.getY(), board)) {
                    piece.moveLeft();
                }
            }
        }
    }

    private void dropPieceToBottom(Tetromino piece, Board board) {
        while (piece.canPlaceAt(piece.getX(), piece.getY() + 1, board)) {
            piece.moveDown();
        }
    }

    private Tetromino clonePiece(Tetromino original) {
        Tetromino clone = null;
        int attempts = 0;
        final int maxAttempts = 100;

        while (attempts++ < maxAttempts) {
            clone = Tetromino.createRandom(random);
            if (clone.getType() == original.getType()) {
                break;
            }
        }

        if (clone == null || clone.getType() != original.getType()) {
            logError("Failed to clone piece type " + original.getType() + ". Using original.");
            return original;
        }

        clone.setX(original.getX());
        clone.setY(original.getY());
        for (int i = 0; i < original.getRotationState(); i++) {
            clone.rotate();
        }

        return clone;
    }

    private boolean validateValue(double value, int rotation, int column) {
        if (Double.isNaN(value)) {
            logWarning("NaN at rotation=" + rotation + ", column=" + column);
            return false;
        }
        if (Double.isInfinite(value)) {
            logWarning("Infinite at rotation=" + rotation + ", column=" + column);
            return false;
        }
        return true;
    }

    private double calculateReward(double[] rawFeatures, int linesCleared) {
        double reward = 0.0;

        reward += calculateLineReward(linesCleared);
        reward -= calculateHeightPenalty(rawFeatures[0]);
        reward -= calculateHolePenalty(rawFeatures[4]);
        reward -= rawFeatures[6] * BUMPINESS_PENALTY;
        reward += calculateSurvivalBonus(rawFeatures[0]);

        return reward;
    }

    private double calculateLineReward(int linesCleared) {
        double reward = linesCleared * LINE_REWARD;
        if (linesCleared >= 4) {
            reward += TETRIS_BONUS;
        }
        return reward;
    }

    private double calculateHeightPenalty(double currentHeight) {
        double increase = Math.max(0, currentHeight - previousHeightRaw);
        return increase * HEIGHT_PENALTY;
    }

    private double calculateHolePenalty(double currentHoles) {
        double increase = Math.max(0, currentHoles - previousHoleCountRaw);
        return increase * HOLE_PENALTY;
    }

    private double calculateSurvivalBonus(double currentHeight) {
        return currentHeight <= SAFE_HEIGHT ? SURVIVAL_BONUS : 0.0;
    }

    private void log(String message) {
        System.out.println("[AIPlayer] " + message);
    }

    private void logWarning(String message) {
        System.out.println("[AIPlayer WARNING] " + message);
    }

    private void logError(String message) {
        System.err.println("[AIPlayer ERROR] " + message);
    }

    private String formatValue(double value) {
        return String.format("%.4f", value);
    }

    public int getGameCount() {
        return gameCount;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public boolean isEpisodeActive() {
        return isEpisodeActive;
    }

    private static class MoveEvaluation {
        int bestRotation = 0;
        int bestColumn = 0;
        double bestValue = Double.NEGATIVE_INFINITY;
        int validCount = 0;
        private int nanWarningCount = 0;

        void updateIfBetter(int rotation, int column, double value) {
            if (value > bestValue) {
                bestValue = value;
                bestRotation = rotation;
                bestColumn = column;
            }
            validCount++;
        }
    }
}
