package com.tetriskhronos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Board Tests")
class BoardTest {
    private Board board;

    @Spy
    private Board spyBoard;

    @BeforeEach
    void setUp() {
        board = new Board(10, 20);
        spyBoard = spy(new Board(10, 20));
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should clear board on initialization")
    void testBoardClear() {
        // Verify all cells are empty (0)
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                assertTrue(board.isCellEmpty(row, col),
                    "Cell [" + row + "," + col + "] should be empty");
            }
        }
    }

    @Test
    @DisplayName("Should detect complete rows and clear them")
    void testClearCompleteRows() {
        // Fill bottom row completely (tetrominoId = 1)
        for (int col = 0; col < board.getWidth(); col++) {
            board.setCell(board.getHeight() - 1, col, 1);
        }

        int clearedCount = board.clearRows();

        assertEquals(1, clearedCount, "Should clear 1 row");
        // Verify row is now empty
        for (int col = 0; col < board.getWidth(); col++) {
            assertTrue(board.isCellEmpty(board.getHeight() - 1, col),
                "Cleared row should be empty");
        }
    }

    @Test
    @DisplayName("Should detect occupied cells")
    void testOccupiedCells() {
        board.setCell(5, 5, 2);

        assertFalse(board.isCellEmpty(5, 5), "Cell should be occupied");
        assertEquals(2, board.getCell(5, 5), "Cell value should be 2");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
    @DisplayName("Should set and retrieve different tetromino IDs")
    void testSetAndGetCell(int tetrominoId) {
        board.setCell(10, 5, tetrominoId);

        assertEquals(tetrominoId, board.getCell(10, 5),
            "Cell should contain tetromino ID " + tetrominoId);
    }

    @Test
    @DisplayName("Should verify spy tracks cell placement")
    void testSpyPlacedBlocks() {
        int x = 5, y = 5;
        int[][] blocks = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
        int tetrominoId = 3;

        spyBoard.placeBlocks(x, y, blocks, tetrominoId);

        // Verify cell is occupied (not empty)
        assertFalse(spyBoard.isCellEmpty(y, x),
            "Placed block should not be empty");

        // Verify spy was called
        verify(spyBoard).placeBlocks(x, y, blocks, tetrominoId);
    }

    @Test
    @DisplayName("Should detect complete rows")
    void testGetCompleteRows() {
        // Fill row 15 completely
        for (int col = 0; col < board.getWidth(); col++) {
            board.setCell(15, col, 1);
        }

        List<Integer> completeRows = board.getCompleteRows();

        assertTrue(completeRows.contains(15), "Row 15 should be complete");
    }

    @Test
    @DisplayName("Should detect game over when top row has blocks")
    void testGameOverCondition() {
        board.setCell(0, 5, 1);

        assertTrue(board.isGameOver(), "Game should be over when top row is occupied");
    }

    @Test
    @DisplayName("Should validate bounds checking")
    void testBoundsChecking() {
        assertFalse(board.isWithinBounds(-1, 5), "Negative row should be out of bounds");
        assertFalse(board.isWithinBounds(20, 5), "Row >= height should be out of bounds");
        assertFalse(board.isWithinBounds(5, -1), "Negative col should be out of bounds");
        assertFalse(board.isWithinBounds(5, 10), "Col >= width should be out of bounds");
        assertTrue(board.isWithinBounds(5, 5), "Valid coords should be in bounds");
    }
}
