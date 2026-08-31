package com.tetriskhronos.view.renderers;

import com.tetriskhronos.model.Board;
import com.tetriskhronos.model.Game;
import com.tetriskhronos.model.Tetromino;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BoardRenderer {
    private static final int CELL_SIZE = 30; // pixels per block
    private static final Color GRID_COLOR = Color.web("#333333");
    private static final Color BG_COLOR = Color.web("#0a0a0a");

    public void render(GraphicsContext gc, Game game) {
        Board board = game.getBoard();
        int cols = board.getWidth();
        int rows = board.getHeight();

        // Clear canvas
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, cols * CELL_SIZE, rows * CELL_SIZE);

        // Draw grid
        drawGrid(gc, cols, rows);

        // Draw placed blocks
        drawPlacedBlocks(gc, board);

        // Draw current falling piece
        if (game.getCurrentPiece() != null) {
            drawTetromino(gc, game.getCurrentPiece(), Color.CYAN);
        }
        drawHUD(gc, game);
    }

    private void drawGrid(GraphicsContext gc, int cols, int rows) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(1);
        for (int x = 0; x <= cols; x++) {
            int px = x * CELL_SIZE;
            gc.strokeLine(px, 0, px, rows * CELL_SIZE);
        }
        for (int y = 0; y <= rows; y++) {
            int py = y * CELL_SIZE;
            gc.strokeLine(0, py, cols * CELL_SIZE, py);
        }
    }
    private void drawPlacedBlocks(GraphicsContext gc, Board board) {
        int[][] grid = board.getGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] != 0) {  // Check if cell is occupied
                    drawCell(gc, col, row, Color.LIMEGREEN);
                }
            }
        }
    }

    private void drawTetromino(GraphicsContext gc, Tetromino piece, Color color) {
        var blocks = piece.getBlocks();
        for (int[] block : blocks) {
            int row = block[0]; // [0] is Y (row)
            int col = block[1];  // [1] is X (col)
            drawCell(gc, col, row, color);
        }
    }

    private void drawCell(GraphicsContext gc, int col, int row, Color color) {
        int x = col * CELL_SIZE;
        int y = row * CELL_SIZE;

        gc.setFill(color);
        gc.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
        gc.setStroke(Color.web("#555555"));
        gc.setLineWidth(0.5);
        gc.strokeRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
    }

    private void drawHUD(GraphicsContext gc, Game game) {
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 16));
        gc.fillText("Score: " + game.getPoints(), 10, -5);
        gc.fillText("Level: " + game.getConfiguration().getGameLevel(), 10, 15);
        gc.fillText("Lines: " + game.getLines(), 10, 35);
    }
}
