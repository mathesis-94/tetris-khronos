package com.tetriskhronos.model;

import java.util.Random;
/**
 * Sealed abstract class for Tetromino pieces, track positions etc
 */
public sealed abstract class Tetromino permits
    IPiece, OPiece, TPiece, SPiece, ZPiece, JPiece, LPiece {

    protected int[][] shape;
    protected TetrominoColor color;
    protected int rotationState = 0;
    protected int x; // column position on board
    protected int y; // row position on board
    protected int type; // 1-7 for each piece type

    protected Tetromino(int[][] initialShape, TetrominoColor color, int type) {
        this.shape = initialShape;
        this.color = color;
        this.type = type;
        this.x = 3; // spawn near center (column 3-4 for 10-width board)
        this.y = 0; // spawn at top
    }

    public int[][] getBlocks() {
        int[][] blocks = new int[4][2];
        int blockIndex = 0;
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1 && blockIndex < 4) {
                    blocks[blockIndex][0] = y + row;
                    blocks[blockIndex][1] = x + col;
                    blockIndex++;
                }
            }
        }
        return blocks;
    }

    public int[][] getBlocksAtOffset(int rowOffset, int colOffset) {
        int[][] blocks = new int[4][2];
        int blockIndex = 0;
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1 && blockIndex < 4) {
                    blocks[blockIndex][0] = y + row + rowOffset;
                    blocks[blockIndex][1] = x + col + colOffset;
                    blockIndex++;
                }
            }
        }
        return blocks;
    }

    public int[][] getRotatedBlocks() {
        int[][] tempBlocks = new int[4][2];
        int blockIndex = 0;
        int oldRotationState = this.rotationState;
        rotate();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1 && blockIndex < 4) {
                    tempBlocks[blockIndex][0] = y + row;
                    tempBlocks[blockIndex][1] = x + col;
                    blockIndex++;
                }
            }
        }

        // Revert rotation
        this.rotationState = oldRotationState;
        rotate();

        return tempBlocks;
    }

    public void moveDown() {
        this.y++;
    }
    public void moveLeft() {
        this.x--;
    }
    public void moveRight() {
        this.x++;
    }

    /**
     * Rotate 90
     */
    public abstract void rotate();
    public abstract boolean canPlaceAt(int x, int y, Board board);

    public int getRotationState() {
        return rotationState;
    }

    public int[][] getShape() {
        return shape;
    }
    public TetrominoColor getColor() {
        return color;
    }

    public int getType() {
        return type;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    /* make random tetro */
    public static Tetromino createRandom(Random random) {
        return switch (random.nextInt(7)) {
            case 0 -> new IPiece();
            case 1 -> new OPiece();
            case 2 -> new TPiece();
            case 3 -> new SPiece();
            case 4 -> new ZPiece();
            case 5 -> new JPiece();
            case 6 -> new LPiece();
            default -> new TPiece();
        };
    }

    //  Colour Enum
    public enum TetrominoColor {
        CYAN("#00FFFF"),
        YELLOW("#FFFF00"),
        PURPLE("#AA00FF"),
        GREEN("#00FF00"),
        RED("#FF0000"),
        BLUE("#0000FF"),
        ORANGE("#FFA500");

        private final String hexColor;

        TetrominoColor(String hexColor) {
            this.hexColor = hexColor;
        }

        public String getHexColor() {
            return hexColor;
        }
    }
}

// ========== Tetro Pieces  ==========


final class IPiece extends Tetromino {
    public IPiece() {
        super(new int[][] {{1, 1, 1, 1}}, TetrominoColor.CYAN, 1);
    }

    @Override
    public void rotate() {
        rotationState = (rotationState + 1) % 2;
        shape = rotationState == 0
            ? new int[][] {{1, 1, 1, 1}}
            : new int[][] {{1}, {1}, {1}, {1}};
    }

    @Override
    public boolean canPlaceAt(int x, int y, Board board) {
        return rotationState == 0
            ? x + 4 <= board.getWidth() && y + 1 <= board.getHeight()
            : x + 1 <= board.getWidth() && y + 4 <= board.getHeight();
    }
}


final class OPiece extends Tetromino {
    public OPiece() {
        super(new int[][] {{1, 1}, {1, 1}}, TetrominoColor.YELLOW, 2);
    }

    @Override
    public void rotate() {
        // O piece doesn't rotate
    }

    @Override
    public boolean canPlaceAt(int x, int y, Board board) {
        return x + 2 <= board.getWidth() && y + 2 <= board.getHeight();
    }
}


final class TPiece extends Tetromino {
    public TPiece() {
        super(new int[][] {{0, 1, 0}, {1, 1, 1}}, TetrominoColor.PURPLE, 3);
    }

    @Override
    public void rotate() {
        rotationState = (rotationState + 1) % 4;
        shape = switch (rotationState) {
            case 0 -> new int[][] {{0, 1, 0}, {1, 1, 1}};
            case 1 -> new int[][] {{1, 0}, {1, 1}, {1, 0}};
            case 2 -> new int[][] {{1, 1, 1}, {0, 1, 0}};
            case 3 -> new int[][] {{0, 1}, {1, 1}, {0, 1}};
            default -> shape;
        };
    }

    @Override
    public boolean canPlaceAt(int x, int y, Board board) {
        return x + 3 <= board.getWidth() && y + 2 <= board.getHeight();
    }
}


final class SPiece extends Tetromino {
    public SPiece() {
        super(new int[][] {{0, 1, 1}, {1, 1, 0}}, TetrominoColor.GREEN, 4);
    }

    @Override
    public void rotate() {
        rotationState = (rotationState + 1) % 2;
        shape = rotationState == 0
            ? new int[][] {{0, 1, 1}, {1, 1, 0}}
            : new int[][] {{1, 0}, {1, 1}, {0, 1}};
    }

    @Override
    public boolean canPlaceAt(int x, int y, Board board) {
        return rotationState == 0
            ? x + 3 <= board.getWidth() && y + 2 <= board.getHeight()
            : x + 2 <= board.getWidth() && y + 3 <= board.getHeight();
    }
}


final class ZPiece extends Tetromino {
    public ZPiece() {
        super(new int[][] {{1, 1, 0}, {0, 1, 1}}, TetrominoColor.RED, 5);
    }

    @Override
    public void rotate() {
        rotationState = (rotationState + 1) % 2;
        shape = rotationState == 0
            ? new int[][] {{1, 1, 0}, {0, 1, 1}}
            : new int[][] {{0, 1}, {1, 1}, {1, 0}};
    }

    @Override
    public boolean canPlaceAt(int x, int y, Board board) {
        return rotationState == 0
            ? x + 3 <= board.getWidth() && y + 2 <= board.getHeight()
            : x + 2 <= board.getWidth() && y + 3 <= board.getHeight();
    }
}

final class JPiece extends Tetromino {
    public JPiece() {
        super(new int[][] {{1, 0, 0}, {1, 1, 1}}, TetrominoColor.BLUE, 6);
    }

    @Override
    public void rotate() {
        rotationState = (rotationState + 1) % 4;
        shape = switch (rotationState) {
            case 0 -> new int[][] {{1, 0, 0}, {1, 1, 1}};
            case 1 -> new int[][] {{1, 1}, {1, 0}, {1, 0}};
            case 2 -> new int[][] {{1, 1, 1}, {0, 0, 1}};
            case 3 -> new int[][] {{0, 1}, {0, 1}, {1, 1}};
            default -> shape;
        };
    }

    @Override
    public boolean canPlaceAt(int x, int y, Board board) {
        return x + 3 <= board.getWidth() && y + 2 <= board.getHeight();
    }
}

final class LPiece extends Tetromino {
    public LPiece() {
        super(new int[][] {{0, 0, 1}, {1, 1, 1}}, TetrominoColor.ORANGE, 7);
    }

    @Override
    public void rotate() {
        rotationState = (rotationState + 1) % 4;
        shape = switch (rotationState) {
            case 0 -> new int[][] {{0, 0, 1}, {1, 1, 1}};
            case 1 -> new int[][] {{1, 0}, {1, 0}, {1, 1}};
            case 2 -> new int[][] {{1, 1, 1}, {1, 0, 0}};
            case 3 -> new int[][] {{1, 1}, {0, 1}, {0, 1}};
            default -> shape;
        };
    }

    @Override
    public boolean canPlaceAt(int x, int y, Board board) {
        return x + 3 <= board.getWidth() && y + 2 <= board.getHeight();
    }
}
