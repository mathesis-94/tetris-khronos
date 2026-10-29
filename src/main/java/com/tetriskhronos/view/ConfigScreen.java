package com.tetriskhronos.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import com.tetriskhronos.model.Configuration;
import com.tetriskhronos.model.Game;

public class ConfigScreen extends Screen {
    private final Game game;
    private final Runnable onBack;
    private Scene scene;

    public ConfigScreen(Game game, Runnable onBack, javafx.stage.Stage stage) {
        super(stage);
        this.game = game;
        this.onBack = onBack;
        createScene();
    }

    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a1a;");

        // Title
        Label title = new Label("CONFIGURATION");
        title.setTextFill(Color.web("#00ff00"));
        title.setFont(new Font("Arial", 32));
        BorderPane.setAlignment(title, javafx.geometry.Pos.CENTER);
        BorderPane.setMargin(title, new Insets(30, 0, 30, 0));
        root.setTop(title);

        // Config panel
        VBox configBox = new VBox(20);
        configBox.setPadding(new Insets(20));
        configBox.setStyle("-fx-background-color: #2a2a2a;");
        configBox.setAlignment(Pos.TOP_CENTER);
        configBox.setMaxWidth(500);

        Configuration config = game.getConfiguration();

        // Field Width
        VBox widthBox = createSpinnerOption("Field Width (5-15)",
            config.getFieldWidth(), 5, 15, value -> config.setFieldWidth(value));
        configBox.getChildren().add(widthBox);

        // Field Height
        VBox heightBox = createSpinnerOption("Field Height (15-30)",
            config.getFieldHeight(), 15, 30, value -> config.setFieldHeight(value));
        configBox.getChildren().add(heightBox);

        // Game Level
        VBox levelBox = createSpinnerOption("Game Level (1-10)",
            config.getGameLevel(), 1, 10, value -> config.setGameLevel(value));
        configBox.getChildren().add(levelBox);

        // Music toggle
        configBox.getChildren().add(createToggleOption("Music",
            config.isMusicEnabled(), enabled -> config.setMusicEnabled(enabled)));

        // Sound Effects toggle
        configBox.getChildren().add(createToggleOption("Sound Effects",
            config.isSoundEffectsEnabled(), enabled -> config.setSoundEffectsEnabled(enabled)));

        // AI Play toggle
        configBox.getChildren().add(createToggleOption("AI Play",
            config.isAiPlayEnabled(), enabled -> config.setAiPlayEnabled(enabled)));

        // Extend Mode toggle
        configBox.getChildren().add(createToggleOption("Extend Mode",
            config.isExtendModeEnabled(), enabled -> config.setExtendModeEnabled(enabled)));

        // Back button
        Button backButton = new Button("BACK");
        backButton.setStyle("-fx-font-size: 14; -fx-padding: 10; -fx-background-color: #444444; -fx-text-fill: white;");
        backButton.setOnAction(e -> onBack.run());
        configBox.getChildren().add(new Separator());
        configBox.getChildren().add(backButton);

        root.setCenter(configBox);
        scene = new Scene(root, 600, 700);
    }

    private VBox createSpinnerOption(String label, int initialValue, int min, int max, java.util.function.Consumer<Integer> onChanged) {
        VBox box = new VBox(8);
        Label labelNode = new Label(label);
        labelNode.setTextFill(Color.WHITE);
        labelNode.setFont(new Font("Arial", 14));

        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue);
        Spinner<Integer> spinner = new Spinner<>(factory);
        spinner.setStyle("-fx-font-size: 12;");
        spinner.setPrefWidth(200);
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> onChanged.accept(newVal));

        box.getChildren().addAll(labelNode, spinner);
        return box;
    }

    private VBox createToggleOption(String label, boolean initialValue, java.util.function.Consumer<Boolean> onChanged) {
        VBox box = new VBox(8);
        Label labelNode = new Label(label);
        labelNode.setTextFill(Color.WHITE);
        labelNode.setFont(new Font("Arial", 14));

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(initialValue);
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> onChanged.accept(newVal));

        box.getChildren().addAll(labelNode, checkBox);
        return box;
    }

    @Override
    public void show() {
        // Handled by screen manager
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public void onHide() {
        // Cleanup if needed
    }
}
