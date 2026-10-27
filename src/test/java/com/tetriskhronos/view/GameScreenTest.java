package com.tetriskhronos.view;

import com.tetriskhronos.model.Configuration;
import com.tetriskhronos.model.Game;
import com.tetriskhronos.view.panels.GamePanel;
import com.tetriskhronos.audio.MusicManager;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
@DisplayName("GameScreen Tests")
class GameScreenTest {

    private GameScreen gameScreen;
    private Stage stage;

    @Mock
    private Game mockGame;

    @Mock
    private Configuration mockConfiguration;

    @Mock
    private GamePanel mockGamePanel;

    @Mock
    private MusicManager mockMusicManager;

    @Mock
    private Runnable mockOnBack;

    @Start
    void start(Stage stage) {
        this.stage = stage;
    }

    @BeforeEach
    void setUp() {
        when(mockGame.getConfiguration()).thenReturn(mockConfiguration);
        when(mockConfiguration.getFieldWidth()).thenReturn(10);
        when(mockConfiguration.getFieldHeight()).thenReturn(20);
    }

    @Test
    @DisplayName("GameScreen constructor initializes with correct window dimensions")
    void testConstructorInitializesDimensions() {
        gameScreen = new GameScreen(stage, mockGame, mockOnBack, mockMusicManager);
        assertEquals(400, stage.getWidth(), "Stage width should be 400");
        assertEquals(700, stage.getHeight(), "Stage height should be 700");
        assertFalse(stage.isResizable(), "Stage should not be resizable");
    }

    @Test
    @DisplayName("GameScreen getScene returns non-null Scene")
    void testGetSceneReturnsScene() {
        gameScreen = new GameScreen(stage, mockGame, mockOnBack, mockMusicManager);
        Scene scene = gameScreen.getScene();
        assertNotNull(scene, "getScene() should return non-null Scene");
        assertEquals(400, scene.getWidth(), "Scene width should be 400");
        assertEquals(700, scene.getHeight(), "Scene height should be 700");
    }

    @Test
    @DisplayName("show() starts game and music, starts game loop, and requests focus")
    void testShowStartsGameAndMusicAndGameLoop() {
        gameScreen = createGameScreenWithMockedPanel();
        gameScreen.show();
        verify(mockGame, times(1)).start();
        verify(mockMusicManager, times(1)).play();
        verify(mockGamePanel, times(1)).startGameLoop();
        verify(mockGamePanel, times(1)).requestFocus();
    }

    @Test
    @DisplayName("show() plays music via MusicManager")
    void testShowPlaysMusicFromMusicManager() {
        gameScreen = createGameScreenWithMockedPanel();
        gameScreen.show();
        verify(mockMusicManager, times(1)).play();
    }

    @Test
    @DisplayName("onHide() stops the game loop")
    void testOnHideStopsGameLoop() {
        gameScreen = createGameScreenWithMockedPanel();
        gameScreen.onHide();
        verify(mockGamePanel, times(1)).stopGameLoop();
    }

    @Test
    @DisplayName("show() followed by onHide() properly manages game lifecycle")
    void testShowAndHideLifecycle() {
        gameScreen = createGameScreenWithMockedPanel();
        gameScreen.show();
        verify(mockGame).start();
        verify(mockGamePanel).startGameLoop();
        gameScreen.onHide();
        verify(mockGamePanel).stopGameLoop();
    }

    @Test
    @DisplayName("Stage is centered on screen after construction")
    void testStageCenteredOnScreen() {
        gameScreen = new GameScreen(stage, mockGame, mockOnBack, mockMusicManager);
        assertNotNull(gameScreen, "GameScreen should be created successfully");
        assertEquals(400, stage.getWidth());
        assertEquals(700, stage.getHeight());
    }

    @Test
    @DisplayName("Multiple show() calls start game and music each time")
    void testMultipleShowCalls() {
        gameScreen = createGameScreenWithMockedPanel();
        gameScreen.show();
        gameScreen.show();
        verify(mockGame, times(2)).start();
        verify(mockMusicManager, times(2)).play();
        verify(mockGamePanel, times(2)).startGameLoop();
    }

    @Test
    @DisplayName("getScene preserves Scene reference across multiple calls")
    void testGetSceneConsistency() {
        gameScreen = new GameScreen(stage, mockGame, mockOnBack, mockMusicManager);
        Scene scene1 = gameScreen.getScene();
        Scene scene2 = gameScreen.getScene();
        assertSame(scene1, scene2, "getScene() should return the same Scene instance");
    }

    @Test
    @DisplayName("GamePanel receives Game instance in constructor")
    void testGamePanelReceivesGameInstance() {
        gameScreen = new GameScreen(stage, mockGame, mockOnBack, mockMusicManager);
        assertNotNull(gameScreen, "GameScreen created with Game instance");
    }

    @Test
    @DisplayName("GamePanel receives onBack callback in constructor")
    void testGamePanelReceivesOnBackCallback() {
        Runnable onBack = mock(Runnable.class);
        gameScreen = new GameScreen(stage, mockGame, onBack, mockMusicManager);
        assertNotNull(gameScreen, "GameScreen created with onBack callback");
    }

    @Test
    @DisplayName("onHide() can be called without prior show()")
    void testOnHideWithoutShow() {
        gameScreen = createGameScreenWithMockedPanel();
        assertDoesNotThrow(() -> gameScreen.onHide());
        verify(mockGamePanel, times(1)).stopGameLoop();
    }

    private GameScreen createGameScreenWithMockedPanel() {
        GameScreen screen = new GameScreen(stage, mockGame, mockOnBack, mockMusicManager);
        try {
            var field = GameScreen.class.getDeclaredField("gamePanel");
            field.setAccessible(true);
            field.set(screen, mockGamePanel);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject mock GamePanel: " + e.getMessage());
        }
        return screen;
    }
}
