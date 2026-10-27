package com.tetriskhronos.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import com.tetriskhronos.controller.ScreenManager;
import com.tetriskhronos.controller.ScreenType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
@DisplayName("MainMenuScreen Tests")
class MainMenuScreenTest {

    @Mock
    private ScreenManager screenManager;
    private Stage mockStage;
    private MainMenuScreen mainMenuScreen;


    @Start
    void start(Stage stage) {
        this.mockStage = stage;
    }


    @BeforeEach
    void setUp() {
        when(screenManager.getPrimaryStage()).thenReturn(mockStage);
        mainMenuScreen = new MainMenuScreen(screenManager);
    }


    @Test
    @DisplayName("Should create MainMenuScreen with valid ScreenManager")
    void testConstructorWithValidScreenManager() {
        assertNotNull(mainMenuScreen, "MainMenuScreen should be instantiated");
        verify(screenManager).getPrimaryStage();
    }

    @Test
    @DisplayName("Should initialize scene with correct dimensions (600x700)")
    void testSceneDimensions() {
        Scene scene = mainMenuScreen.getScene();

        assertNotNull(scene, "Scene should not be null");
        assertEquals(600, scene.getWidth(), "Scene width should be 600");
        assertEquals(700, scene.getHeight(), "Scene height should be 700");
    }

    @Test
    @DisplayName("Should have BorderPane as root node")
    void testSceneRootNode() {
        Scene scene = mainMenuScreen.getScene();

        assertNotNull(scene.getRoot(), "Scene should have a root node");
        assertTrue(scene.getRoot() instanceof BorderPane,
            "Root should be a BorderPane");
    }

    @Test
    @DisplayName("Should return same Scene instance on multiple calls")
    void testGetSceneReturnsSameInstance() {
        Scene scene1 = mainMenuScreen.getScene();
        Scene scene2 = mainMenuScreen.getScene();

        assertSame(scene1, scene2, "getScene() should return the same instance");
    }

    @Test
    @DisplayName("Should switch to PLAYING screen when play button is clicked")
    void testPlayButtonClicked() {
        mainMenuScreen.onPlayClicked();

        verify(screenManager).switchTo(ScreenType.PLAYING);
    }

    @Test
    @DisplayName("Should switch to CONFIG screen when config button is clicked")
    void testConfigButtonClicked() {
        mainMenuScreen.onConfigClicked();

        verify(screenManager).switchTo(ScreenType.CONFIG);
    }

    @Test
    @DisplayName("Should switch to HIGH_SCORES screen when scores button is clicked")
    void testScoresButtonClicked() {
        mainMenuScreen.onScoresClicked();

        verify(screenManager).switchTo(ScreenType.HIGH_SCORES);
    }

    @Test
    @DisplayName("Should create server status label with non-null text")
    void testServerStatusLabelCreation() {
        Label label = mainMenuScreen.createServerStatusLabel();

        assertNotNull(label, "Server status label should not be null");
        assertNotNull(label.getText(), "Label text should not be null");
    }

    @Test
    @DisplayName("Should create label containing 'Server' text")
    void testServerStatusLabelContainsServerText() {
        Label label = mainMenuScreen.createServerStatusLabel();

        assertTrue(label.getText().contains("Server"),
            "Label should contain 'Server' text");
    }

    @Test
    @DisplayName("Should apply correct styling to server status label")
    void testServerStatusLabelStyling() {
        Label label = mainMenuScreen.createServerStatusLabel();
        String style = label.getStyle();

        assertNotNull(style, "Label style should not be null");
        assertTrue(style.contains("-fx-font-size"),
            "Label should have font-size styling");
        assertTrue(style.contains("-fx-text-fill"),
            "Label should have text-fill styling");
    }

    @Test
    @DisplayName("Should not throw exception on show()")
    void testShowMethodDoesNotThrow() {
        assertDoesNotThrow(() -> mainMenuScreen.show(),
            "show() should not throw exceptions");
    }

    @Test
    @DisplayName("Should not throw exception on onHide()")
    void testOnHideMethodDoesNotThrow() {
        assertDoesNotThrow(() -> mainMenuScreen.onHide(),
            "onHide() should not throw exceptions");
    }
}
