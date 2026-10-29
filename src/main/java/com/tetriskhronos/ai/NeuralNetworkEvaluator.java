package com.tetriskhronos.ai;

import java.io.*;
import java.nio.file.*;

public class NeuralNetworkEvaluator {
    // Network architecture constants
    private static final int INPUT_SIZE = 28;
    private static final int HIDDEN1_SIZE = 64;
    private static final int HIDDEN2_SIZE = 32;
    private static final int OUTPUT_SIZE = 1;

    // Initialization constants
    private static final double INIT_STDDEV = 0.01;
    private static final double OUTPUT_SCALING_FACTOR = 10.0;
    private static final double OUTPUT_AMPLITUDE = 100.0;

    // Weight matrices
    private double[][] weightsInputHidden1;
    private double[][] weightsHidden1Hidden2;
    private double[][] weightsHidden2Output;

    // Bias vectors
    private double[] biasHidden1;
    private double[] biasHidden2;
    private double[] biasOutput;

    // Activation caches (used during forward pass and backprop)
    private double[] hidden1Activations;
    private double[] hidden2Activations;

    // Input cache for backpropagation
    private double[] lastInputFeatures;

    public NeuralNetworkEvaluator() {
        initialize();
    }

    /**
     * Initializes all weight matrices and bias vectors.
     */
    private void initialize() {
        weightsInputHidden1 = new double[INPUT_SIZE][HIDDEN1_SIZE];
        weightsHidden1Hidden2 = new double[HIDDEN1_SIZE][HIDDEN2_SIZE];
        weightsHidden2Output = new double[HIDDEN2_SIZE][OUTPUT_SIZE];

        biasHidden1 = new double[HIDDEN1_SIZE];
        biasHidden2 = new double[HIDDEN2_SIZE];
        biasOutput = new double[OUTPUT_SIZE];

        hidden1Activations = new double[HIDDEN1_SIZE];
        hidden2Activations = new double[HIDDEN2_SIZE];

        lastInputFeatures = new double[INPUT_SIZE];

        initializeGaussian();
    }

    /**
     * Initializes weights and biases with Gaussian distribution (mean=0, stdDev=0.01).
     */
    private void initializeGaussian() {
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                weightsInputHidden1[i][j] = gaussianRandom(0, INIT_STDDEV);
            }
        }
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < HIDDEN2_SIZE; j++) {
                weightsHidden1Hidden2[i][j] = gaussianRandom(0, INIT_STDDEV);
            }
        }
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            weightsHidden2Output[i][0] = gaussianRandom(0, INIT_STDDEV);
        }
        // Biases are initialized to 0 by default
    }

    /**
     * Box-Muller transform to generate Gaussian-distributed random numbers.
     */
    private double gaussianRandom(double mean, double stdDev) {
        return mean + stdDev * Math.sqrt(-2.0 * Math.log(Math.random()))
               * Math.cos(2.0 * Math.PI * Math.random());
    }

    /**
     * Forward pass through the network with activation caching.
     *
     * @param features Input features (28 board features)
     * @return Scaled output value in range [-100, 100]
     */
    public double evaluate(double[] features) {
        if (features.length != INPUT_SIZE) {
            throw new IllegalArgumentException("Expected " + INPUT_SIZE + " features, got " + features.length);
        }

        // Cache input for backpropagation
        System.arraycopy(features, 0, lastInputFeatures, 0, INPUT_SIZE);

        // Hidden layer 1: fully connected + ReLU
        for (int j = 0; j < HIDDEN1_SIZE; j++) {
            double sum = biasHidden1[j];
            for (int i = 0; i < INPUT_SIZE; i++) {
                sum += features[i] * weightsInputHidden1[i][j];
            }
            hidden1Activations[j] = relu(sum);
        }

        // Hidden layer 2: fully connected + ReLU
        for (int j = 0; j < HIDDEN2_SIZE; j++) {
            double sum = biasHidden2[j];
            for (int i = 0; i < HIDDEN1_SIZE; i++) {
                sum += hidden1Activations[i] * weightsHidden1Hidden2[i][j];
            }
            hidden2Activations[j] = relu(sum);
        }

        // Output layer: fully connected + tanh scaling
        double rawOutput = biasOutput[0];
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            rawOutput += hidden2Activations[i] * weightsHidden2Output[i][0];
        }

        return Math.tanh(rawOutput / OUTPUT_SCALING_FACTOR) * OUTPUT_AMPLITUDE;
    }

    /**
     * ReLU activation function: max(0, x)
     */
    private double relu(double x) {
        return Math.max(0, x);
    }

    /**
     * ReLU derivative for backpropagation.
     */
    private double reluDerivative(double x) {
        return x > 0 ? 1.0 : 0.0;
    }

    // ===================== GETTER METHODS (for TemporalDifferenceLearner) =====================

    /**
     * Get the last cached input features (used for backpropagation).
     */
    public double[] getLastInputFeatures() {
        return lastInputFeatures;
    }

    /**
     * Get cached activations from hidden layer 1.
     */
    public double[] getHidden1Activations() {
        return hidden1Activations;
    }

    /**
     * Get cached activations from hidden layer 2.
     */
    public double[] getHidden2Activations() {
        return hidden2Activations;
    }

    /**
     * Get weights from input to hidden layer 1.
     */
    public double[][] getWeightsInputHidden1() {
        return weightsInputHidden1;
    }

    /**
     * Get weights from hidden layer 1 to hidden layer 2.
     */
    public double[][] getWeightsHidden1Hidden2() {
        return weightsHidden1Hidden2;
    }

    /**
     * Get weights from hidden layer 2 to output.
     */
    public double[][] getWeightsHidden2Output() {
        return weightsHidden2Output;
    }

    /**
     * Get bias for hidden layer 1.
     */
    public double[] getBiasHidden1() {
        return biasHidden1;
    }

    /**
     * Get bias for hidden layer 2.
     */
    public double[] getBiasHidden2() {
        return biasHidden2;
    }

    /**
     * Get bias for output layer.
     */
    public double[] getBiasOutput() {
        return biasOutput;
    }

    /**
     * Get network architecture information.
     */
    public int[] getLayerSizes() {
        return new int[]{INPUT_SIZE, HIDDEN1_SIZE, HIDDEN2_SIZE, OUTPUT_SIZE};
    }

    // ===================== WEIGHT MANAGEMENT =====================

    /**
     * Get all weights flattened into arrays (for serialization or analysis).
     */
    public double[][] getWeights() {
        double[][] allWeights = new double[3][];
        allWeights[0] = flattenWeights(weightsInputHidden1);
        allWeights[1] = flattenWeights(weightsHidden1Hidden2);
        allWeights[2] = flattenWeights(weightsHidden2Output);
        return allWeights;
    }

    /**
     * Set all weights from flattened arrays.
     */
    public void setWeights(double[][] allWeights) {
        weightsInputHidden1 = reshapeWeights(allWeights[0], INPUT_SIZE, HIDDEN1_SIZE);
        weightsHidden1Hidden2 = reshapeWeights(allWeights[1], HIDDEN1_SIZE, HIDDEN2_SIZE);
        weightsHidden2Output = reshapeWeights(allWeights[2], HIDDEN2_SIZE, OUTPUT_SIZE);
    }

    /**
     * Flatten a 2D weight matrix into a 1D array.
     */
    private double[] flattenWeights(double[][] matrix) {
        double[] flat = new double[matrix.length * matrix[0].length];
        int idx = 0;
        for (double[] row : matrix) {
            for (double w : row) {
                flat[idx++] = w;
            }
        }
        return flat;
    }

    /**
     * Reshape a 1D array into a 2D weight matrix.
     */
    private double[][] reshapeWeights(double[] flat, int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        int idx = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = flat[idx++];
            }
        }
        return matrix;
    }

    // ===================== PERSISTENCE =====================

    /**
     * Save all weights and biases to a file.
     */
    public void saveWeights(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
            oos.writeObject(flattenWeights(weightsInputHidden1));
            oos.writeObject(flattenWeights(weightsHidden1Hidden2));
            oos.writeObject(flattenWeights(weightsHidden2Output));
            oos.writeObject(biasHidden1);
            oos.writeObject(biasHidden2);
            oos.writeObject(biasOutput);
        }
    }

    /**
     * Load weights and biases from a file. Gracefully returns if file doesn't exist.
     */
    public void loadWeights(String filePath) throws IOException, ClassNotFoundException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            double[] w1 = (double[]) ois.readObject();
            double[] w2 = (double[]) ois.readObject();
            double[] w3 = (double[]) ois.readObject();
            biasHidden1 = (double[]) ois.readObject();
            biasHidden2 = (double[]) ois.readObject();
            biasOutput = (double[]) ois.readObject();

            weightsInputHidden1 = reshapeWeights(w1, INPUT_SIZE, HIDDEN1_SIZE);
            weightsHidden1Hidden2 = reshapeWeights(w2, HIDDEN1_SIZE, HIDDEN2_SIZE);
            weightsHidden2Output = reshapeWeights(w3, HIDDEN2_SIZE, OUTPUT_SIZE);
        }
    }

    // ===================== GRADIENT APPLICATION =====================

    /**
     * Apply computed gradients with clipping to prevent exploding gradients.
     *
     * @param gradients Array of gradient matrices: [w1, w2, w3, b1, b2, b3]
     * @param learningRate Learning rate for gradient descent
     * @param clipNorm Maximum allowed gradient norm (for stability)
     */
    public void applyGradients(double[][] gradients, double learningRate, double clipNorm) {
        // Compute and apply gradient clipping
        double norm = calculateGradientNorm(gradients);
        double scaleFactor = norm > clipNorm ? clipNorm / norm : 1.0;

        // Update weights: input -> hidden1
        int idx = 0;
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                weightsInputHidden1[i][j] -= learningRate * gradients[0][idx] * scaleFactor;
                idx++;
            }
        }

        // Update weights: hidden1 -> hidden2
        idx = 0;
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < HIDDEN2_SIZE; j++) {
                weightsHidden1Hidden2[i][j] -= learningRate * gradients[1][idx] * scaleFactor;
                idx++;
            }
        }

        // Update weights: hidden2 -> output
        idx = 0;
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            weightsHidden2Output[i][0] -= learningRate * gradients[2][idx] * scaleFactor;
            idx++;
        }

        // Update biases
        for (int j = 0; j < HIDDEN1_SIZE; j++) {
            biasHidden1[j] -= learningRate * gradients[3][j] * scaleFactor;
        }
        for (int j = 0; j < HIDDEN2_SIZE; j++) {
            biasHidden2[j] -= learningRate * gradients[4][j] * scaleFactor;
        }
        biasOutput[0] -= learningRate * gradients[5][0] * scaleFactor;
    }

    /**
     * Calculate the L2 norm of all gradients.
     */
    private double calculateGradientNorm(double[][] gradients) {
        double norm = 0;
        for (double[] grad : gradients) {
            for (double g : grad) {
                norm += g * g;
            }
        }
        return Math.sqrt(norm);
    }

    /**
     * Get gradient norm for debugging/monitoring training stability.
     */
    public double getGradientNorm(double[][] gradients) {
        return calculateGradientNorm(gradients);
    }
}
