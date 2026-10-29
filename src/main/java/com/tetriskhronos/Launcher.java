package com.tetriskhronos;

import com.tetriskhronos.ai.AIPlayer;
import com.tetriskhronos.ai.TrainingMode;
import com.tetriskhronos.model.Configuration;
import com.tetriskhronos.model.Game;

import java.io.IOException;

public class Launcher {
    private static final int DEFAULT_TRAINING_EPISODES = 1000;
    private static final int DEFAULT_SAVE_INTERVAL = 100;

    private static int[] parseArguments(String[] args) {
        boolean isTraining = false;
        int numEpisodes = DEFAULT_TRAINING_EPISODES;
        int saveInterval = DEFAULT_SAVE_INTERVAL;

        for (int i = 0; i < args.length; i++) {
            if ("--train".equals(args[i])) {
                isTraining = true;
            } else if ("--episodes".equals(args[i]) && i + 1 < args.length) {
                try {
                    numEpisodes = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid episode count, using default: " + DEFAULT_TRAINING_EPISODES);
                }
            } else if ("--save-interval".equals(args[i]) && i + 1 < args.length) {
                try {
                    saveInterval = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid save interval, using default: " + DEFAULT_SAVE_INTERVAL);
                }
            }
        }

        return new int[]{isTraining ? 1 : 0, numEpisodes, saveInterval};
    }

    public static void main(String[] args) {
        int[] parsedArgs = parseArguments(args);
        boolean isTraining = parsedArgs[0] == 1;
        int numEpisodes = parsedArgs[1];
        int saveInterval = parsedArgs[2];

        if (isTraining) {
            System.setProperty("java.awt.headless", "true");
            System.out.println("Starting headless training mode...");
            System.out.println("Episodes: " + numEpisodes + ", Save interval: " + saveInterval);

            Configuration configuration = Configuration.getInstance();
            AIPlayer aiPlayer = new AIPlayer();
            Game gameTemplate = new Game(configuration);

            TrainingMode trainingMode = new TrainingMode(aiPlayer, numEpisodes, saveInterval, gameTemplate);

            try {
                trainingMode.runTrainingSession();
            } catch (IOException e) {
                System.err.println("Error during training: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("Training completed.");
            System.exit(0);
        } else {
            App.launch(App.class, args);
        }
    }
}
