package com.tetriskhronos.view.renderers;

import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class FieldPane extends Pane {
    private final Canvas canvas;
    private final BoardRenderer boardRenderer;

    public FieldPane(int width, int height, BoardRenderer boardRenderer) {
        this.boardRenderer = boardRenderer;
        this.canvas = new Canvas(width, height);
        this.getChildren().add(canvas);

        // Set preferred size to match canvas
        setPrefWidth(width);
        setPrefHeight(height);
    }

    public void render(com.tetriskhronos.model.Game game) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        boardRenderer.render(gc, game);
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
