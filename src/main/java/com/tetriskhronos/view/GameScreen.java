package com.tetriskhronos.view;

import com.tetriskhronos.model.Configuration;
import com.tetriskhronos.model.Game;
import com.tetriskhronos.view.panels.GamePanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GameScreen extends Screen {
    private final Game game;
    private final Scene scene;
    private final GamePanel gamePanel;

    public GameScreen(Stage stage, Game game, Runnable onBack) {
        super(stage);
        this.game = game;
        this.gamePanel = new GamePanel(game, onBack);

        // Use Configuration window dimensions
        Configuration config = game.getConfiguration();
        int windowWidth = config.getWindowWidth();
        int windowHeight = config.getWindowHeight();

        StackPane root = new StackPane(gamePanel);
        root.setPrefSize(windowWidth, windowHeight);

        scene = new Scene(root, windowWidth, windowHeight);

        // Bind the stage size when the scene is shown
        scene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
            if (newWindow != null) {
                newWindow.setWidth(windowWidth);
                newWindow.setHeight(windowHeight);
            }
        });
    }

    @Override
    public void show() {
        game.start();
        gamePanel.startGameLoop();
        gamePanel.requestFocus();
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public void onHide() {
        gamePanel.stopGameLoop();
    }
}
