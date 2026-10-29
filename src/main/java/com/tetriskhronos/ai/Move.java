package com.tetriskhronos.ai;

public class Move {
    public int rotations;
    public int targetColumn;

    public Move(int rotations, int targetColumn) {
        this.rotations = rotations;
        this.targetColumn = targetColumn;
    }
}
