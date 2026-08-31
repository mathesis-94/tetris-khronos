package com.tetriskhronos.view.panels;

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
import com.tetriskhronos.view.renderers.FieldPane;
import com.tetriskhronos.view.renderers.BoardRenderer;
import java.util.Optional;

public class GamePanel extends BorderPane {
    private final Game game;
    private final Runnable onExit;
    private final FieldPane fieldPane;
    private final Label pauseIndicator;
    private GameLoopThread gameLoopThread;
    private boolean showQuitConfirm = false;

    public GamePanel(Game game, Runnable onExit) {
        this.game = game;
        this.onExit = onExit;

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

        // Wrap fieldPane with pause indicator overlay
        StackPane gameField = new StackPane(fieldPane, pauseIndicator);
        StackPane.setAlignment(pauseIndicator, Pos.CENTER);
        gameField.setStyle("-fx-background-color: #1a1a1a;");

        // Make gameField fill available space
        gameField.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        gameField.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Bind fieldPane to fill gameField
        fieldPane.prefWidthProperty().bind(gameField.widthProperty());
        fieldPane.prefHeightProperty().bind(gameField.heightProperty());

        // Wrap in VBox to allow it to grow
        VBox centerBox = new VBox(gameField);
        centerBox.setStyle("-fx-background-color: #1a1a1a;");
        VBox.setVgrow(gameField, Priority.ALWAYS);
        setCenter(centerBox);

        // Bottom: instructions
        Label instructions = new Label("← → Move | ↓ Speed Up | ↑ Rotate | Space Hard Drop | P Pause | ESC Quit to Menu");
        instructions.setTextFill(Color.web("#888888"));
        instructions.setFont(new Font("Arial", 11));
        instructions.setPadding(new Insets(10));
        setBottom(instructions);

        setupKeyboardInput();
    }

    private void setupKeyboardInput() {
        setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            switch (code) {
                case LEFT -> game.moveLeft();
                case RIGHT -> game.moveRight();
                case DOWN -> game.moveDown();
                case UP -> game.rotate();
                case SPACE -> game.hardDrop();
                case P -> {
                    game.togglePause();
                    updatePauseIndicator();
                }
                case ESCAPE -> showQuitConfirm = true;
                default -> {}
            }
            event.consume();
        });
    }

    private void updatePauseIndicator() {
        pauseIndicator.setVisible(game.isPaused());
        fieldPane.render(game);
    }

    public void startGameLoop() {
        if (gameLoopThread == null || !gameLoopThread.isRunning()) {
            gameLoopThread = new GameLoopThread(game, () -> {
                fieldPane.render(game);

                // Handle quit confirmation on FX thread
                if (showQuitConfirm) {
                    Platform.runLater(this::showQuitDialog);
                    showQuitConfirm = false;
                }
            });
            gameLoopThread.start();
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
