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
        int windowWidth = 400;   // 10 cols * 40px per cell
        int windowHeight = 700;  // 20 rows * 35px per cell
        StackPane root = new StackPane(gamePanel);
        root.setPrefSize(windowWidth, windowHeight);
        scene = new Scene(root, windowWidth, windowHeight);
        stage.setWidth(windowWidth);
        stage.setHeight(windowHeight);
        stage.centerOnScreen();
        stage.setResizable(false);
    }

    public GameScreen(Stage stage, Game game, Runnable onBack) {
        this(stage, game, onBack, MusicManager.getInstance());
    }

    @Override
    public void show() {
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
