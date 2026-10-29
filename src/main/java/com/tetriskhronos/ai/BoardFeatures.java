package com.tetriskhronos.ai;

import com.tetriskhronos.model.Board;
import java.io.*;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Queue;

public class BoardFeatures {
    private final Board board;
    private static final int MAX_HEIGHT = 20;
    private static final int FEATURE_COUNT = 28;

    private double[] featureMinValues;
    private double[] featureMaxValues;
    private boolean statisticsInitialized = false;

    public BoardFeatures(Board board) {
        this.board = board;
        initializeStatistics();
    }

    private void initializeStatistics() {
        featureMinValues = new double[FEATURE_COUNT];
        featureMaxValues = new double[FEATURE_COUNT];

        for (int i = 0; i < FEATURE_COUNT; i++) {
            featureMinValues[i] = Double.POSITIVE_INFINITY;
            featureMaxValues[i] = Double.NEGATIVE_INFINITY;
        }
    }

    public double[] getFeatureVector() {
        double[] rawFeatures = computeRawFeatures();

        if (!statisticsInitialized) {
            updateStatistics(rawFeatures);
        }

        return normalizeFeatures(rawFeatures);
    }

    private double[] computeRawFeatures() {
        double[] features = new double[FEATURE_COUNT];
        int[][] grid = board.getGrid();
        int width = board.getWidth();
        int height = board.getHeight();

        int[] columnHeights = calculateColumnHeights(grid, width, height);

        features[0] = maxHeight(columnHeights);
        features[1] = avgHeight(columnHeights);
        features[2] = heightVariance(columnHeights);

        int holes = countHoles(grid, columnHeights, width, height);
        features[3] = holes;

        int isolatedHoles = countIsolatedHoles(grid, columnHeights, width, height);
        features[4] = isolatedHoles;

        int deepHoles = countDeepHoles(grid, columnHeights, width, height);
        features[5] = deepHoles;

        int bumpiness = calculateBumpiness(columnHeights);
        features[6] = bumpiness;

        boolean hasWell = hasWell(columnHeights);
        features[7] = hasWell ? 1.0 : 0.0;

        int wellDepth = calculateWellDepth(columnHeights);
        features[8] = wellDepth;

        int compactness = calculateCompactness(grid, columnHeights, width, height);
        features[9] = compactness;

        int[] filledCells = countFilledCells(grid, width, height);
        features[10] = filledCells[0];
        features[11] = filledCells[1];

        int transitions = countRowTransitions(grid, width, height);
        features[12] = transitions;

        int colTransitions = countColumnTransitions(grid, columnHeights, width);
        features[13] = colTransitions;

        for (int i = 0; i < Math.min(14, width); i++) {
            features[14 + i] = columnHeights.length > i ? columnHeights[i] : 0.0;
        }

        return features;
    }
    public void saveStatistics(String filePath) throws IOException {
        saveNormalizationStatistics(filePath);
    }

    public void loadStatistics(String filePath) throws IOException, ClassNotFoundException {
        loadNormalizationStatistics(filePath);
    }

    public double[] getRawFeatureVector() {
        return computeRawFeatures();
    }

    public double[] normalizeFeatures(double[] rawFeatures) {
        double[] normalized = new double[FEATURE_COUNT];

        for (int i = 0; i < FEATURE_COUNT; i++) {
            double min = featureMinValues[i];
            double max = featureMaxValues[i];
            double value = rawFeatures[i];

            if (min == Double.POSITIVE_INFINITY || max == Double.NEGATIVE_INFINITY) {
                normalized[i] = 0.0;
            } else if (Math.abs(max - min) < 1e-9) {
                normalized[i] = 0.0;
            } else {
                normalized[i] = 2.0 * (value - min) / (max - min) - 1.0;
                normalized[i] = Math.max(-1.0, Math.min(1.0, normalized[i]));
            }
        }

        return normalized;
    }

    private void updateStatistics(double[] rawFeatures) {
        for (int i = 0; i < FEATURE_COUNT; i++) {
            double value = rawFeatures[i];
            if (!Double.isNaN(value) && !Double.isInfinite(value)) {
                featureMinValues[i] = Math.min(featureMinValues[i], value);
                featureMaxValues[i] = Math.max(featureMaxValues[i], value);
            }
        }
    }


    public void setStatisticsLocked(boolean locked) {
        if (locked) {
            finalizeStatistics();
        } else {
            resetStatistics();
        }
    }

    public void resetStatistics() {
        initializeStatistics();
        statisticsInitialized = false;
    }

    public double[] getFeatureMinValues() {
        return featureMinValues.clone();
    }

    public double[] getFeatureMaxValues() {
        return featureMaxValues.clone();
    }

    public boolean isStatisticsInitialized() {
        return statisticsInitialized;
    }

    public void finalizeStatistics() {
        statisticsInitialized = true;
    }

    public void saveNormalizationStatistics(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
            oos.writeObject(featureMinValues);
            oos.writeObject(featureMaxValues);
            oos.writeBoolean(statisticsInitialized);
        }
    }

    public void loadNormalizationStatistics(String filePath) throws IOException, ClassNotFoundException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            featureMinValues = (double[]) ois.readObject();
            featureMaxValues = (double[]) ois.readObject();
            statisticsInitialized = ois.readBoolean();
        }
    }

    private int[] calculateColumnHeights(int[][] grid, int width, int height) {
        int[] heights = new int[width];
        for (int col = 0; col < width; col++) {
            heights[col] = 0;
            for (int row = 0; row < height; row++) {
                if (grid[row][col] != 0) {
                    heights[col] = height - row;
                    break;
                }
            }
        }
        return heights;
    }

    private int maxHeight(int[] heights) {
        int max = 0;
        for (int h : heights) {
            max = Math.max(max, h);
        }
        return max;
    }

    private double avgHeight(int[] heights) {
        int sum = 0;
        for (int h : heights) {
            sum += h;
        }
        return heights.length > 0 ? (double) sum / heights.length : 0;
    }

    private double heightVariance(int[] heights) {
        double avg = avgHeight(heights);
        double variance = 0;
        for (int h : heights) {
            variance += Math.pow(h - avg, 2);
        }
        return heights.length > 0 ? Math.sqrt(variance / heights.length) : 0;
    }

    private int countHoles(int[][] grid, int[] heights, int width, int height) {
        int holeCount = 0;
        for (int col = 0; col < width; col++) {
            for (int row = height - heights[col]; row < height; row++) {
                if (grid[row][col] == 0) {
                    holeCount++;
                }
            }
        }
        return holeCount;
    }

    private int countIsolatedHoles(int[][] grid, int[] heights, int width, int height) {
        int isolatedCount = 0;
        boolean[][] visited = new boolean[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (grid[row][col] == 0 && !visited[row][col]) {
                    int regionSize = floodFillHoles(grid, visited, row, col, height, width);
                    if (regionSize == 1) {
                        isolatedCount++;
                    }
                }
            }
        }
        return isolatedCount;
    }

    private int floodFillHoles(int[][] grid, boolean[][] visited, int startRow, int startCol, int height, int width) {
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        int count = 0;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int row = cell[0];
            int col = cell[1];
            count++;

            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                if (newRow >= 0 && newRow < height && newCol >= 0 && newCol < width &&
                    !visited[newRow][newCol] && grid[newRow][newCol] == 0) {
                    visited[newRow][newCol] = true;
                    queue.add(new int[]{newRow, newCol});
                }
            }
        }
        return count;
    }

    private int countDeepHoles(int[][] grid, int[] heights, int width, int height) {
        int deepCount = 0;
        for (int col = 0; col < width; col++) {
            int holeDepth = 0;
            for (int row = height - heights[col]; row < height; row++) {
                if (grid[row][col] == 0) {
                    holeDepth++;
                }
            }
            if (holeDepth > 5) {
                deepCount += holeDepth;
            }
        }
        return deepCount;
    }

    private int calculateBumpiness(int[] heights) {
        int bumpiness = 0;
        for (int i = 0; i < heights.length - 1; i++) {
            bumpiness += Math.abs(heights[i] - heights[i + 1]);
        }
        return bumpiness;
    }

    private boolean hasWell(int[] heights) {
        if (heights.length < 3) return false;
        for (int i = 1; i < heights.length - 1; i++) {
            if (heights[i] < heights[i - 1] && heights[i] < heights[i + 1]) {
                return true;
            }
        }
        return false;
    }

    private int calculateWellDepth(int[] heights) {
        int maxDepth = 0;
        for (int i = 1; i < heights.length - 1; i++) {
            int depth = Math.min(heights[i - 1], heights[i + 1]) - heights[i];
            maxDepth = Math.max(maxDepth, depth);
        }
        return maxDepth;
    }

    private int calculateCompactness(int[][] grid, int[] heights, int width, int height) {
        int emptySpaces = 0;
        for (int col = 0; col < width; col++) {
            for (int row = height - heights[col]; row < height; row++) {
                if (grid[row][col] == 0) {
                    emptySpaces++;
                }
            }
        }
        return emptySpaces;
    }

    private int[] countFilledCells(int[][] grid, int width, int height) {
        int filled = 0;
        int topHalf = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (grid[row][col] != 0) {
                    filled++;
                    if (row < height / 2) {
                        topHalf++;
                    }
                }
            }
        }
        return new int[]{filled, topHalf};
    }

    private int countRowTransitions(int[][] grid, int width, int height) {
        int transitions = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width - 1; col++) {
                if ((grid[row][col] != 0) != (grid[row][col + 1] != 0)) {
                    transitions++;
                }
            }
        }
        return transitions;
    }

    private int countColumnTransitions(int[][] grid, int[] heights, int width) {
        int transitions = 0;
        for (int col = 0; col < width - 1; col++) {
            if (Math.abs(heights[col] - heights[col + 1]) > 0) {
                transitions++;
            }
        }
        return transitions;
    }
}
