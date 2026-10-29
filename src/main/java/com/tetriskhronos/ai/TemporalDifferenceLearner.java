package com.tetriskhronos.ai;

public class TemporalDifferenceLearner {
    private final NeuralNetworkEvaluator evaluator;
    private final double alphaInitial;
    private final double gamma;
    private final double lambda;
    private final double maxGradientNorm;

    private double[] eligibilityTraces;
    private double currentAlpha;
    private int updateCount;

    private static final double OUTPUT_SCALE = 10.0; // Must match NeuralNetworkEvaluator

    public TemporalDifferenceLearner(NeuralNetworkEvaluator evaluator,
                                      double alphaInitial,
                                      double gamma,
                                      double lambda,
                                      double maxGradientNorm) {
        this.evaluator = evaluator;
        this.alphaInitial = alphaInitial;
        this.gamma = gamma;
        this.lambda = lambda;
        this.maxGradientNorm = maxGradientNorm;
        this.eligibilityTraces = new double[28];
        this.updateCount = 0;
        this.currentAlpha = alphaInitial;
    }

    public void initializeEpisode(double[] initialFeatures) {
        eligibilityTraces = new double[28];
        updateCount = 0;
    }

    public void processTransition(double[] currentFeatures, double reward, double[] nextFeatures, boolean isTerminal) {
        double valueNext = isTerminal ? 0.0 : evaluator.evaluate(nextFeatures);
        double valueCurrent = evaluator.evaluate(currentFeatures);
        double tdError = reward + gamma * valueNext - valueCurrent;

        updateAlpha();

        // Forward pass to capture activations
        ForwardPassResult forward = forwardPass(currentFeatures);

        // Backpropagation with tanh scaling
        BackpropResult backprop = backpropagation(forward, tdError);

        // Update eligibility traces
        updateEligibilityTraces(backprop.inputGradient);

        // Apply weight updates with eligibility traces
        applyWeightUpdates(backprop, currentAlpha);
    }

    public void finalizeEpisode(double finalReward) {
        // Use zero-vector for terminal state (represents absorbing state with zero value)
        double[] terminalFeatures = new double[28];
        double tdError = finalReward; // Terminal value is 0, so TD error = finalReward - 0

        updateAlpha();

        ForwardPassResult forward = forwardPass(terminalFeatures);
        BackpropResult backprop = backpropagation(forward, tdError);

        applyWeightUpdates(backprop, currentAlpha);
    }

    private void updateEligibilityTraces(double[] inputGradient) {
        for (int i = 0; i < eligibilityTraces.length; i++) {
            eligibilityTraces[i] = lambda * gamma * eligibilityTraces[i] + inputGradient[i];
        }
    }

    private void updateAlpha() {
        currentAlpha = Math.max(0.00001, alphaInitial / (1.0 + updateCount / 1000.0));
        updateCount++;
    }

    private ForwardPassResult forwardPass(double[] features) {
        ForwardPassResult result = new ForwardPassResult();
        result.input = features;
        result.hidden1 = new double[64];
        result.hidden2 = new double[32];
        result.z1 = new double[64];
        result.z2 = new double[32];

        double[][] w1 = evaluator.getWeightsInputHidden1();
        double[] b1 = evaluator.getBiasHidden1();
        double[][] w2 = evaluator.getWeightsHidden1Hidden2();
        double[] b2 = evaluator.getBiasHidden2();
        double[][] w3 = evaluator.getWeightsHidden2Output();
        double[] b3 = evaluator.getBiasOutput();

        // Layer 1: Input -> Hidden1 (ReLU)
        for (int j = 0; j < 64; j++) {
            double sum = b1[j];
            for (int i = 0; i < 28; i++) {
                sum += features[i] * w1[i][j];
            }
            result.z1[j] = sum;
            result.hidden1[j] = Math.max(0, sum); // ReLU
        }

        // Layer 2: Hidden1 -> Hidden2 (ReLU)
        for (int j = 0; j < 32; j++) {
            double sum = b2[j];
            for (int i = 0; i < 64; i++) {
                sum += result.hidden1[i] * w2[i][j];
            }
            result.z2[j] = sum;
            result.hidden2[j] = Math.max(0, sum); // ReLU
        }

        // Layer 3: Hidden2 -> Output (linear, then tanh scaling applied in evaluate())
        double sum = b3[0];
        for (int i = 0; i < 32; i++) {
            sum += result.hidden2[i] * w3[i][0];
        }
        result.output = sum;

        return result;
    }

    private BackpropResult backpropagation(ForwardPassResult forward, double tdError) {
        BackpropResult result = new BackpropResult();

        double[][] w2 = evaluator.getWeightsHidden1Hidden2();
        double[][] w3 = evaluator.getWeightsHidden2Output();

        // **Account for tanh scaling in output**
        // The output is scaled: Math.tanh(output / 10.0) * 100.0
        // So gradient w.r.t. raw output = tdError * d/doutput[tanh(output/10.0)*100.0]
        // d/doutput[tanh(x/10.0)*100.0] = 100.0 * (1/10.0) * (1 - tanh²(output/10.0))
        double tanhValue = Math.tanh(forward.output / OUTPUT_SCALE);
        double tanhDerivative = 1.0 - (tanhValue * tanhValue);
        double scalingDerivative = 100.0 * (1.0 / OUTPUT_SCALE) * tanhDerivative;
        double outputDelta = tdError * scalingDerivative;

        // Backprop to Hidden2
        double[] hidden2Delta = new double[32];
        for (int i = 0; i < 32; i++) {
            hidden2Delta[i] = outputDelta * w3[i][0];
            // ReLU derivative: 0 if z <= 0, else 1
            if (forward.z2[i] <= 0) {
                hidden2Delta[i] = 0;
            }
        }

        // Backprop to Hidden1
        double[] hidden1Delta = new double[64];
        for (int i = 0; i < 64; i++) {
            hidden1Delta[i] = 0;
            for (int j = 0; j < 32; j++) {
                hidden1Delta[i] += hidden2Delta[j] * w2[i][j];
            }
            // ReLU derivative
            if (forward.z1[i] <= 0) {
                hidden1Delta[i] = 0;
            }
        }

        // Backprop to Input
        result.inputGradient = new double[28];
        double[][] w1 = evaluator.getWeightsInputHidden1();
        for (int i = 0; i < 28; i++) {
            result.inputGradient[i] = 0;
            for (int j = 0; j < 64; j++) {
                result.inputGradient[i] += hidden1Delta[j] * w1[i][j];
            }
        }

        // Compute weight and bias gradients
        result.weightInputHidden1Grad = new double[28 * 64];
        int idx = 0;
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 64; j++) {
                result.weightInputHidden1Grad[idx++] = forward.input[i] * hidden1Delta[j];
            }
        }

        result.biasHidden1Grad = hidden1Delta.clone();

        result.weightHidden1Hidden2Grad = new double[64 * 32];
        idx = 0;
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 32; j++) {
                result.weightHidden1Hidden2Grad[idx++] = forward.hidden1[i] * hidden2Delta[j];
            }
        }

        result.biasHidden2Grad = hidden2Delta.clone();

        result.weightHidden2OutputGrad = new double[32];
        for (int i = 0; i < 32; i++) {
            result.weightHidden2OutputGrad[i] = forward.hidden2[i] * outputDelta;
        }

        result.biasOutputGrad = new double[1];
        result.biasOutputGrad[0] = outputDelta;

        return result;
    }

    private void applyWeightUpdates(BackpropResult backprop, double alpha) {
        // Apply eligibility traces to all gradients
        applyEligibilityToGradients(backprop);

        // Clip gradients by norm
        clipGradientsByNorm(backprop);

        // Package gradients for applyGradients()
        double[][] gradients = new double[6][];
        gradients[0] = backprop.weightInputHidden1Grad;
        gradients[1] = backprop.weightHidden1Hidden2Grad;
        gradients[2] = backprop.weightHidden2OutputGrad;
        gradients[3] = backprop.biasHidden1Grad;
        gradients[4] = backprop.biasHidden2Grad;
        gradients[5] = backprop.biasOutputGrad;

        evaluator.applyGradients(gradients, alpha, maxGradientNorm);
    }

    private void applyEligibilityToGradients(BackpropResult backprop) {
        // Apply eligibility traces to input-hidden1 weights
        int idx = 0;
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 64; j++) {
                backprop.weightInputHidden1Grad[idx] *= eligibilityTraces[i];
                idx++;
            }
        }

        // Apply eligibility traces to hidden1 bias
        for (int j = 0; j < 64; j++) {
            backprop.biasHidden1Grad[j] *= eligibilityTraces[j % 28]; // Heuristic: cycle through input traces
        }

        // Other weight/bias gradients are not directly tied to input eligibility,
        // but they participate in the learning signal through backpropagated deltas.
        // For a more sophisticated approach, track eligibility traces through all layers.
    }

    /**
     * Gradient clipping by global norm to prevent exploding gradients.
     */
    private void clipGradientsByNorm(BackpropResult backprop) {
        double globalNormSq = 0.0;

        // Compute global norm
        for (double g : backprop.weightInputHidden1Grad) {
            globalNormSq += g * g;
        }
        for (double g : backprop.weightHidden1Hidden2Grad) {
            globalNormSq += g * g;
        }
        for (double g : backprop.weightHidden2OutputGrad) {
            globalNormSq += g * g;
        }
        for (double g : backprop.biasHidden1Grad) {
            globalNormSq += g * g;
        }
        for (double g : backprop.biasHidden2Grad) {
            globalNormSq += g * g;
        }
        for (double g : backprop.biasOutputGrad) {
            globalNormSq += g * g;
        }

        double globalNorm = Math.sqrt(globalNormSq);

        if (globalNorm > maxGradientNorm && globalNorm > 0) {
            double clipFactor = maxGradientNorm / globalNorm;

            for (int i = 0; i < backprop.weightInputHidden1Grad.length; i++) {
                backprop.weightInputHidden1Grad[i] *= clipFactor;
            }
            for (int i = 0; i < backprop.weightHidden1Hidden2Grad.length; i++) {
                backprop.weightHidden1Hidden2Grad[i] *= clipFactor;
            }
            for (int i = 0; i < backprop.weightHidden2OutputGrad.length; i++) {
                backprop.weightHidden2OutputGrad[i] *= clipFactor;
            }
            for (int i = 0; i < backprop.biasHidden1Grad.length; i++) {
                backprop.biasHidden1Grad[i] *= clipFactor;
            }
            for (int i = 0; i < backprop.biasHidden2Grad.length; i++) {
                backprop.biasHidden2Grad[i] *= clipFactor;
            }
            for (int i = 0; i < backprop.biasOutputGrad.length; i++) {
                backprop.biasOutputGrad[i] *= clipFactor;
            }
        }
    }

    private static class ForwardPassResult {
        double[] input;
        double[] hidden1;
        double[] hidden2;
        double output;
        double[] z1;
        double[] z2;
    }

    private static class BackpropResult {
        double[] inputGradient;
        double[] weightInputHidden1Grad;
        double[] biasHidden1Grad;
        double[] weightHidden1Hidden2Grad;
        double[] biasHidden2Grad;
        double[] weightHidden2OutputGrad;
        double[] biasOutputGrad;
    }
}
