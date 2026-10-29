package com.tetriskhronos.model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import com.tetriskhronos.audio.SoundEffects;
import com.tetriskhronos.networking.ExternalServerClient;
import com.tetriskhronos.model.OpMove;
import com.tetriskhronos.ai.AIPlayer;
import com.tetriskhronos.ai.Move;

public class Game {
    private final Board board;
    private final Configuration config;
    private GameState gameState;
    private Tetromino currentPiece;
    private Tetromino nextPiece;
    private int dropCounter;
    private int dropSpeed;
    private Random random;
    private AIPlayer aiPlayer;
    private boolean useAIPlayer;
    private boolean aiMoveMadeForCurrentPiece = false;


    private int points;
    private int lines;

    private ExternalServerClient serverClient;
    private boolean serverAvailable;
    private boolean useExternalPlayer;
    private CompletableFuture<OpMove> pendingServerMove;

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
        this.useAIPlayer = false;
        this.aiPlayer = new AIPlayer();
        this.pendingServerMove = null;
        updateDropSpeed();
        initializeExternalServer();
    }

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

    public boolean isServerAvailable() {
        return serverAvailable;
    }


    public void setUseAIPlayer(boolean useAI) {
        this.useAIPlayer = useAI;
        System.out.println("[Game] AI player mode: " + (useAI ? "ENABLED" : "DISABLED"));
    }

    public boolean isUsingAIPlayer() {
        return useAIPlayer;
    }

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
        if (useAIPlayer && config.isAiPlayEnabled()) {
            aiPlayer.startEpisode(board);
        }
    }

    public void spawnNextPiece() {
            if (currentPiece == null) {
                currentPiece = Tetromino.createRandom(random);
            } else {
                currentPiece = nextPiece;
            }
            nextPiece = Tetromino.createRandom(random);

            currentPiece.setX(3);
            currentPiece.setY(0);

            // Reset flag for new piece
            aiMoveMadeForCurrentPiece = false;

            System.out.println("[SPAWN] New piece: type=" + currentPiece.getType() +
                               ", x=" + currentPiece.getX() +
                               ", y=" + currentPiece.getY());

            if (!board.canPlacePiece(currentPiece)) {
                System.out.println("[SPAWN] GAME OVER - Cannot place new piece at spawn");
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

                    // AI player moves - ONLY ONCE per piece
                    if (useAIPlayer && config.isAiPlayEnabled() && !aiMoveMadeForCurrentPiece) {
                        if (currentPiece != null) {
                            Move aiMove = aiPlayer.selectMove(board, currentPiece);
                            System.out.println("[AI] Move decided: rotations=" + aiMove.rotations +
                                             ", targetCol=" + aiMove.targetColumn +
                                             ", currentX=" + currentPiece.getX());
                            applyOptimalMove(aiMove);
                            aiMoveMadeForCurrentPiece = true;  // Mark as done for this piece
                        }
                    }
                    // External server player moves
                    else if (useExternalPlayer && serverAvailable) {
                        if (pendingServerMove != null && pendingServerMove.isDone()) {
                            try {
                                OpMove move = pendingServerMove.get();
                                if (move != null) {
                                    System.out.println("[Game] Applying server move: col=" + move.opX() + ", rotate=" + move.opRotate());
                                    applyOptimalMove(move);
                                } else {
                                    System.out.println("[Game] Server returned null, applying gravity");
                                    moveDown();
                                }
                            } catch (Exception e) {
                                System.err.println("[Game] Error retrieving server move: " + e.getMessage());
                                serverAvailable = false;
                                moveDown();
                            }
                            pendingServerMove = null;
                        } else if (pendingServerMove == null) {
                            System.out.println("[Game] Queuing server request...");
                            requestOptimalMoveAsync();
                            moveDown();
                        } else {
                            System.out.println("[Game] Waiting for server response, applying gravity");
                            moveDown();
                        }
                    }
                    else {
                        moveDown();
                    }
           }
        }


    private void applyOptimalMove(Move move) {
        System.out.println("[Game] Applying AI move: rotations=" + move.rotations +
                         ", targetCol=" + move.targetColumn);

        for (int i = 0; i < move.rotations; i++) {
            currentPiece.rotate();
            if (!board.canPlacePiece(currentPiece)) {
                for (int j = 0; j < 3; j++) {
                    currentPiece.rotate();
                }
                System.out.println("[Game] Cannot apply rotation " + (i + 1));
                break;
            }
        }

        int currentX = currentPiece.getX();
        int targetX = move.targetColumn;

        if (targetX < currentX) {
            for (int i = 0; i < currentX - targetX; i++) {
                currentPiece.moveLeft();
                if (!board.canPlacePiece(currentPiece)) {
                    currentPiece.moveRight();
                    System.out.println("[Game] Cannot move further left");
                    break;
                }
            }
        } else if (targetX > currentX) {
            for (int i = 0; i < targetX - currentX; i++) {
                currentPiece.moveRight();
                if (!board.canPlacePiece(currentPiece)) {
                    currentPiece.moveLeft();
                    System.out.println("[Game] Cannot move further right");
                    break;
                }
            }
        }

        moveDown();
    }

    private void requestOptimalMoveAsync() {
        try {
            PureGame pureGame = toPureGame();
            if (pureGame != null && serverClient != null) {
                pendingServerMove = serverClient.getOptimalMoveAsync(pureGame);
                System.out.println("[Game] Async server request queued");
            } else {
                moveDown();
            }
        } catch (Exception e) {
            System.err.println("[Game] Error queuing async move request: " + e.getMessage());
            serverAvailable = false;
            moveDown();
        }
    }

    public PureGame toPureGame() {
        PureGame pg = new PureGame();
        pg.setWidth(board.getWidth());
        pg.setHeight(board.getHeight());

        int[][] gridCopy = new int[board.getGrid().length][];
        for (int i = 0; i < board.getGrid().length; i++) {
            gridCopy[i] = board.getGrid()[i].clone();
        }
        pg.setCells(gridCopy);

        if (currentPiece != null) {
            pg.setCurrentShape(currentPiece.getShape());
            pg.setCurrentX(currentPiece.getX());
            pg.setCurrentY(currentPiece.getY());
            pg.setCurrentRotation(currentPiece.getRotationState());
            pg.setCurrentType(currentPiece.getType());
        }

        if (nextPiece != null) {
            pg.setNextShape(nextPiece.getShape());
            pg.setNextType(nextPiece.getType());
        }

        pg.setScore(points);
        pg.setLevel(config.getGameLevel());
        pg.setLines(lines);
        pg.setGameTime(System.currentTimeMillis());

        return pg;
    }

    @Deprecated
    private PureGame convertToPureGame() {
        return toPureGame();
    }

    private void applyOptimalMove(OpMove move) {
        System.out.println("[Game] Applying optimal move: opX=" + move.opX() +
                         ", opRotate=" + move.opRotate());

        for (int i = 0; i < move.opRotate(); i++) {
            currentPiece.rotate();
            if (!board.canPlacePiece(currentPiece)) {
                for (int j = 0; j < i + 1; j++) {
                    int rotationState = currentPiece.getRotationState();
                    for (int k = 0; k < 3; k++) {
                        currentPiece.rotate();
                    }
                    if (rotationState == 0) break;
                }
                System.out.println("[Game] Cannot apply rotation " + (i + 1));
                break;
            }
        }

        int currentX = currentPiece.getX();
        int targetX = move.opX();

        if (targetX < currentX) {
            for (int i = 0; i < currentX - targetX; i++) {
                currentPiece.moveLeft();
                if (!board.canPlacePiece(currentPiece)) {
                    currentPiece.moveRight();
                    System.out.println("[Game] Cannot move further left");
                    break;
                }
            }
        } else if (targetX > currentX) {
            for (int i = 0; i < targetX - currentX; i++) {
                currentPiece.moveRight();
                if (!board.canPlacePiece(currentPiece)) {
                    currentPiece.moveLeft();
                    System.out.println("[Game] Cannot move further right");
                    break;
                }
            }
        }

        moveDown();
    }


    public AIPlayer getAIPlayer() {
        return aiPlayer;
    }

    public void reset() {
        board.clear();
        points = 0;
        lines = 0;
        gameState = GameState.IDLE;
        dropCounter = 0;
        currentPiece = null;
        nextPiece = null;
        aiMoveMadeForCurrentPiece = false;
        updateDropSpeed();
    }

    public void moveDown() {
        currentPiece.moveDown();
        if (!board.canPlacePiece(currentPiece)) {
            currentPiece.moveUp();
            System.out.println("[GRAVITY] Piece locked at y=" + currentPiece.getY());
            lockPiece();
        }
    }

    public int checkForLineClears() {
        int[] completeRows = board.getCompleteRows();
        if (completeRows.length > 0) {
            board.clearRows(completeRows);
            lines += completeRows.length;
            addLinesToScore(completeRows.length);
            SoundEffects.getInstance().playClearLines();
            return completeRows.length;
        }
        return 0;
    }

    private void lockPiece() {
        System.out.println("\n[LOCK] ========== LOCKING PIECE ==========");
        System.out.println("[LOCK] Piece: type=" + currentPiece.getType() +
                           ", x=" + currentPiece.getX() +
                           ", y=" + currentPiece.getY());

        board.placePiece(currentPiece);
        printBoard();

        int[] completeRows = board.getCompleteRows();
        int linesCleared = 0;
        if (completeRows.length > 0) {
            board.clearRows(completeRows);
            lines += completeRows.length;
            linesCleared = completeRows.length;
            addLinesToScore(completeRows.length);
            SoundEffects.getInstance().playClearLines();
            System.out.println("[LOCK] CLEARED! Total lines: " + lines + ", Score: " + points);

            // Notify AI of reward after line clear
            if (useAIPlayer && config.isAiPlayEnabled()) {
                aiPlayer.updateAfterMove(board, linesCleared);
            }
        } else {
            System.out.println("[LOCK] No complete rows found");
        }

        if (board.isGameOver()) {
            System.out.println("[LOCK] GAME OVER detected");
            gameState = GameState.GAME_OVER;
            SoundEffects.getInstance().playGameOver();

            // End current AI episode with final score
            if (useAIPlayer && config.isAiPlayEnabled()) {
                aiPlayer.endGame(points);
            }
        } else {
            spawnNextPiece();
        }
        System.out.println("[LOCK] ====================================\n");
    }


    private void printBoard() {
        int[][] grid = board.getGrid();
        System.out.println("[Game] ========== FULL BOARD STATE (20 rows) ==========");
        for (int r = 0; r < grid.length; r++) {
            System.out.print("  Row " + String.format("%2d", r) + ": ");
            for (int c = 0; c < grid[r].length; c++) {
                System.out.print(grid[r][c] > 0 ? "█" : "·");
            }
            boolean isFull = true;
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                System.out.print("  FULL ROW");
            }
            System.out.println();
        }
        System.out.println("[Game] ===============================================");
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
        currentPiece.moveLeft();
        if (!board.canPlacePiece(currentPiece)) {
            currentPiece.moveRight();
        }
    }

    public void moveRight() {
        if (gameState != GameState.RUNNING) {
            return;
        }
        currentPiece.moveRight();
        if (!board.canPlacePiece(currentPiece)) {
            currentPiece.moveLeft();
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

        currentPiece.rotate();
        System.out.println("DEBUG: Attempted rotation. New rotationState=" + currentPiece.getRotationState());

        if (!board.canPlacePiece(currentPiece)) {
            for (int i = 0; i < 3; i++) {
                currentPiece.rotate();
            }
            System.out.println("DEBUG: Rotation BLOCKED - reverted");
        } else {
            System.out.println("DEBUG: Rotation applied successfully");
        }
    }

    public void hardDrop() {
        if (gameState != GameState.RUNNING) {
            return;
        }
        while (true) {
            currentPiece.moveDown();
            if (!board.canPlacePiece(currentPiece)) {
                currentPiece.moveUp();
                break;
            }
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

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
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

    public int getScore() {
        return points;
    }

    public void clear() {
        board.clear();
    }
    public Configuration getConfiguration() {
        return config;
    }
}
