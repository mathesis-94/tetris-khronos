package com.tetriskhronos.model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import com.tetriskhronos.audio.SoundEffects;
import com.tetriskhronos.networking.ExternalServerClient;
import com.tetriskhronos.model.OpMove;




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

    // External server support
    private ExternalServerClient serverClient;
    private boolean serverAvailable;
    private boolean useExternalPlayer;

    public Game(Configuration config) {
        this.config = config;
        this.board = new Board(config.getFieldWidth(), config.getFieldHeight());
        this.gameState = GameState.IDLE;
        this.random = new Random();
        this.dropCounter = 0;
        this.points = 0;
        this.lines = 0;
        this.serverAvailable = false;
        this.useExternalPlayer = false;
        updateDropSpeed();
        initializeExternalServer();
    }

    /**
     * Initialize connection to external server and check availability
     */
    private void initializeExternalServer() {
        try {
            serverClient = new ExternalServerClient();
            serverAvailable = serverClient.isServerRunning();
            if (serverAvailable) {
                System.out.println("[Game] External server is available at localhost:3000");
            } else {
                System.out.println("[Game] External server not running - external player mode unavailable");
            }
        } catch (Exception e) {
            System.err.println("[Game] Failed to initialize external server client: " + e.getMessage());
            serverAvailable = false;
        }
    }

    /**
     * Check if external server is available
     */
    public boolean isServerAvailable() {
        return serverAvailable;
    }

    /**
     * Set whether to use external player for moves
     */
    public void setUseExternalPlayer(boolean useExternal) {
        if (useExternal && !serverAvailable) {
            System.err.println("[Game] Cannot enable external player - server not running");
            this.useExternalPlayer = false;
        } else {
            this.useExternalPlayer = useExternal;
            System.out.println("[Game] External player mode: " + (useExternal ? "ENABLED" : "DISABLED"));
        }
    }

    public boolean isUsingExternalPlayer() {
        return useExternalPlayer;
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

            if (useExternalPlayer && serverAvailable) {
                try {
                    PureGame pureGame = convertToPureGame();
                    if (pureGame != null) {
                        OpMove optimalMove = serverClient.getOptimalMove(pureGame);
                        if (optimalMove != null) {
                            applyOptimalMove(optimalMove);
                        } else {
                            serverAvailable = false;
                            moveDown(); // Fallback when server returns null
                        }
                    } else {
                        moveDown();
                    }
                } catch (IOException e) {
                    System.err.println("[Game] Error getting optimal move: " + e.getMessage());
                    serverAvailable = false;
                    moveDown();
                }
            } else {
                // Solo play or external player disabled: normal descent
                moveDown();
            }
        }
    }




    /**
     * Request optimal move from external server and apply it
     */
    private void makeExternalMove() {
        try {
            PureGame gameState = convertToPureGame();
            if (gameState == null) {
                moveDown();
                return;
            }

            OpMove move = serverClient.getOptimalMove(gameState);
            if (move != null) {
                applyOptimalMove(move);
            } else {
                System.out.println("[Game] Server returned null move, falling back to moveDown");
                moveDown();
                serverAvailable = false;
            }
        } catch (Exception e) {
            System.err.println("[Game] Error getting move from external server: " + e.getMessage());
            serverAvailable = false;
            moveDown();
        }
    }
    private PureGame convertToPureGame() {
        PureGame game = new PureGame();
        game.setWidth(board.getWidth());
        game.setHeight(board.getHeight());
        game.setCells(board.getGrid());
        game.setCurrentShape(currentPiece.getBlocks());
        game.setNextShape(nextPiece.getBlocks());
        return game;
    }

    private void applyOptimalMove(OpMove move) {
        System.out.println("[Game] Applying optimal move: opX=" + move.opX() +
                         ", opRotate=" + move.opRotate());

        // Apply rotations
        for (int i = 0; i < move.opRotate(); i++) {
            int[][] rotatedBlocks = currentPiece.getRotatedBlocks();
            if (board.canPlaceBlocks(currentPiece.getX(), currentPiece.getY(), rotatedBlocks)) {
                currentPiece.rotate();
            } else {
                System.out.println("[Game] Cannot apply rotation " + (i + 1));
                break;
            }
        }

        // Move to the target X position
        int currentX = currentPiece.getX();
        int targetX = move.opX();

        if (targetX == 0) {
            // Move to left-most position
            while (currentX > 0 &&
                   board.canPlaceBlocks(currentX - 1, currentPiece.getY(), currentPiece.getBlocks())) {
                currentPiece.moveLeft();
                currentX--;
            }
        } else if (targetX < currentX) {
            // Move left
            for (int i = 0; i < currentX - targetX; i++) {
                if (board.canPlaceBlocks(currentPiece.getX() - 1, currentPiece.getY(), currentPiece.getBlocks())) {
                    currentPiece.moveLeft();
                } else {
                    System.out.println("[Game] Cannot move further left");
                    break;
                }
            }
        } else if (targetX > currentX) {
            // Move right
            for (int i = 0; i < targetX - currentX; i++) {
                if (board.canPlaceBlocks(currentPiece.getX() + 1, currentPiece.getY(), currentPiece.getBlocks())) {
                    currentPiece.moveRight();
                } else {
                    System.out.println("[Game] Cannot move further right");
                    break;
                }
            }
        }

        // Drop the piece
        moveDown();
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
            SoundEffects.getInstance().playClearLines();
        }

        if (board.isGameOver()) {
            gameState = GameState.GAME_OVER;
            SoundEffects.getInstance().playGameOver();
        } else {
            spawnNextPiece();
        }
    }

    private void addLinesToScore(int rowsCleared) {
        int lineScore = switch (rowsCleared) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 600;
            case 4 -> 1000;
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
            System.out.println("DEBUG: Game not running, state=" + gameState);
            return;
        }

        System.out.println("DEBUG: Current piece type=" + currentPiece.getType() +
                           ", rotationState=" + currentPiece.getRotationState() +
                           ", x=" + currentPiece.getX() + ", y=" + currentPiece.getY());

        int[][] rotatedBlocks = currentPiece.getRotatedBlocks();
        System.out.println("DEBUG: Rotated blocks to check: " + java.util.Arrays.deepToString(rotatedBlocks));

        boolean canPlace = board.canPlaceBlocks(currentPiece.getX(), currentPiece.getY(), rotatedBlocks);
        System.out.println("DEBUG: canPlaceBlocks result = " + canPlace);

        if (canPlace) {
            currentPiece.rotate();
            System.out.println("DEBUG: Rotation applied. New rotationState=" + currentPiece.getRotationState());
        } else {
            System.out.println("DEBUG: Rotation BLOCKED");
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

    public boolean isGameOver() {
        return gameState == GameState.GAME_OVER;
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
