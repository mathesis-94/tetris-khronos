package com.tetriskhronos;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import com.tetriskhronos.controller.ScreenManager;
import com.tetriskhronos.controller.ScreenType;
import com.tetriskhronos.model.Configuration;


// entry point to initialise app window, singleton config,
// screenmanager, and launch the splash screen like in demo video for
// m1
public class App extends Application {
    // Window constants
    private static final String APP_TITLE = "Tetris-Khronos";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 700;

    // app singleton, factory
    private Configuration configuration;
    private ScreenManager screenManager;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.configuration = Configuration.getInstance();
        this.screenManager = new ScreenManager(primaryStage);

        // config primary stage
        configureWindow();

        // splash screen
        showSplashScreen();
    }

    // config prim stage window props
    private void configureWindow() {
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();

        // set icon (need to create icon so this works), update path for icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        primaryStage.setOnCloseRequest(event -> handleWindowClose());

        primaryStage.show();

    }
    // display splash on start
    private void showSplashScreen() {
        screenManager.switchTo(ScreenType.SPLASH);
    }
    private void handleWindowClose() {
        System.exit(0);
    }
    // entry point
    public static void main(String[] args) {
        launch(args);
    }
}
