package com.tetriskhronos.view;

import com.tetriskhronos.controller.ScreenManager;
import com.tetriskhronos.controller.ScreenType;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

public class HighScoreScreen extends Screen {
    private final Scene scene;
    private final ScreenManager screenManager;

    public HighScoreScreen(ScreenManager screenManager) {
        super(screenManager.getPrimaryStage());
        this.screenManager = screenManager;

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1a1a1a;");
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("HIGH SCORES");
        titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28; -fx-font-weight: bold;");

        Label emptyLabel = new Label("No scores yet");
        emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 16;");

        Button backButton = new Button("BACK");
        backButton.setStyle("-fx-font-size: 16; -fx-padding: 10;");
        backButton.setOnAction(e -> screenManager.switchTo(ScreenType.MAIN_MENU));

        root.getChildren().addAll(titleLabel, emptyLabel, backButton);
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
