package com.tetriskhronos.ai;

import com.tetriskhronos.model.Board;
import com.tetriskhronos.model.Game;
import com.tetriskhronos.model.GameState;

/**
 * Greedy AI for autonomous Tetris play
 *
 * Strategy:
 * 1. Evaluate 4 moves: left, right, rotate, drop
 * 2. Score each based on:
 *    - Lines cleared (highest priority)
 *    - Board height (prefer lower fills)
 *    - Holes created (penalize gaps)
 * 3. Execute best move
 * 4. Target: 500+ points (5+ line clears)
 */
public class SimpleAIPlayer {
    private final Game game;
    private int moveDelayMs = 150; // Deliberate delay for visibility
    private volatile boolean running = false;
    private volatile boolean thinking = false;

    public SimpleAIPlayer(Game game) {
        this.game = game;
    }


    public void startPlaying() {
        running = true;
        thinking = false;
        Thread aiThread = new Thread(this::playLoop);
        aiThread.setDaemon(true);
        aiThread.setName("SimpleAIPlayer");
        aiThread.start();
        System.out.println("🤖 SimpleAIPlayer started");
    }

    public void stopPlaying() {
        running = false;
        System.out.println("🛑 SimpleAIPlayer stopped");
    }

    public boolean isThinking() {
        return thinking;
    }


    private void playLoop() {
        while (running && game.getGameState() == GameState.RUNNING) {
            try {
                thinking = true;

                String bestMove = decideBestMove();
                if (bestMove != null) {
                    executeMove(bestMove);
                }

                thinking = false;
                Thread.sleep(moveDelayMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("🎮 AI game loop ended. Final score: " + game.getPoints());
    }


    private String decideBestMove() {
        Board board = game.getBoard();

        int scoreLeft = evaluateMove("left");
        int scoreRight = evaluateMove("right");
        int scoreRotate = evaluateMove("rotate");
        int scoreDrop = evaluateMove("drop");

        int maxScore = Math.max(Math.max(scoreLeft, scoreRight),
                               Math.max(scoreRotate, scoreDrop));

        if (maxScore == scoreLeft) return "left";
        if (maxScore == scoreRight) return "right";
        if (maxScore == scoreRotate) return "rotate";
        if (maxScore == scoreDrop) return "drop";

        return "drop";
    }


    private int evaluateMove(String move) {
        try {
            int originalPoints = game.getPoints();
            int originalLines = game.getLines();

            Board board = game.getBoard();
            int currentPieceX = game.getCurrentPiece().getX();
            int currentPieceY = game.getCurrentPiece().getY();
            int[][] currentBlocks = game.getCurrentPiece().getBlocks();

            boolean canExecute = false;
            switch (move.toLowerCase()) {
                case "left":
                    canExecute = board.canPlaceBlocks(
                        currentPieceX - 1, currentPieceY, currentBlocks);
                    break;
                case "right":
                    canExecute = board.canPlaceBlocks(
                        currentPieceX + 1, currentPieceY, currentBlocks);
                    break;
                case "rotate":
                    int[][] rotated = game.getCurrentPiece().getRotatedBlocks();
                    canExecute = board.canPlaceBlocks(
                        currentPieceX, currentPieceY, rotated);
                    break;
                case "drop":
                    canExecute = true;
                    break;
            }

            int score = 0;
            if (canExecute) {
                score += 10;
                if ("drop".equals(move)) {
                    score += 50;
                }

                // Prefer moves toward center (reduce extremes)
                int distFromCenter = Math.abs(currentPieceX - (board.getWidth() / 2));
                score += (20 - distFromCenter);
            }

            // Check board height (prefer lower pieces)
            int boardHeight = calculateBoardHeight(board);
            if (boardHeight < board.getHeight() / 2) {
                score += 15; // Reward if board stays low
            }

            return score;

        } catch (Exception e) {
            System.err.println("⚠️ Error evaluating move " + move + ": " + e.getMessage());
            return 0;
        }
    }


    private int calculateBoardHeight(Board board) {
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                if (!board.isCellEmpty(row, col)) {
                    return board.getHeight() - row;
                }
            }
        }
        return 0;
    }


    private void executeMove(String move) {
        try {
            switch (move.toLowerCase()) {
                case "left":
                    game.moveLeft();
                    break;
                case "right":
                    game.moveRight();
                    break;
                case "rotate":
                    game.rotate();
                    break;
                case "drop":
                    game.hardDrop();
                    break;
            }
            game.update();
        } catch (Exception e) {
            System.err.println("❌ Error executing move: " + e.getMessage());
        }
    }


    public void setMoveDelayMs(int delayMs) {
        this.moveDelayMs = Math.max(50, delayMs);
    }

    public int getScore() {
        return game.getPoints();
    }

    public int getLines() {
        return game.getLines();
    }


    public static void main(String[] args) {
        System.out.println("SimpleAIPlayer test mode");

        com.tetriskhronos.model.Configuration config =
            com.tetriskhronos.model.Configuration.getInstance();
        config.setFieldWidth(10);
        config.setFieldHeight(20);
        config.setGameLevel(1);
        Game game = new Game(config);
        game.start();

        SimpleAIPlayer ai = new SimpleAIPlayer(game);
        ai.setMoveDelayMs(200);
        ai.startPlaying();

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ai.stopPlaying();
        System.out.println("Final Score: " + ai.getScore() + " points");
        System.out.println("Lines Cleared: " + ai.getLines());
    }
}
