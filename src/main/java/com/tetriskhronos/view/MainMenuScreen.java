package com.tetriskhronos.view;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import com.tetriskhronos.controller.ScreenManager;
import com.tetriskhronos.controller.ScreenType;
import com.tetriskhronos.networking.ExternalServerClient;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class MainMenuScreen extends Screen {
    private final Scene scene;
    private final ScreenManager screenManager;
    private final ExternalServerClient serverClient;

    public MainMenuScreen(ScreenManager screenManager) {
        super(screenManager.getPrimaryStage());
        this.screenManager = screenManager;
        this.serverClient = new ExternalServerClient();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a1a;");

        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("TETRIS KHRONOS");
        titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 32; -fx-font-weight: bold;");

        Button playButton = new Button("PLAY");
        playButton.setStyle("-fx-font-size: 20; -fx-padding: 10;");
        playButton.setOnAction(e -> screenManager.switchTo(ScreenType.PLAYING));

        Button configButton = new Button("CONFIGURATION");
        configButton.setStyle("-fx-font-size: 20; -fx-padding: 10;");
        configButton.setOnAction(e -> screenManager.switchTo(ScreenType.CONFIG));

        Button scoresButton = new Button("HIGH SCORES");
        scoresButton.setStyle("-fx-font-size: 20; -fx-padding: 10;");
        scoresButton.setOnAction(e -> screenManager.switchTo(ScreenType.HIGH_SCORES));

        Button exitButton = new Button("EXIT");
        exitButton.setStyle("-fx-font-size: 20; -fx-padding: 10;");
        exitButton.setOnAction(e -> System.exit(0));

        centerBox.getChildren().addAll(titleLabel, playButton, configButton, scoresButton, exitButton);
        root.setCenter(centerBox);

        // Bottom section with author and server status
        VBox bottomBox = new VBox(8);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Label authorLabel = new Label("By Mathesis");
        authorLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11;");

        // Server status indicator
        Label serverStatusLabel = new Label();
        boolean serverRunning = serverClient.isServerRunning();

        if (serverRunning) {
            serverStatusLabel.setText("✓ External Server: RUNNING");
            serverStatusLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #00dd00;");
        } else {
            serverStatusLabel.setText("⚠ External Server: NOT RUNNING");
            serverStatusLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #dd8800;");
        }

        bottomBox.getChildren().addAll(authorLabel, serverStatusLabel);
        root.setBottom(bottomBox);

        this.scene = new Scene(root, 600, 700);
    }

    @Override
    public void show() {}

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public void onHide() {}
}
