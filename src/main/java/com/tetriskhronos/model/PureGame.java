package com.tetriskhronos.model;

import java.util.Arrays;

public class PureGame {
    private int width;
    private int height;
    private int[][] cells;
    private int[][] currentShape;
    private int currentX;
    private int currentY;
    private int currentRotation;
    private int currentType;
    private int[][] nextShape;
    private int nextType;
    private int score;
    private int level;
    private int lines;
    private long gameTime;

    // Constructors
    public PureGame() {}

    public PureGame(int width, int height, int[][] cells, int[][] currentShape, int[][] nextShape) {
        this.width = width;
        this.height = height;
        this.cells = cells;
        this.currentShape = currentShape;
        this.nextShape = nextShape;
    }

    // Width
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    // Height
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    // Cells (board grid)
    public int[][] getCells() { return cells; }
    public void setCells(int[][] cells) { this.cells = cells; }

    // Current piece shape
    public int[][] getCurrentShape() { return currentShape; }
    public void setCurrentShape(int[][] currentShape) { this.currentShape = currentShape; }

    // Current piece position and rotation
    public int getCurrentX() { return currentX; }
    public void setCurrentX(int currentX) { this.currentX = currentX; }

    public int getCurrentY() { return currentY; }
    public void setCurrentY(int currentY) { this.currentY = currentY; }

    public int getCurrentRotation() { return currentRotation; }
    public void setCurrentRotation(int currentRotation) { this.currentRotation = currentRotation; }

    // Current piece type
    public int getCurrentType() { return currentType; }
    public void setCurrentType(int currentType) { this.currentType = currentType; }

    // Next piece shape and type
    public int[][] getNextShape() { return nextShape; }
    public void setNextShape(int[][] nextShape) { this.nextShape = nextShape; }

    public int getNextType() { return nextType; }
    public void setNextType(int nextType) { this.nextType = nextType; }

    // Game state: score, level, lines, time
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getLines() { return lines; }
    public void setLines(int lines) { this.lines = lines; }

    public long getGameTime() { return gameTime; }
    public void setGameTime(long gameTime) { this.gameTime = gameTime; }

    @Override
    public String toString() {
        return "PureGame{" +
                "width=" + width +
                ", height=" + height +
                ", currentX=" + currentX +
                ", currentY=" + currentY +
                ", currentRotation=" + currentRotation +
                ", currentType=" + currentType +
                ", score=" + score +
                ", level=" + level +
                ", lines=" + lines +
                ", cells=" + Arrays.deepToString(cells) +
                ", currentShape=" + Arrays.deepToString(currentShape) +
                ", nextShape=" + Arrays.deepToString(nextShape) +
                ", gameTime=" + gameTime +
                '}';
    }
}
