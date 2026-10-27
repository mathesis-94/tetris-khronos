package com.tetriskhronos.view.panels;

import javafx.scene.control.TextInputDialog;
import com.tetriskhronos.persistence.ScoreRepository;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.tetriskhronos.controller.GameLoopThread;
import com.tetriskhronos.model.Game;
import com.tetriskhronos.model.Configuration;
import com.tetriskhronos.model.Tetromino;
import com.tetriskhronos.ai.SimpleAIPlayer;
import com.tetriskhronos.view.renderers.FieldPane;
import com.tetriskhronos.view.renderers.BoardRenderer;
import com.tetriskhronos.audio.MusicManager;
import com.tetriskhronos.audio.SoundEffects;
import java.util.Optional;

public class GamePanel extends BorderPane {
    private final Game game;
    private final Runnable onExit;
    private final FieldPane fieldPane;
    private final Label pauseIndicator;
    private GameLoopThread gameLoopThread;
    private boolean showQuitConfirm = false;

    // AI Integration
    private SimpleAIPlayer aiPlayer;
    private boolean aiEnabled;
    private long lastAIMoveTime = 0;
    private static final long AI_MOVE_DELAY = 500_000_000; // 500ms in nanoseconds

    public GamePanel(Game game, Runnable onExit) {
        this.game = game;
        this.onExit = onExit;

        // Initialize AI
        this.aiEnabled = Configuration.getInstance().isAiPlayEnabled();
        if (aiEnabled) {
            this.aiPlayer = new SimpleAIPlayer(game);
            System.out.println("AI Player enabled");
        }

        // Calculate cell size dynamically based on window (we'll use default for now)
        int cellSize = 30;
        int boardWidth = game.getConfiguration().getFieldWidth() * cellSize;
        int boardHeight = game.getConfiguration().getFieldHeight() * cellSize;

        this.fieldPane = new FieldPane(boardWidth, boardHeight, new BoardRenderer());

        setStyle("-fx-background-color: #1a1a1a;");
        setFocusTraversable(true);

        // Create pause indicator (green || symbol)
        pauseIndicator = new Label("||");
        pauseIndicator.setTextFill(Color.web("#00FF00"));
        pauseIndicator.setFont(Font.font("Arial", FontWeight.BOLD, 80));
        pauseIndicator.setVisible(false);
        pauseIndicator.setStyle("-fx-text-fill: #00FF00; -fx-font-weight: bold; -fx-font-size: 80;");

        StackPane gameField = new StackPane(fieldPane, pauseIndicator);
        StackPane.setAlignment(pauseIndicator, Pos.CENTER);
        gameField.setStyle("-fx-background-color: #1a1a1a;");

        gameField.setPrefWidth(400);
        gameField.setPrefHeight(700);
        gameField.setMinWidth(400);
        gameField.setMinHeight(700);
        gameField.setMaxWidth(400);
        gameField.setMaxHeight(700);

        // vbox wrap grow
        VBox centerBox = new VBox(gameField);
        centerBox.setStyle("-fx-background-color: #1a1a1a;");
        VBox.setVgrow(gameField, Priority.ALWAYS);
        setCenter(centerBox);

        // Bottom: instructions
        String aiIndicator = aiEnabled ? " | 🤖 AI: ON" : "";
        Label instructions = new Label("← → Move | ↓ Speed Up | ↑ Rotate | Space Hard Drop | P Pause | M Music | S Sound | ESC Quit" + aiIndicator);
        instructions.setTextFill(Color.web("#888888"));
        instructions.setFont(new Font("Arial", 11));
        instructions.setPadding(new Insets(10));
        setBottom(instructions);

        setupKeyboardInput();
    }

    private void setupKeyboardInput() {
        setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            boolean needsRender = false;

            switch (code) {
                case LEFT -> {
                    game.moveLeft();
                    SoundEffects.getInstance().playMove();
                    needsRender = true;
                }
                case RIGHT -> {
                    game.moveRight();
                    SoundEffects.getInstance().playMove();
                    needsRender = true;
                }
                case DOWN -> {
                    game.moveDown();
                    SoundEffects.getInstance().playMove();
                    needsRender = true;
                }
                case UP -> {
                    game.rotate();
                    SoundEffects.getInstance().playRotate();
                    needsRender = true;
                }
                case SPACE -> {
                    game.hardDrop();
                    SoundEffects.getInstance().playHardDrop();
                    needsRender = true;
                }
                case P -> {
                    game.togglePause();
                    updatePauseIndicator();
                }
                case M -> {
                    MusicManager.getInstance().toggleMute();
                    System.out.println(MusicManager.getInstance().isMuted() ? "🔇 Music OFF" : "🔊 Music ON");
                }
                case S -> {
                    SoundEffects.getInstance().toggleMute();
                    System.out.println(SoundEffects.getInstance().isMuted() ? "🔇 Sound Effects OFF" : "🔊 Sound Effects ON");
                }
                case ESCAPE -> showQuitConfirm = true;
                default -> {}
            }

            if (needsRender) {
                Platform.runLater(() -> fieldPane.render(game));
            }

            event.consume();
        });
    }


    private void updatePauseIndicator() {
        pauseIndicator.setVisible(game.isPaused());
        Platform.runLater(() -> fieldPane.render(game));
    }


    public void startGameLoop() {
        if (gameLoopThread == null || !gameLoopThread.isRunning()) {
            gameLoopThread = new GameLoopThread(game, () -> {
                // Execute AI move if enabled
                if (aiEnabled && aiPlayer != null && !game.isPaused() && !game.isGameOver()) {
                    long now = System.nanoTime();
                    if (now - lastAIMoveTime > AI_MOVE_DELAY) {
                        executeAIMove();
                        lastAIMoveTime = now;
                    }
                }

                Platform.runLater(() -> fieldPane.render(game));

                // Check if game is over
                if (game.isGameOver()) {
                    Platform.runLater(this::showGameOverDialog);
                }

                // Handle quit confirmation on FX thread
                if (showQuitConfirm) {
                    Platform.runLater(this::showQuitDialog);
                    showQuitConfirm = false;
                }
            });
            gameLoopThread.start();
        }
    }



    private void executeAIMove() {
        try {
            if (aiPlayer == null) {
                return;
            }

            // Use the existing SimpleAIPlayer API
            String bestMove = aiPlayer.decideBestMove();
            if (bestMove != null) {
                aiPlayer.executeMove(bestMove);

                System.out.println("🤖 AI Move: " + bestMove +
                        ", Score: " + aiPlayer.getScore() +
                        ", Lines: " + aiPlayer.getLines());
            }
        } catch (Exception e) {
            System.err.println("❌ AI Error: " + e.getMessage());
        }
    }


    private void showQuitDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quit to Menu");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to quit to the main menu? Current game progress will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onExit.run();
        }
    }

    private void showGameOverDialog() {
        stopGameLoop();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over!");
        alert.setContentText("Final Score: " + game.getPoints() + "\nLines Cleared: " + game.getLines());

        // Create a dialog with name input
        javafx.scene.control.TextInputDialog nameDialog = new javafx.scene.control.TextInputDialog("Player");
        nameDialog.setTitle("Save Score");
        nameDialog.setHeaderText("Enter your name:");
        nameDialog.setContentText("Name:");

        Optional<String> result = nameDialog.showAndWait();
        if (result.isPresent() && !result.get().isBlank()) {
            String playerName = result.get();
            com.tetriskhronos.persistence.ScoreRepository.getInstance()
                .saveScore(playerName, game.getPoints());
            System.out.println("Score saved: " + playerName + " - " + game.getPoints());
        }

        // Return to menu
        onExit.run();
    }



    public void stopGameLoop() {
        if (gameLoopThread != null && gameLoopThread.isRunning()) {
            gameLoopThread.stopLoop();
            try {
                gameLoopThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
