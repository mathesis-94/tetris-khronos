package com.tetriskhronos.controller;

import javafx.stage.Stage;
import com.tetriskhronos.model.Game;
import com.tetriskhronos.view.Screen;
import com.tetriskhronos.view.SplashScreen;
import com.tetriskhronos.view.MainMenuScreen;
import com.tetriskhronos.view.GameScreen;
import com.tetriskhronos.view.ConfigScreen;
import com.tetriskhronos.view.HighScoreScreen;
import com.tetriskhronos.model.Configuration;

// factory pattern controller, manages screen creation & nav/ switching states
public class ScreenManager {

    private Stage primaryStage;
    private Screen currentScreen;

    // new screenmanager
    public ScreenManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // switch to screen type
    public void switchTo(ScreenType screenType) {
        if (currentScreen != null) {
            currentScreen.onHide();
        }
        currentScreen = createScreen(screenType);
        if (currentScreen != null) {
            currentScreen.show();
            primaryStage.setScene(currentScreen.getScene());
        }
    }

    private Screen createScreen(ScreenType screenType) {
        switch (screenType) {
            case SPLASH:
                return new SplashScreen(primaryStage, this);
            case MAIN_MENU:
                return new MainMenuScreen(this);
            case CONFIG:
                return new ConfigScreen(
                    new Game(Configuration.getInstance()),
                    () -> switchTo(ScreenType.MAIN_MENU),
                    primaryStage
                );
            case HIGH_SCORES:
                return new HighScoreScreen(this);
            case PLAYING:
                return new GameScreen(
                    primaryStage,
                    new Game(Configuration.getInstance()),
                    () -> switchTo(ScreenType.MAIN_MENU)
                );
            default:
                System.err.println("Unknown screen type: " + screenType);
                return null;
        }
    }

    // currently displayed screen
    public Screen getCurrentScreen() {
        return currentScreen;
    }

    // get prim stage
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
