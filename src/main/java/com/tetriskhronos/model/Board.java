package com.tetriskhronos.model;

public class Board {
    private final int width;
    private final int height;
    private final int[][] grid;

    public Board() {
        this(10, 20);
    }

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
    }

    public Board(Board other) {
        this.width = other.width;
        this.height = other.height;
        this.grid = new int[height][width];
        for (int row = 0; row < height; row++) {
            System.arraycopy(other.grid[row], 0, this.grid[row], 0, width);
        }
    }

    public boolean isCellEmpty(int row, int col) {
        return isWithinBounds(row, col) && grid[row][col] == 0;
    }

    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    public void setCell(int row, int col, int value) {
        if (isWithinBounds(row, col)) {
            grid[row][col] = value;
        }
    }

    public void clear() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col] = 0;
            }
        }
    }

    public int getCell(int row, int col) {
        if (isWithinBounds(row, col)) {
            return grid[row][col];
        }
        return -1;
    }

    public boolean canPlacePiece(Tetromino piece) {
        int[][] blocks = piece.getBlocksAtOffset(piece.getY(), piece.getX());
        for (int[] block : blocks) {
            int row = block[0];
            int col = block[1];
            if (!isWithinBounds(row, col) || !isCellEmpty(row, col)) {
                return false;
            }
        }
        return true;
    }

    public void placePiece(Tetromino piece) {
        int[][] blocks = piece.getBlocksAtOffset(piece.getY(), piece.getX());
        for (int[] block : blocks) {
            int row = block[0];
            int col = block[1];
            if (isWithinBounds(row, col)) {
                setCell(row, col, piece.getType());
            }
        }
    }

    public int[] getCompleteRows() {
        int[] completeRows = new int[height];
        int count = 0;
        for (int row = 0; row < height; row++) {
            boolean complete = true;
            for (int col = 0; col < width; col++) {
                if (isCellEmpty(row, col)) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                completeRows[count++] = row;
            }
        }
        int[] result = new int[count];
        System.arraycopy(completeRows, 0, result, 0, count);
        return result;
    }

    public void clearRows(int[] rows) {
        for (int row : rows) {
            if (row >= 0 && row < height) {
                for (int col = 0; col < width; col++) {
                    setCell(row, col, 0);
                }
            }
        }
        compactRows();
    }

    private void compactRows() {
        int writeRow = height - 1;
        for (int readRow = height - 1; readRow >= 0; readRow--) {
            boolean hasBlock = false;
            for (int col = 0; col < width; col++) {
                if (!isCellEmpty(readRow, col)) {
                    hasBlock = true;
                    break;
                }
            }
            if (hasBlock) {
                if (readRow != writeRow) {
                    System.arraycopy(grid[readRow], 0, grid[writeRow], 0, width);
                    for (int col = 0; col < width; col++) {
                        grid[readRow][col] = 0;
                    }
                }
                writeRow--;
            }
        }
    }

    public boolean isGameOver() {
        for (int col = 0; col < width; col++) {
            if (!isCellEmpty(0, col)) {
                return true;
            }
        }
        return false;
    }

    public int[][] getGrid() {
        int[][] copy = new int[height][width];
        for (int row = 0; row < height; row++) {
            System.arraycopy(grid[row], 0, copy[row], 0, width);
        }
        return copy;
    }

    public int[][] getCells() {
        return getGrid();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
