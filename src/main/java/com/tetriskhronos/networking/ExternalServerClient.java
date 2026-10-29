package com.tetriskhronos.networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetriskhronos.model.OpMove;  // ← ADD THIS
import com.tetriskhronos.model.PureGame;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ExternalServerClient {
    private static final String SERVER_URL = "http://localhost:3000";
    private static final String GET_MOVE_ENDPOINT = "/api/getMove";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ExternalServerClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Check if external server is running
     */
    public boolean isServerRunning() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/health"))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(2))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("[ExternalServerClient] Server health check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Request optimal move asynchronously (non-blocking)
     * Returns a CompletableFuture that completes when the server responds
     */
    public CompletableFuture<OpMove> getOptimalMoveAsync(PureGame pureGame) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getOptimalMove(pureGame);
            } catch (IOException e) {
                System.err.println("[ExternalServerClient] Error during async move request: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Request optimal move (synchronous - use getOptimalMoveAsync() to avoid blocking UI)
     */
    public OpMove getOptimalMove(PureGame pureGame) throws IOException {
        try {
            String jsonPayload = objectMapper.writeValueAsString(pureGame);
            System.out.println("[ExternalServerClient] Sending game state to server: " + jsonPayload.substring(0, Math.min(100, jsonPayload.length())) + "...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + GET_MOVE_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                OpMove move = objectMapper.readValue(response.body(), OpMove.class);
                System.out.println("[ExternalServerClient] Received move: " + move);
                return move;
            } else {
                System.err.println("[ExternalServerClient] Server returned status " + response.statusCode() +
                                 ": " + response.body());
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[ExternalServerClient] Request interrupted: " + e.getMessage());
            return null;
        }
    }
}
