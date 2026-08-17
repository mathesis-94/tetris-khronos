package com.tetriskhronos.view;


import javafx.scene.Scene;
import javafx.stage.Stage;

// base class for screens for app
public abstract class Screen {
    protected Stage stage;
    protected Scene scene;
    public Screen(Stage stage) {
        this.stage = stage;
    }
// init and display base screens, subclasses need ui imp
    public abstract void show();
    public Scene getScene() {
        return scene;
    }
    public void onHide() {
        // nothing yet
    }
}
