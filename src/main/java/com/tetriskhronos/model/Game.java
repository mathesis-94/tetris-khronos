package com.tetriskhronos.model;

import java.time.LocalDateTime;
import java.util.Random;

public class Game {
    private final Board board;
    private final Configuration config;
    private GameState gameState;
    private Tetromino currentPiece;
    private Tetromino nextPiece;
    private int dropCounter;
    private int dropSpeed;
    private Random random;

    private int points;
    private int lines;

    public Game(Configuration config) {
        this.config = config;
        this.board = new Board(config.getFieldHeight(), config.getFieldWidth());
        this.gameState = GameState.IDLE;
        this.random = new Random();
        this.dropCounter = 0;
        this.points = 0;
        this.lines = 0;
        updateDropSpeed();
    }

    private void updateDropSpeed() {
        this.dropSpeed = Math.max(10, 60 - (config.getGameLevel() - 1) * 5);
    }

    public void start() {
        board.clear();
        points = 0;
        lines = 0;
        gameState = GameState.RUNNING;
        dropCounter = 0;
        spawnNextPiece();
        spawnNextPiece();
    }

    private void spawnNextPiece() {
        if (currentPiece == null) {
            currentPiece = Tetromino.createRandom(random);
        } else {
            currentPiece = nextPiece;
        }
        nextPiece = Tetromino.createRandom(random);

        if (!board.canPlaceBlocks(currentPiece.getX(), currentPiece.getY(), currentPiece.getBlocks())) {
            gameState = GameState.GAME_OVER;
        }
    }

    public void update() {
        if (gameState != GameState.RUNNING) {
            return;
        }

        dropCounter++;
        if (dropCounter >= dropSpeed) {
            dropCounter = 0;
            moveDown();
        }
    }

    public void moveDown() {
        int nextY = currentPiece.getY() + 1;
        if (board.canPlaceBlocks(currentPiece.getX(), nextY, currentPiece.getBlocks())) {
            currentPiece.moveDown();
        } else {
            lockPiece();
        }
    }

    private void lockPiece() {
        board.placeBlocks(currentPiece.getX(), currentPiece.getY(), currentPiece.getBlocks(), currentPiece.getType());

        int rowsCleared = board.clearRows();
        if (rowsCleared > 0) {
            lines += rowsCleared;
            addLinesToScore(rowsCleared);
        }

        if (board.isGameOver()) {
            gameState = GameState.GAME_OVER;
        } else {
            spawnNextPiece();
        }
    }

    private void addLinesToScore(int rowsCleared) {
        int lineScore = switch (rowsCleared) {
            case 1 -> 40;
            case 2 -> 100;
            case 3 -> 300;
            case 4 -> 1200;
            default -> 0;
        };
        points += lineScore;
    }

    public void moveLeft() {
        if (gameState != GameState.RUNNING) {
            return;
        }
        int nextX = currentPiece.getX() - 1;
        if (board.canPlaceBlocks(nextX, currentPiece.getY(), currentPiece.getBlocks())) {
            currentPiece.moveLeft();
        }
    }

    public void moveRight() {
        if (gameState != GameState.RUNNING) {
            return;
        }
        int nextX = currentPiece.getX() + 1;
        if (board.canPlaceBlocks(nextX, currentPiece.getY(), currentPiece.getBlocks())) {
            currentPiece.moveRight();
        }
    }

    public void rotate() {
        if (gameState != GameState.RUNNING) {
            return;
        }
        int[][] rotatedBlocks = currentPiece.getRotatedBlocks();
        if (board.canPlaceBlocks(currentPiece.getX(), currentPiece.getY(), rotatedBlocks)) {
            currentPiece.rotate();
        }
    }

    public void hardDrop() {
        if (gameState != GameState.RUNNING) {
            return;
        }
        while (board.canPlaceBlocks(currentPiece.getX(), currentPiece.getY() + 1, currentPiece.getBlocks())) {
            currentPiece.moveDown();
        }
        lockPiece();
    }

    public void togglePause() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING;
        }
    }

    public boolean isPaused() {
        return gameState == GameState.PAUSED;
    }

    public Board getBoard() {
        return board;
    }

    public int getPoints() {
        return points;
    }

    public int getLines() {
        return lines;
    }

    public Score createScoreRecord(String playerName) {
        return new Score(playerName, points, LocalDateTime.now());
    }

    public GameState getGameState() {
        return gameState;
    }

    public Tetromino getCurrentPiece() {
        return currentPiece;
    }

    public Tetromino getNextPiece() {
        return nextPiece;
    }

    public int getLevel() {
        return config.getGameLevel();
    }

    public Configuration getConfiguration() {
        return config;
    }
}
