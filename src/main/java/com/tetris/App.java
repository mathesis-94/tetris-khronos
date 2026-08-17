package com.tetris;

import javafx.application.Application;
import javafx.stage.Stage;
import com.tetris.view.SplashScreen;
import com.tetris.view.Mainframe;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Show splash screen
        SplashScreen splash = new SplashScreen(4000);
        splash.showSplash();

        // Show main application window
        Mainframe mainFrame = new Mainframe();
        mainFrame.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
