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
    private final Stage stage;

    public GameScreen(Stage stage, Game game, Runnable onBack) {
        super(stage);
        this.stage = stage;
        this.game = game;
        this.gamePanel = new GamePanel(game, onBack);

        int windowWidth = 400;   // 10 cols * 40px per cell
        int windowHeight = 700;  // 20 rows * 35px per cell

        StackPane root = new StackPane(gamePanel);
        root.setPrefSize(windowWidth, windowHeight);

        scene = new Scene(root, windowWidth, windowHeight);

        // Set stage size, center
        stage.setWidth(windowWidth);
        stage.setHeight(windowHeight);
        stage.centerOnScreen();
        stage.setResizable(false);
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
