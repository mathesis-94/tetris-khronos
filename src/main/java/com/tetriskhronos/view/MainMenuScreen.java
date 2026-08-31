package com.tetriskhronos.view;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import com.tetriskhronos.controller.ScreenManager;
import com.tetriskhronos.controller.ScreenType;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class MainMenuScreen extends Screen {
    private final Scene scene;
    private final ScreenManager screenManager;

    public MainMenuScreen(ScreenManager screenManager) {
        super(screenManager.getPrimaryStage());
        this.screenManager = screenManager;

        BorderPane root = new BorderPane();  // Changed from VBox to BorderPane
        root.setStyle("-fx-background-color: #1a1a1a;");

        VBox centerBox = new VBox(20);  // Separate VBox for center content
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

        Label authorLabel = new Label("By Mathesis");
        authorLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11;");
        VBox bottomBox = new VBox(authorLabel);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new javafx.geometry.Insets(15));  // Add import
        root.setBottom(bottomBox);  // Now this works with BorderPane

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
