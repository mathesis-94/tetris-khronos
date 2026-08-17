package com.tetriskhronos.controller;
import javafx.stage.Stage;
import com.tetriskhronos.view.Screen;
import com.tetriskhronos.view.SplashScreen;

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
                // return new MainMenuScreen(primaryStage, this);
                break;
            case CONFIG:
                // return new ConfigScreen(primaryStage, this);
                break;
            case HIGH_SCORES:
                // return new HighScoreScreen(primaryStage, this);
                break;
            case PLAYING:
                // return new GameScreen(primaryStage, this);
                break;
            default:
                System.err.println("Unknown screen type: " + screenType);
                return null;
        }
        return null;
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
