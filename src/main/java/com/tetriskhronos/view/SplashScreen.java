package com.tetriskhronos.view;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import com.tetriskhronos.controller.ScreenManager;
import com.tetriskhronos.controller.ScreenType;

public class SplashScreen extends Screen {

    private ScreenManager screenManager;

    public SplashScreen(Stage stage, ScreenManager screenManager) {
        super(stage);
        this.screenManager = screenManager;
    }

    @Override
    public void show() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a1a;");

        try {
            // Load image from resources
            Image splashImage = new Image(
                getClass().getResourceAsStream("/splash.png")
            );
            ImageView imageView = new ImageView(splashImage);
            imageView.setFitWidth(600);
            imageView.setFitHeight(700);
            imageView.setPreserveRatio(false);

            root.getChildren().add(imageView);
        } catch (Exception e) {
            System.err.println("Failed to load splash image: " + e.getMessage());
        }

        this.scene = new Scene(root, 600, 700);
        stage.setScene(this.scene);
        stage.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> screenManager.switchTo(ScreenType.MAIN_MENU));
        delay.play();
    }
}
