package com.tetriskhronos.networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetriskhronos.model.OpMove;
import com.tetriskhronos.model.PureGame;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ExternalServerClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;
    private static final int TIMEOUT = 5000; // 5 seconds timeout
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Sends the current game state to TetrisServer and receives the optimal move.
     * @param game The current game state to send to the server
     * @return The optimal move (OpMove) if successful, null if server is unreachable
     * @throws IOException if there's an error during communication
     */
    public OpMove getOptimalMove(PureGame game) throws IOException {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT);

            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // Step 1: Convert PureGame to JSON
            String jsonGameState = mapper.writeValueAsString(game);
            System.out.println("[ExternalServer] Sending game state: " + jsonGameState);

            // Step 2: Send the game state to the server (raw bytes, no PrintWriter)
            out.write(jsonGameState.getBytes());
            out.write('\n'); // Send newline as delimiter if server expects it
            out.flush();

            // Step 3: Wait for the server's response
            String response = in.readLine();
            if (response == null) {
                System.err.println("[ExternalServer] Server closed connection without response.");
                return null;
            }

            System.out.println("[ExternalServer] Received response: " + response);

            // Step 4: Convert the JSON response to an OpMove object
            OpMove move = mapper.readValue(response, OpMove.class);
            System.out.println("[ExternalServer] Optimal Move: X=" + move.opX() +
                             ", Rotations=" + move.opRotate());

            return move;

        } catch (SocketException e) {
            System.err.println("[ExternalServer] Server is unreachable at " +
                             SERVER_HOST + ":" + SERVER_PORT);
            return null;
        } catch (IOException e) {
            System.err.println("[ExternalServer] Error communicating with server: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if the server is running
     * @return true if server is reachable, false otherwise
     */
    public boolean isServerRunning() {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
