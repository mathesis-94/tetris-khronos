package com.tetriskhronos.view;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import com.tetriskhronos.controller.ScreenManager;
import com.tetriskhronos.controller.ScreenType;

// display on start up like in demo vid
public class SplashScreen extends Screen {

    private ScreenManager screenManager;

    // new splashscreen
    public SplashScreen(Stage stage, ScreenManager screenManager) {
        super(stage);
        this.screenManager = screenManager;
    }
    // panes and text stuff
    @Override
    public void show() {

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a1a;");

        Text splashText = new Text("TETRIS-KHRONOS");
        splashText.setFont(Font.font("Arial", 48));
        splashText.setFill(Color.CYAN);

        root.getChildren().add(splashText);

        this.scene = new Scene(root, 600, 700);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> screenManager.switchTo(ScreenType.MAIN_MENU));
        delay.play();
    }
}
