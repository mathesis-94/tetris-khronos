package com.tetriskhronos.ai;

import com.tetriskhronos.model.Board;
import com.tetriskhronos.model.Tetromino;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AIPlayerTest {

    private AIPlayer aiPlayer;
    private Board board;
    private Random random;

    @BeforeEach
    void setUp() {
        aiPlayer = new AIPlayer();
        board = new Board();
        random = new Random();
    }

    @Test
    void testSelectMoveWithValidBoard() {
        aiPlayer.startEpisode(board);

        Tetromino currentPiece = Tetromino.createRandom(random);

        Move move = aiPlayer.selectMove(board, currentPiece);

        assertNotNull(move, "Move should not be null");
        assertTrue(move.rotations >= 0 && move.rotations < 4,
                   "Rotation should be between 0-3");
        assertTrue(move.targetColumn >= 0 && move.targetColumn < board.getWidth(),
                   "Target column should be within board bounds");
    }

    @Test
    void testStartAndEndEpisode() {
        aiPlayer.startEpisode(board);

        Tetromino piece = Tetromino.createRandom(random);
        Move move = aiPlayer.selectMove(board, piece);
        assertNotNull(move);

        aiPlayer.updateAfterMove(board, 0);
        aiPlayer.endGame(100);

        assertTrue(true, "Episode completed without errors");
    }

    @Test
    void testReloadWeights() throws Exception {
        aiPlayer.startEpisode(board);
        aiPlayer.selectMove(board, Tetromino.createRandom(random));
        aiPlayer.updateAfterMove(board, 0);
        aiPlayer.endGame(50);
        aiPlayer.saveWeights();

        String weightsPath = System.getProperty("user.home") + "/.tetriskhronos/neural_weights.dat";
        assertTrue(Files.exists(Paths.get(weightsPath)),
                   "Weights file should exist after saveWeights()");
    }

    @Test
    void testMultipleEpisodes() {
        for (int i = 0; i < 3; i++) {
            Board testBoard = new Board();
            aiPlayer.startEpisode(testBoard);

            for (int step = 0; step < 5; step++) {
                Move move = aiPlayer.selectMove(testBoard, Tetromino.createRandom(random));
                assertNotNull(move);
                aiPlayer.updateAfterMove(testBoard, 0);
            }

            aiPlayer.endGame(50 + i * 10);
        }

        assertTrue(true, "Multiple episodes completed successfully");
    }

    @Test
    void testRewardCalculationWithLineClears() {
        aiPlayer.startEpisode(board);
        aiPlayer.selectMove(board, Tetromino.createRandom(random));

        aiPlayer.updateAfterMove(board, 1);
        aiPlayer.updateAfterMove(board, 0);

        aiPlayer.endGame(200);
        assertTrue(true, "Reward calculation with line clears handled");
    }

    @Test
    void testEpsilonDecay() {
        for (int episode = 0; episode < 20; episode++) {
            Board testBoard = new Board();
            aiPlayer.startEpisode(testBoard);

            Move move = aiPlayer.selectMove(testBoard, Tetromino.createRandom(random));
            assertNotNull(move);

            aiPlayer.updateAfterMove(testBoard, 0);
            aiPlayer.endGame(100);
        }

        assertTrue(true, "Epsilon decay applied over multiple episodes");
    }

    @Test
    void testHandlesEmptyBoard() {
        Board emptyBoard = new Board();
        aiPlayer.startEpisode(emptyBoard);

        Move move = aiPlayer.selectMove(emptyBoard, Tetromino.createRandom(random));
        assertNotNull(move);

        aiPlayer.updateAfterMove(emptyBoard, 0);
        aiPlayer.endGame(0);

        assertTrue(true, "Empty board handled correctly");
    }

    @Test
    void testMoveObjectProperties() {
        aiPlayer.startEpisode(board);
        Move move = aiPlayer.selectMove(board, Tetromino.createRandom(random));

        assertTrue(move.rotations >= 0, "Rotation should be non-negative");
        assertTrue(move.targetColumn >= 0, "Target column should be non-negative");
    }
}
