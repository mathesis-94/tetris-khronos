package com.tetriskhronos.view.renderers;

import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class FieldPane extends Pane {
    private final Canvas canvas;
    private final BoardRenderer boardRenderer;

    public FieldPane(int initialWidth, int initialHeight, BoardRenderer boardRenderer) {
        this.boardRenderer = boardRenderer;
        this.canvas = new Canvas();

        // Bind canvas to Pane size, but only if Pane is large enough
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        this.getChildren().add(canvas);

        // Set initial AND minimum size to prevent collapse
        setPrefWidth(initialWidth);
        setPrefHeight(initialHeight);
        setMinWidth(initialWidth);
        setMinHeight(initialHeight);
    }


    public void render(com.tetriskhronos.model.Game game) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        boardRenderer.render(gc, game);
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
