package com.tetriskhronos.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tetris game board (10x20 def)
 * Handle piece placement, collision detection, line clearing, state queries
 */
public class Board {
    private final int width;
    private final int height;
    private final int[][] grid; // 0 = empty, 1-7 = tetromino type (or color)

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        clear();
    }

    /* clear board */
    public void clear() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col] = 0;
            }
        }
    }

    public boolean isCellEmpty(int row, int col) {
        if (row < 0 || row >= height || col < 0 || col >= width) {
            return false;
        }
        return grid[row][col] == 0;
    }

    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    /* set cell to tetro id */
    public void setCell(int row, int col, int value) {
        if (isWithinBounds(row, col)) {
            grid[row][col] = value;
        }
    }

    /*  cell value check */
    public int getCell(int row, int col) {
        if (isWithinBounds(row, col)) {
            return grid[row][col];
        }
        return -1;
    }

    public boolean canPlaceBlocks(int x, int y, int[][] blocks) {
        for (int[] block : blocks) {
            int row = y + block[0];
            int col = x + block[1];
            if (!isWithinBounds(row, col) || !isCellEmpty(row, col)) {
                return false;
            }
        }
        return true;
    }

    /* place tetromino */
    public void placeBlocks(int x, int y, int[][] blocks, int tetrominoId) {
        for (int[] block : blocks) {
            int row = y + block[0];
            int col = x + block[1];
            if (isWithinBounds(row, col)) {
                grid[row][col] = tetrominoId;
            }
        }
    }

    public List<Integer> getCompleteRows() {
        List<Integer> completeRows = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            boolean isComplete = true;
            for (int col = 0; col < width; col++) {
                if (grid[row][col] == 0) {
                    isComplete = false;
                    break;
                }
            }
            if (isComplete) {
                completeRows.add(row);
            }
        }
        return completeRows;
    }


    public int clearRows() {
        List<Integer> completeRows = getCompleteRows();
        for (Integer rowIndex : completeRows) {
            for (int row = rowIndex; row > 0; row--) {
                System.arraycopy(grid[row - 1], 0, grid[row], 0, width);
            }
            for (int col = 0; col < width; col++) {
                grid[0][col] = 0;
            }
        }
        return completeRows.size();  // Return count instead
    }

    /* game over check  */
    public boolean isGameOver() {
        for (int col = 0; col < width; col++) {
            if (grid[0][col] != 0) {
                return true;
            }
        }
        return false;
    }

    /* grid snapshot */
    public int[][] getGrid() {
        return grid;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
