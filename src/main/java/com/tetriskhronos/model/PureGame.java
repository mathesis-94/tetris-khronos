package com.tetriskhronos.model;

import java.util.Arrays;

public class PureGame {
    private int width;
    private int height;
    private int[][] cells;
    private int[][] currentShape;
    private int[][] nextShape;

    // Constructors
    public PureGame() {}

    public PureGame(int width, int height, int[][] cells, int[][] currentShape, int[][] nextShape) {
        this.width = width;
        this.height = height;
        this.cells = cells;
        this.currentShape = currentShape;
        this.nextShape = nextShape;
    }

    // Getters and Setters
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public int[][] getCells() { return cells; }
    public void setCells(int[][] cells) { this.cells = cells; }

    public int[][] getCurrentShape() { return currentShape; }
    public void setCurrentShape(int[][] currentShape) { this.currentShape = currentShape; }

    public int[][] getNextShape() { return nextShape; }
    public void setNextShape(int[][] nextShape) { this.nextShape = nextShape; }

    @Override
    public String toString() {
        return "PureGame{" +
                "width=" + width +
                ", height=" + height +
                ", cells=" + Arrays.deepToString(cells) +
                ", currentShape=" + Arrays.deepToString(currentShape) +
                ", nextShape=" + Arrays.deepToString(nextShape) +
                '}';
    }
}
