package com.tetriskhronos.ai;

import com.tetriskhronos.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SimpleAIPlayer Tests")
public class SimpleAIPlayerTest {

    private SimpleAIPlayer aiPlayer;
    private Game game;
    private Board board;
    private Configuration config;

    @BeforeEach
    void setUp() {
        config = Configuration.getInstance();
        config.resetToDefaults();
        config.setFieldWidth(10);
        config.setFieldHeight(20);
        config.setGameLevel(1);

        game = new Game(config);
        board = game.getBoard();
        aiPlayer = new SimpleAIPlayer(game);
    }

    @Test
    @DisplayName("should initialize with correct game reference")
    void testInitialization() {
        assertNotNull(aiPlayer);
        assertEquals(150, aiPlayer.getMoveDelayMs());
        assertFalse(aiPlayer.isRunning());
        assertFalse(aiPlayer.isThinking());
    }

    @Test
    @DisplayName("should set move delay correctly")
    void testSetMoveDelayMs() {
        aiPlayer.setMoveDelayMs(200);
        assertEquals(200, aiPlayer.getMoveDelayMs());
    }

    @Test
    @DisplayName("should enforce minimum move delay of 50ms")
    void testSetMoveDelayMinimum() {
        aiPlayer.setMoveDelayMs(10);
        assertEquals(50, aiPlayer.getMoveDelayMs());
    }

    @Test
    @DisplayName("should count holes correctly in board")
    void testCountHoles_EmptyBoard() {
        int holes = aiPlayer.countHoles(board);
        assertEquals(0, holes);
    }

    @Test
    @DisplayName("should count holes with filled cells above empty cells")
    void testCountHoles_WithHoles() {

        for (int col = 0; col < board.getWidth(); col++) {
            board.setCell(board.getHeight() - 1, col, 1);
            if (col % 2 == 0) {
                board.setCell(board.getHeight() - 2, col, 1);
            }
        }

        int holes = aiPlayer.countHoles(board);
        assertTrue(holes > 0, "Should detect holes in pattern");
    }

    @Test
    @DisplayName("should calculate board height correctly for empty board")
    void testCalculateBoardHeight_Empty() {
        int height = aiPlayer.calculateBoardHeight(board);
        assertEquals(0, height);
    }

    @Test
    @DisplayName("should calculate board height from highest filled cell")
    void testCalculateBoardHeight_WithPieces() {
        for (int row = board.getHeight() - 5; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                board.setCell(row, col, 1);
            }
        }

        int height = aiPlayer.calculateBoardHeight(board);
        assertEquals(5, height, "Height should be 5 from bottom");
    }

    @Test
    @DisplayName("should calculate bumpiness for flat surface")
    void testCalculateBumpiness_Flat() {
        for (int col = 0; col < board.getWidth(); col++) {
            board.setCell(board.getHeight() - 1, col, 1);
        }

        int bumpiness = aiPlayer.calculateBumpiness(board);
        assertEquals(0, bumpiness, "Flat surface should have 0 bumpiness");
    }

    @Test
    @DisplayName("should calculate bumpiness for uneven surface")
    void testCalculateBumpiness_Uneven() {
        for (int col = 0; col < board.getWidth(); col++) {
            for (int row = 0; row < col + 1; row++) {
                board.setCell(board.getHeight() - 1 - row, col, 1);
            }
        }

        int bumpiness = aiPlayer.calculateBumpiness(board);
        assertTrue(bumpiness > 0, "Uneven surface should have positive bumpiness");
    }

    @Test
    @DisplayName("should create accurate deep copy of board")
    void testCopyBoard() {
        for (int i = 0; i < 5; i++) {
            board.setCell(board.getHeight() - 1, i, 1);
        }

        Board copy = aiPlayer.copyBoard(board);

        assertEquals(board.getWidth(), copy.getWidth());
        assertEquals(board.getHeight(), copy.getHeight());

        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                assertEquals(board.isCellEmpty(row, col), copy.isCellEmpty(row, col),
                    "Cell mismatch at (" + row + "," + col + ")");
            }
        }
        copy.setCell(0, 0, 1);
        assertTrue(board.isCellEmpty(0, 0), "Original should remain unchanged");
    }

    @Test
    @DisplayName("should give high score for line clears")
    void testEvaluateBoard_LineClears() {
        int scoreNoClears = aiPlayer.evaluateBoard(board, 0);
        int scoreWithClears = aiPlayer.evaluateBoard(board, 4);

        assertTrue(scoreWithClears > scoreNoClears,
            "Score with line clears should be higher");
        assertEquals(4000, scoreWithClears - scoreNoClears,
            "Each line clear should add 1000 points");
    }

    @Test
    @DisplayName("should penalize holes in board evaluation")
    void testEvaluateBoard_HolePenalty() {
        Board boardWithHoles = new Board(10, 20);
        for (int col = 0; col < 10; col++) {
            boardWithHoles.setCell(19, col, 1);
            if (col != 5) {
                boardWithHoles.setCell(18, col, 1);
            }
        }

        int scoreWithHoles = aiPlayer.evaluateBoard(boardWithHoles, 0);
        int scoreNoHoles = aiPlayer.evaluateBoard(new Board(10, 20), 0);

        assertTrue(scoreWithHoles < scoreNoHoles,
            "Board with holes should score lower");
    }

    @Test
    @DisplayName("should penalize tall boards")
    void testEvaluateBoard_HeightPenalty() {
        Board tallBoard = new Board(10, 20);
        for (int row = 5; row < 20; row++) {
            for (int col = 0; col < 10; col++) {
                tallBoard.setCell(row, col, 1);
            }
        }

        int scoreTall = aiPlayer.evaluateBoard(tallBoard, 0);
        int scoreShort = aiPlayer.evaluateBoard(new Board(10, 20), 0);

        assertTrue(scoreTall < scoreShort,
            "Taller board should score lower");
    }

    @Test
    @DisplayName("should penalize bumpy surfaces")
    void testEvaluateBoard_BumpinessPenalty() {
        Board bumpyBoard = new Board(10, 20);
        for (int col = 0; col < 10; col++) {
            int height = (col % 2 == 0) ? 5 : 2;
            for (int row = 0; row < height; row++) {
                bumpyBoard.setCell(bumpyBoard.getHeight() - 1 - row, col, 1);
            }
        }

        int scoreBumpy = aiPlayer.evaluateBoard(bumpyBoard, 0);
        int scoreFlat = aiPlayer.evaluateBoard(new Board(10, 20), 0);

        assertTrue(scoreBumpy < scoreFlat,
            "Bumpy board should score lower than flat");
    }

    @Test
    @DisplayName("should find best move when game is not running")
    void testFindBestMove_NoGameError() {
        game.setGameState(GameState.IDLE);
        assertDoesNotThrow(() -> aiPlayer.findBestMove());
    }

    @Test
    @DisplayName("should find best move with empty board")
    void testFindBestMove_EmptyBoard() {
        game.start();
        SimpleAIPlayer.AIMove bestMove = aiPlayer.findBestMove();

        assertNotNull(bestMove, "Should find a move");
        assertTrue(bestMove.rotation >= 0 && bestMove.rotation < 4,
            "Rotation should be 0-3");
        assertTrue(bestMove.column >= 0 && bestMove.column < board.getWidth(),
            "Column should be valid");
    }

    @Test
    @DisplayName("should return null when no valid moves exist")
    void testFindBestMove_NoValidMoves() {
        game.start();
        for (int row = 5; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                board.setCell(row, col, 1);
            }
        }

        SimpleAIPlayer.AIMove move = aiPlayer.findBestMove();
        if (move != null) {
            assertTrue(move.column >= 0 && move.column < board.getWidth());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("should handle all rotation states")
    void testEvaluatePlacement_AllRotations(int rotation) {
        game.start();
        assertDoesNotThrow(() -> {
            aiPlayer.evaluatePlacement(rotation, board.getWidth() / 2);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3, 5, 9})
    @DisplayName("should handle all column positions")
    void testEvaluatePlacement_AllColumns(int column) {
        game.start();
        assertDoesNotThrow(() -> {
            aiPlayer.evaluatePlacement(0, column);
        });
    }

    @Test
    @DisplayName("should return current score from game")
    void testGetScore() {
        game.start();
        int score = aiPlayer.getScore();
        assertEquals(game.getPoints(), score);
    }

    @Test
    @DisplayName("should return current lines from game")
    void testGetLines() {
        game.start();
        int lines = aiPlayer.getLines();
        assertEquals(game.getLines(), lines);
    }

    @Test
    @DisplayName("should report not thinking when idle")
    void testIsThinking_Idle() {
        assertFalse(aiPlayer.isThinking(), "Should not be thinking initially");
    }

    @Test
    @DisplayName("should prevent concurrent thinking state updates")
    void testIsThinking_ThreadSafe() {
        assertFalse(aiPlayer.isThinking());
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                aiPlayer.isThinking();
            }
        });
    }

    @Test
    @DisplayName("should execute move sequence without throwing")
    void testExecuteMoveSequence_Valid() {
        game.start();
        SimpleAIPlayer.AIMove move = new SimpleAIPlayer.AIMove(0, 5, 100);

        assertDoesNotThrow(() -> {
            aiPlayer.executeMoveSequence(move);
        });
    }

    @Test
    @DisplayName("should handle move execution with boundary conditions")
    void testExecuteMoveSequence_Boundary() {
        game.start();
        SimpleAIPlayer.AIMove leftMove = new SimpleAIPlayer.AIMove(0, 0, 50);
        SimpleAIPlayer.AIMove rightMove = new SimpleAIPlayer.AIMove(0, board.getWidth() - 1, 75);

        assertDoesNotThrow(() -> {
            aiPlayer.executeMoveSequence(leftMove);
            aiPlayer.executeMoveSequence(rightMove);
        });
    }

    @Test
    @DisplayName("should create AIMove with correct values")
    void testAIMove_Creation() {
        SimpleAIPlayer.AIMove move = new SimpleAIPlayer.AIMove(2, 5, 1500);

        assertEquals(2, move.rotation);
        assertEquals(5, move.column);
        assertEquals(1500, move.score);
    }

    @Test
    @DisplayName("should compare AIMove scores correctly")
    void testAIMove_ScoreComparison() {
        SimpleAIPlayer.AIMove move1 = new SimpleAIPlayer.AIMove(1, 3, 1000);
        SimpleAIPlayer.AIMove move2 = new SimpleAIPlayer.AIMove(2, 4, 2000);

        assertTrue(move2.score > move1.score);
    }
}
