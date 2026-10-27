package com.tetriskhronos.ai;

import com.tetriskhronos.model.Board;
import com.tetriskhronos.model.Game;
import com.tetriskhronos.model.GameState;


public class SimpleAIPlayer {
    private final Game game;
    private int moveDelayMs = 150;
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
        System.out.println("SimpleAIPlayer started");
    }

    public void stopPlaying() {
        running = false;
        System.out.println("SimpleAIPlayer stopped");
    }

    public boolean isThinking() {
        return thinking;
    }

    private void playLoop() {
        while (running && game.getGameState() == GameState.RUNNING) {
            try {
                thinking = true;

                AIMove bestMove = findBestMove();
                if (bestMove != null) {
                    executeMoveSequence(bestMove);
                }

                thinking = false;
                Thread.sleep(moveDelayMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("AI game loop ended. Final score: " + game.getPoints());
    }

    /**
     * Evaluates all possible placements (rotations × columns)
     * and returns the best one
     */
    public AIMove findBestMove() {
        Board board = game.getBoard();
        int bestScore = Integer.MIN_VALUE;
        AIMove bestMove = null;

        // Try all 4 rotation states
        for (int rotation = 0; rotation < 4; rotation++) {
            // Try all column positions
            for (int col = 0; col < board.getWidth(); col++) {
                int score = evaluatePlacement(rotation, col);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new AIMove(rotation, col, score);
                }
            }
        }

        if (bestMove != null) {
            System.out.println("AI Move: rotation=" + bestMove.rotation +
                             ", col=" + bestMove.column + ", score=" + bestMove.score);
        }
        return bestMove;
    }

    /**
     * Simulates placing the piece at a given rotation and column,
     * then evaluates the resulting board
     */
    private int evaluatePlacement(int targetRotation, int targetColumn) {
        try {
            // Create a copy of the board for simulation
            Board testBoard = copyBoard(game.getBoard());
            int currentX = game.getCurrentPiece().getX();
            int currentY = game.getCurrentPiece().getY();
            int currentRotation = game.getCurrentPiece().getRotationState();
            int[][] currentBlocks = game.getCurrentPiece().getBlocks();

            // Simulate rotating to target rotation
            int[][] blocks = currentBlocks;
            for (int i = 0; i < targetRotation - currentRotation; i++) {
                blocks = game.getCurrentPiece().getRotatedBlocks();
            }

            // Check if placement is valid
            if (!testBoard.canPlaceBlocks(targetColumn, currentY, blocks)) {
                return Integer.MIN_VALUE;  // Invalid placement
            }

            // Find drop position
            int dropY = currentY;
            while (testBoard.canPlaceBlocks(targetColumn, dropY + 1, blocks)) {
                dropY++;
            }

            // Place the piece on test board
            for (int[] block : blocks) {
                int row = dropY + block[0];
                int col = targetColumn + block[1];
                if (row >= 0 && row < testBoard.getHeight() &&
                    col >= 0 && col < testBoard.getWidth()) {
                    testBoard.setCell(row, col, 1);
                }
            }

            // Clear lines and count them
            int linesCleared = testBoard.clearRows();

            // Evaluate the resulting board
            int score = evaluateBoard(testBoard, linesCleared);
            return score;

        } catch (Exception e) {
            System.err.println("Error evaluating placement: " + e.getMessage());
            return Integer.MIN_VALUE;
        }
    }

    /**
     * Scores a board state based on:
     * - Lines cleared (highest weight)
     * - Holes (heavily penalized)
     * - Height (lower is better)
     * - Bumpiness (uneven surfaces penalized)
     */
    private int evaluateBoard(Board board, int linesCleared) {
        int score = 0;

        // Reward line clears heavily
        score += linesCleared * 1000;

        // Penalize holes
        int holes = countHoles(board);
        score -= holes * 500;

        // Penalize height
        int height = calculateBoardHeight(board);
        score -= height * 1;

        // Penalize bumpiness (uneven surface)
        int bumpiness = calculateBumpiness(board);
        score -= bumpiness * 50;

        return score;
    }

    /**
     * Counts holes: filled cells with empty cells below them
     */
    private int countHoles(Board board) {
        int holes = 0;
        for (int col = 0; col < board.getWidth(); col++) {
            boolean foundFilled = false;
            for (int row = 0; row < board.getHeight(); row++) {
                if (!board.isCellEmpty(row, col)) {
                    foundFilled = true;
                } else if (foundFilled && board.isCellEmpty(row, col)) {
                    holes++;
                }
            }
        }
        return holes;
    }

    /**
     * Calculates the height of the highest filled cell
     */
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

    /**
     * Calculates bumpiness: sum of height differences between adjacent columns
     */
    private int calculateBumpiness(Board board) {
        int[] columnHeights = new int[board.getWidth()];

        for (int col = 0; col < board.getWidth(); col++) {
            for (int row = 0; row < board.getHeight(); row++) {
                if (!board.isCellEmpty(row, col)) {
                    columnHeights[col] = board.getHeight() - row;
                    break;
                }
            }
        }

        int bumpiness = 0;
        for (int i = 0; i < columnHeights.length - 1; i++) {
            bumpiness += Math.abs(columnHeights[i] - columnHeights[i + 1]);
        }
        return bumpiness;
    }

    /**
     * Creates a deep copy of the board for simulation
     */
    private Board copyBoard(Board original) {
        Board copy = new Board(original.getWidth(), original.getHeight());
        for (int row = 0; row < original.getHeight(); row++) {
            for (int col = 0; col < original.getWidth(); col++) {
                if (!original.isCellEmpty(row, col)) {
                    copy.setCell(row, col, 1);
                }
            }
        }
        return copy;
    }

    /**
     * Executes the move sequence: rotations, then horizontal movement, then drop
     */
    public void executeMoveSequence(AIMove move) {
        try {
            // Rotate to target rotation
            int currentRotation = game.getCurrentPiece().getRotationState();
            for (int i = 0; i < move.rotation - currentRotation; i++) {
                game.rotate();
                game.update();
                Thread.sleep(50);
            }

            // Move horizontally to target column
            int currentX = game.getCurrentPiece().getX();
            while (currentX < move.column) {
                game.moveRight();
                game.update();
                currentX++;
                Thread.sleep(30);
            }
            while (currentX > move.column) {
                game.moveLeft();
                game.update();
                currentX--;
                Thread.sleep(30);
            }

            // Hard drop
            game.hardDrop();
            game.update();

        } catch (Exception e) {
            System.err.println("Error executing move: " + e.getMessage());
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

    /**
     * Helper class to store AI move decision
     */
    public static class AIMove {
        public int rotation;
        public int column;
        public int score;

        public AIMove(int rotation, int column, int score) {
            this.rotation = rotation;
            this.column = column;
            this.score = score;
        }
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
