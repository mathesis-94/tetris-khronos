package com.tetriskhronos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board Tests")
class BoardTest {
    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(10, 20);
    }

    @Test
    @DisplayName("Should clear board on initialization")
    void testBoardClear() {
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
        for (int col = 0; col < board.getWidth(); col++) {
            board.setCell(board.getHeight() - 1, col, 1);
        }

        int[] completeRows = board.getCompleteRows();
        assertEquals(1, completeRows.length, "Should have 1 complete row");

        board.clearRows(completeRows);

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
    @DisplayName("Should detect complete rows")
    void testGetCompleteRows() {
        for (int col = 0; col < board.getWidth(); col++) {
            board.setCell(15, col, 1);
        }

        int[] completeRows = board.getCompleteRows();

        assertTrue(Arrays.stream(completeRows).anyMatch(row -> row == 15),
            "Row 15 should be complete");
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

    @Test
    @DisplayName("Should copy board correctly")
    void testBoardCopy() {
        board.setCell(5, 5, 1);
        board.setCell(10, 3, 2);
        Board copy = new Board(board);
        assertEquals(board.getCell(5, 5), copy.getCell(5, 5), "Copy should have same cell value at (5,5)");
        assertEquals(board.getCell(10, 3), copy.getCell(10, 3), "Copy should have same cell value at (10,3)");
        copy.setCell(5, 5, 99);
        assertNotEquals(board.getCell(5, 5), copy.getCell(5, 5), "Modifying copy should not affect original");
    }

    @Test
    @DisplayName("Should clear the entire board")
    void testBoardClearMethod() {
        board.setCell(5, 5, 1);
        board.setCell(10, 3, 2);
        assertFalse(board.isCellEmpty(5, 5), "Cell should be occupied before clear");
        board.clear();

        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                assertTrue(board.isCellEmpty(row, col),
                    "Cell [" + row + "," + col + "] should be empty after clear()");
            }
        }
    }
}
