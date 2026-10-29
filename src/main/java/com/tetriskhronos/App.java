package com.tetriskhronos;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import com.tetriskhronos.audio.MusicManager;
import com.tetriskhronos.controller.ScreenManager;
import com.tetriskhronos.controller.ScreenType;
import com.tetriskhronos.model.Configuration;


public class App extends Application {
    private static final String APP_TITLE = "Tetris-Khronos";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 700;

    private Configuration configuration;
    private ScreenManager screenManager;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.configuration = Configuration.getInstance();
        this.screenManager = new ScreenManager(primaryStage);

        configureWindow();
        MusicManager.getInstance().play();
        showSplashScreen();
    }

    private void configureWindow() {
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();

        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
        }
        primaryStage.setOnCloseRequest(event -> handleWindowClose());

        primaryStage.show();
    }

    private void showSplashScreen() {
        screenManager.switchTo(ScreenType.SPLASH);
    }

    private void handleWindowClose() {
        System.exit(0);
    }
}
