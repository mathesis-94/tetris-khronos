package com.tetriskhronos.view;

import com.tetriskhronos.controller.ScreenManager;
import com.tetriskhronos.controller.ScreenType;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;

public class HighScoreScreen extends Screen {
    private final Scene scene;
    private final ScreenManager screenManager;
    private VBox scoresContainer;

    public HighScoreScreen(ScreenManager screenManager) {
        super(screenManager.getPrimaryStage());
        this.screenManager = screenManager;

        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #1a1a1a;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new javafx.geometry.Insets(20));

        Label titleLabel = new Label("HIGH SCORES");
        titleLabel.setStyle("-fx-text-fill: #00FFFF; -fx-font-size: 32; -fx-font-weight: bold;");

        // Container for scores
        scoresContainer = new VBox(10);
        scoresContainer.setStyle("-fx-padding: 20;");
        scoresContainer.setPrefWidth(500);
        scoresContainer.setAlignment(Pos.TOP_CENTER);

        Button backButton = new Button("BACK");
        backButton.setStyle("-fx-font-size: 16; -fx-padding: 10 30;");
        backButton.setOnAction(e -> screenManager.switchTo(ScreenType.MAIN_MENU));

        root.getChildren().addAll(titleLabel, scoresContainer, backButton);
        this.scene = new Scene(root, 600, 700);
    }

    @Override
    public void show() {
        loadAndDisplayHighScores();
    }

    private void loadAndDisplayHighScores() {
        scoresContainer.getChildren().clear();

        try {
            // Load highscores.json
            String jsonContent = new String(Files.readAllBytes(
                Paths.get("src/main/resources/highscores.json")
            ));

            JSONArray scoresArray = new JSONArray(jsonContent);

            if (scoresArray.length() == 0) {
                Label noScoresLabel = new Label("No scores yet");
                noScoresLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 16;");
                scoresContainer.getChildren().add(noScoresLabel);
                return;
            }

            // Add header row
            HBox header = createScoreRow("#", "PLAYER", "SCORE", true);
            scoresContainer.getChildren().add(header);

            // Add score rows (limit to 10)
            for (int i = 0; i < Math.min(scoresArray.length(), 10); i++) {
                JSONObject entry = scoresArray.getJSONObject(i);
                String name = entry.getString("name");
                int score = entry.getInt("score");

                HBox scoreRow = createScoreRow(String.valueOf(i + 1), name, String.valueOf(score), false);
                scoresContainer.getChildren().add(scoreRow);
            }

        } catch (Exception e) {
            System.err.println("Error loading high scores: " + e.getMessage());
            Label errorLabel = new Label("Error loading scores");
            errorLabel.setStyle("-fx-text-fill: #FF4444; -fx-font-size: 14;");
            scoresContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createScoreRow(String rank, String name, String score, boolean isHeader) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);
        row.setPrefWidth(500);

        Label rankLabel = new Label(rank);
        rankLabel.setPrefWidth(50);
        rankLabel.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(name);
        nameLabel.setPrefWidth(250);
        nameLabel.setAlignment(Pos.CENTER_LEFT);

        Label scoreLabel = new Label(score);
        scoreLabel.setPrefWidth(150);
        scoreLabel.setAlignment(Pos.CENTER_RIGHT);

        if (isHeader) {
            rankLabel.setStyle("-fx-text-fill: #00FFFF; -fx-font-weight: bold; -fx-font-size: 14;");
            nameLabel.setStyle("-fx-text-fill: #00FFFF; -fx-font-weight: bold; -fx-font-size: 14;");
            scoreLabel.setStyle("-fx-text-fill: #00FFFF; -fx-font-weight: bold; -fx-font-size: 14;");
            row.setStyle("-fx-border-color: #00FFFF; -fx-border-width: 0 0 2 0; -fx-padding: 10;");
        } else {
            rankLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 13;");
            nameLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 13;");
            scoreLabel.setStyle("-fx-text-fill: #FFFF00; -fx-font-weight: bold; -fx-font-size: 13;");
            row.setStyle("-fx-padding: 8;");
        }

        row.getChildren().addAll(rankLabel, nameLabel, scoreLabel);
        return row;
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public void onHide() {}
}
