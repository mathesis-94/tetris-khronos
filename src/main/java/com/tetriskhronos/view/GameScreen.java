package com.tetriskhronos.view;

import com.tetriskhronos.model.Configuration;
import com.tetriskhronos.model.Game;
import com.tetriskhronos.view.panels.GamePanel;
import com.tetriskhronos.audio.MusicManager;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GameScreen extends Screen {
    private final Game game;
    private final Scene scene;
    private final GamePanel gamePanel;
    private final Stage stage;
    private final MusicManager musicManager;

    public GameScreen(Stage stage, Game game, Runnable onBack, MusicManager musicManager) {
        super(stage);
        this.stage = stage;
        this.game = game;
        this.musicManager = musicManager;
        this.gamePanel = new GamePanel(game, onBack);

        int windowWidth = 500;
        int windowHeight = 750;

        StackPane root = new StackPane(gamePanel);
        root.setStyle("-fx-background-color: #1a1a1a;");

        scene = new Scene(root, windowWidth, windowHeight);

        stage.setWidth(windowWidth);
        stage.setHeight(windowHeight);
        stage.setMinWidth(400);
        stage.setMinHeight(600);
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    public GameScreen(Stage stage, Game game, Runnable onBack) {
        this(stage, game, onBack, MusicManager.getInstance());
    }

    @Override
    public void show() {
        // Enable AI player if configured
        if (Configuration.getInstance().isAiPlayEnabled()) {
            game.setUseAIPlayer(true);
        }

        game.start();
        musicManager.play();
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
