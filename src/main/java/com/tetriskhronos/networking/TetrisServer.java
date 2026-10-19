package com.tetriskhronos.networking;

import com.tetriskhronos.model.Board;
import com.tetriskhronos.model.Game;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.nio.charset.StandardCharsets;

/**
 * TetrisServer: Lightweight HTTP server for networked Tetris
 * - Runs on localhost:3000
 * - Handles player moves and game state synchronization
 * - Supports multiple concurrent games
 */
public class TetrisServer {
    private static final int PORT = 3000;
    private static final String HOST = "localhost";
    private HttpServer server;
    private Map<String, GameSession> activeSessions = new HashMap<>();

    public TetrisServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        setupEndpoints();
    }

    private void setupEndpoints() {
        // Create new game session
        server.createContext("/api/game/new", exchange -> handleNewGame(exchange));
        server.createContext("/api/game/move", exchange -> handleMove(exchange));
        server.createContext("/api/game/state", exchange -> handleGetState(exchange));
        server.createContext("/api/game/opponent", exchange -> handleGetOpponentState(exchange));
        server.createContext("/health", exchange -> handleHealth(exchange));
    }

    private void handleNewGame(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            String gameId = UUID.randomUUID().toString();

            GameSession session = new GameSession(gameId);
            activeSessions.put(gameId, session);

            String response = "{\"gameId\":\"" + gameId + "\",\"status\":\"created\"}";
            sendResponse(exchange, 200, response);
            System.out.println("✅ New game created: " + gameId);
        } else {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private void handleMove(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);

            // Parse JSON: {"gameId": "xxx", "move": "left|right|rotate|drop"}
            String gameId = extractJsonField(body, "gameId");
            String move = extractJsonField(body, "move");

            GameSession session = activeSessions.get(gameId);
            if (session == null) {
                sendResponse(exchange, 404, "{\"error\":\"Game not found\"}");
                return;
            }

            applyMove(session.getGame(), move);
            session.getGame().update();

            String response = "{\"status\":\"success\",\"points\":" + session.getGame().getPoints() + "}";
            sendResponse(exchange, 200, response);
        } else {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private void handleGetState(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String gameId = extractQueryParam(exchange.getRequestURI().getQuery(), "gameId");
            GameSession session = activeSessions.get(gameId);

            if (session == null) {
                sendResponse(exchange, 404, "{\"error\":\"Game not found\"}");
                return;
            }

            Game game = session.getGame();
            String stateJson = buildGameStateJson(game);
            sendResponse(exchange, 200, stateJson);
        } else {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private void handleGetOpponentState(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String gameId = extractQueryParam(exchange.getRequestURI().getQuery(), "gameId");
            GameSession session = activeSessions.get(gameId);

            if (session == null) {
                sendResponse(exchange, 404, "{\"error\":\"Game not found\"}");
                return;
            }

            String boardJson = buildBoardJson(session.getGame().getBoard());
            sendResponse(exchange, 200, boardJson);
        } else {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, "{\"status\":\"ok\",\"activeSessions\":" + activeSessions.size() + "}");
    }

    private void applyMove(Game game, String move) {
        switch (move.toLowerCase()) {
            case "left" -> game.moveLeft();
            case "right" -> game.moveRight();
            case "rotate" -> game.rotate();
            case "drop" -> game.hardDrop();
            default -> System.out.println("⚠️ Unknown move: " + move);
        }
    }

    private String buildGameStateJson(Game game) {
        return "{"
            + "\"points\":" + game.getPoints() + ","
            + "\"lines\":" + game.getLines() + ","
            + "\"level\":" + game.getLevel() + ","
            + "\"state\":\"" + game.getGameState() + "\","
            + "\"board\":" + buildBoardJson(game.getBoard())
            + "}";
    }

    private String buildBoardJson(Board board) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"width\":").append(board.getWidth())
          .append(",\"height\":").append(board.getHeight())
          .append(",\"grid\":[");

        int[][] grid = board.getGrid();
        for (int row = 0; row < grid.length; row++) {
            sb.append("[");
            for (int col = 0; col < grid[row].length; col++) {
                sb.append(grid[row][col]);
                if (col < grid[row].length - 1) sb.append(",");
            }
            sb.append("]");
            if (row < grid.length - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }

    private String extractJsonField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private String extractQueryParam(String query, String param) {
        if (query == null) return "";
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv[0].equals(param) && kv.length > 1) {
                return kv[1];
            }
        }
        return "";
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    public void start() {
        server.setExecutor(null);
        server.start();
        System.out.println("🚀 TetrisServer running on http://" + HOST + ":" + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("🛑 TetrisServer stopped");
    }


    public static class GameSession {
        private final String gameId;
        private final Game game;
        private long createdAt;

        public GameSession(String gameId) {
            this.gameId = gameId;
            com.tetriskhronos.model.Configuration config =
                com.tetriskhronos.model.Configuration.getInstance();
            config.setFieldWidth(10);
            config.setFieldHeight(20);
            config.setGameLevel(1);
            this.game = new Game(config);
            this.game.start();
            this.createdAt = System.currentTimeMillis();
        }

        public String getGameId() { return gameId; }
        public Game getGame() { return game; }
        public long getCreatedAt() { return createdAt; }
    }

    public static void main(String[] args) throws IOException {
        TetrisServer server = new TetrisServer();
        server.start();

        System.in.read();
        server.stop();
    }
}
