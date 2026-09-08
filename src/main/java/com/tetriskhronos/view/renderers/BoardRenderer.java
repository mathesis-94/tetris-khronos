package com.tetriskhronos.view.renderers;

import com.tetriskhronos.model.Board;
import com.tetriskhronos.model.Game;
import com.tetriskhronos.model.Tetromino;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BoardRenderer {
    private static final Color GRID_COLOR = Color.web("#333333");
    private static final Color BG_COLOR = Color.web("#0a0a0a");

    public void render(GraphicsContext gc, Game game) {
        Board board = game.getBoard();
        int cols = board.getWidth();
        int rows = board.getHeight();

        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        double cellWidth = canvasWidth / cols;
        double cellHeight = canvasHeight / rows;

        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Draw grid
        drawGrid(gc, cols, rows, cellWidth, cellHeight);

        drawPlacedBlocks(gc, board, cellWidth, cellHeight);

        // Draw current falling piece
        if (game.getCurrentPiece() != null) {
            drawTetromino(gc, game.getCurrentPiece(), Color.CYAN, cellWidth, cellHeight);
        }
        drawHUD(gc, game);
    }

    private void drawGrid(GraphicsContext gc, int cols, int rows, double cellWidth, double cellHeight) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(1);
        for (int x = 0; x <= cols; x++) {
            double px = x * cellWidth;
            gc.strokeLine(px, 0, px, rows * cellHeight);
        }
        for (int y = 0; y <= rows; y++) {
            double py = y * cellHeight;
            gc.strokeLine(0, py, cols * cellWidth, py);
        }
    }

    private void drawPlacedBlocks(GraphicsContext gc, Board board, double cellWidth, double cellHeight) {
        int[][] grid = board.getGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] != 0) {
                    drawCell(gc, col, row, Color.LIMEGREEN, cellWidth, cellHeight);
                }
            }
        }
    }

    private void drawTetromino(GraphicsContext gc, Tetromino piece, Color color, double cellWidth, double cellHeight) {
        var blocks = piece.getBlocks();
        int pieceX = piece.getX();
        int pieceY = piece.getY();

        for (int[] block : blocks) {
            int row = pieceY + block[0];
            int col = pieceX + block[1];
            drawCell(gc, col, row, color, cellWidth, cellHeight);
        }
    }

    private void drawCell(GraphicsContext gc, int col, int row, Color color, double cellWidth, double cellHeight) {
        double x = col * cellWidth;
        double y = row * cellHeight;
        double padding = 1;

        gc.setFill(color);
        gc.fillRect(x + padding, y + padding, cellWidth - 2 * padding, cellHeight - 2 * padding);
        gc.setStroke(Color.web("#555555"));
        gc.setLineWidth(0.5);
        gc.strokeRect(x + padding, y + padding, cellWidth - 2 * padding, cellHeight - 2 * padding);
    }

    private void drawHUD(GraphicsContext gc, Game game) {
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 16));
        gc.fillText("Score: " + game.getPoints(), 10, 20);
        gc.fillText("Level: " + game.getConfiguration().getGameLevel(), 10, 40);
        gc.fillText("Lines: " + game.getLines(), 10, 60);
    }
}
