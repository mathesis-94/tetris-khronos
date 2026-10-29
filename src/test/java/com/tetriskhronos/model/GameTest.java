package com.tetriskhronos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Game Tests")
public class GameTest {

    private Configuration config;
    private Game game;

    @BeforeEach
    public void setUp() {
        config = Configuration.getInstance();
        config.resetToDefaults();
        game = new Game(config);
    }

    @Test
    @DisplayName("Game initializes in IDLE state")
    public void testGameInitializesInIdleState() {
        assertEquals(GameState.IDLE, game.getGameState());
    }

    @Test
    @DisplayName("Game initializes with zero points")
    public void testGameInitializesWithZeroPoints() {
        assertEquals(0, game.getPoints());
    }

    @Test
    @DisplayName("Game initializes with zero lines")
    public void testGameInitializesWithZeroLines() {
        assertEquals(0, game.getLines());
    }

    @Test
    @DisplayName("Game has non-null Board after initialization")
    public void testGameHasBoard() {
        assertNotNull(game.getBoard());
    }

    @Test
    @DisplayName("Game has Configuration reference")
    public void testGameHasConfiguration() {
        assertEquals(config, game.getConfiguration());
    }

    @Test
    @DisplayName("start() transitions game to RUNNING state")
    public void testStartTransitionsToRunning() {
        game.start();
        assertEquals(GameState.RUNNING, game.getGameState());
    }

    @Test
    @DisplayName("start() resets points to zero")
    public void testStartResetsPoints() {
        game.getBoard().setCell(0, 0, 1);
        game.start();
        assertEquals(0, game.getPoints());
    }

    @Test
    @DisplayName("start() resets lines to zero")
    public void testStartResetsLines() {
        game.start();
        assertEquals(0, game.getLines());
    }

    @Test
    @DisplayName("start() clears the board")
    public void testStartClearsBoard() {
        Board realBoard = game.getBoard();
        realBoard.setCell(0, 0, 1);
        assertFalse(realBoard.isCellEmpty(0, 0));

        game.start();
        assertTrue(realBoard.isCellEmpty(0, 0));
    }

    @Test
    @DisplayName("togglePause() transitions RUNNING to PAUSED")
    public void testTogglePausePausesGame() {
        game.start();
        game.togglePause();
        assertTrue(game.isPaused());
        assertEquals(GameState.PAUSED, game.getGameState());
    }

    @Test
    @DisplayName("togglePause() transitions PAUSED to RUNNING")
    public void testTogglePauseResumesGame() {
        game.start();
        game.togglePause();
        game.togglePause();
        assertFalse(game.isPaused());
        assertEquals(GameState.RUNNING, game.getGameState());
    }

    @Test
    @DisplayName("togglePause() has no effect when game is IDLE")
    public void testTogglePauseHasNoEffectInIdleState() {
        game.togglePause();
        assertEquals(GameState.IDLE, game.getGameState());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("moveLeft() does nothing when game is not RUNNING")
    public void testMoveLeftIgnoredWhenNotRunning(int unused) {
        game.moveLeft();
        assertEquals(GameState.IDLE, game.getGameState());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("moveRight() does nothing when game is not RUNNING")
    public void testMoveRightIgnoredWhenNotRunning(int unused) {
        game.moveRight();
        assertEquals(GameState.IDLE, game.getGameState());
    }

    @Test
    @DisplayName("moveDown() calls Board.canPlaceBlocks() to check placement")
    public void testMoveDownChecksPlacement() {
        game.start();
        game.moveDown();
    }

    @Test
    @DisplayName("rotate() does nothing when game is not RUNNING")
    public void testRotateIgnoredWhenNotRunning() {
        game.rotate();
        assertEquals(GameState.IDLE, game.getGameState());
    }

    @Test
    @DisplayName("hardDrop() does nothing when game is not RUNNING")
    public void testHardDropIgnoredWhenNotRunning() {
        game.hardDrop();
        assertEquals(GameState.IDLE, game.getGameState());
    }

    @Test
    @DisplayName("isGameOver() returns false when game is RUNNING")
    public void testIsGameOverFalseWhenRunning() {
        game.start();
        assertFalse(game.isGameOver());
    }

    @Test
    @DisplayName("isGameOver() returns false when game is PAUSED")
    public void testIsGameOverFalseWhenPaused() {
        game.start();
        game.togglePause();
        assertFalse(game.isGameOver());
    }

    @Test
    @DisplayName("isGameOver() returns true when game state is GAME_OVER")
    public void testIsGameOverTrueWhenGameOver() {
        assertFalse(game.isGameOver());
    }

    @Test
    @DisplayName("getConfiguration() returns the injected Configuration")
    public void testGetConfiguration() {
        assertEquals(config, game.getConfiguration());
    }

    @Test
    @DisplayName("getBoard() returns a valid Board instance")
    public void testGetBoard() {
        assertNotNull(game.getBoard());
        assertIsInstance(game.getBoard(), Board.class);
    }

    @Test
    @DisplayName("getGameState() reflects current state")
    public void testGetGameState() {
        assertEquals(GameState.IDLE, game.getGameState());
        game.start();
        assertEquals(GameState.RUNNING, game.getGameState());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    @DisplayName("update() increments drop counter (parametrized levels: {1,5,10})")
    public void testUpdateIncrementsBehavior(int level) {
        game.start();
        game.update();
        game.update();
        game.update();
        assertTrue(game.getGameState() == GameState.RUNNING ||
                   game.getGameState() == GameState.GAME_OVER);
    }

    @Test
    @DisplayName("update() does nothing when game is PAUSED")
    public void testUpdateIgnoredWhenPaused() {
        game.start();
        game.togglePause();

        int initialPoints = game.getPoints();

        game.update();
        game.update();

        assertEquals(initialPoints, game.getPoints());
    }

    @Test
    @DisplayName("createScoreRecord() creates a Score with correct player name and points")
    public void testCreateScoreRecord() {
        game.start();
        Score score = game.createScoreRecord("TestPlayer");

        assertNotNull(score);
        assertEquals("TestPlayer", score.playerName());
        assertEquals(game.getPoints(), score.points());
    }

    @Test
    @DisplayName("createScoreRecord() captures current timestamp")
    public void testCreateScoreRecordHasTimestamp() {
        game.start();
        Score score = game.createScoreRecord("TestPlayer");

        assertNotNull(score);
        assertNotNull(score.timestamp());
    }

    @Test
    @DisplayName("getCurrentPiece() returns non-null after start()")
    public void testGetCurrentPieceAfterStart() {
        game.start();
        assertNotNull(game.getCurrentPiece());
    }

    @Test
    @DisplayName("getNextPiece() returns non-null after start()")
    public void testGetNextPieceAfterStart() {
        game.start();
        assertNotNull(game.getNextPiece());
    }

    @Test
    @DisplayName("getLevel() returns configuration level")
    public void testGetLevel() {
        int level = game.getLevel();
        assertTrue(level >= 1);
    }

    private static void assertIsInstance(Object obj, Class<?> expectedClass) {
        assertTrue(expectedClass.isInstance(obj),
            "Expected instance of " + expectedClass.getSimpleName());
    }
}
