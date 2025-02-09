package com.example.demo1;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.application.Platform;

public class Client {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private final String serverAddress;
    private final int serverPort;
    private static final List<BiConsumer<DataInputStream, Client>> packetHandlers = new ArrayList<>();
    public char playerSymbol; // 'X' or 'O'

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        registerPacketHandlers();
    }

    private static void registerPacketHandlers() {
        System.out.println("[DEBUG] Registering packet handlers...");

        packetHandlers.add((input, client) -> {
            System.out.println("[DEBUG] Handshake handler registered.");
            try { client.handleHandshake(input); }
            catch (IOException e) { e.printStackTrace(); }
        });

        packetHandlers.add((input, client) -> {
            System.out.println("[DEBUG] Join room handler registered.");
            try { client.handleJoinRoom(input); }
            catch (IOException e) { e.printStackTrace(); }
        });

        packetHandlers.add((input, client) -> {
            System.out.println("[DEBUG] Move handler registered.");
            try { client.handleMove(input); }
            catch (IOException e) { e.printStackTrace(); }
        });

        packetHandlers.add((input, client) -> {
            System.out.println("[DEBUG] Leave room handler registered.");
            try { client.handleGameEnd(input); }
            catch (IOException e) { e.printStackTrace(); }
        });

        System.out.println("[DEBUG] Packet handlers registered successfully.");
    }


    public void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            System.out.println("[INFO] Connected to server at " + serverAddress + ":" + serverPort);

            sendPacket(0, "Hello Server"); // Handshake packet
            listenForPackets();
        } catch (IOException e) {
            System.out.println("[ERROR] Connection failed: " + e.getMessage());
        }
    }

    private void listenForPackets() {
        new Thread(() -> {
            try {
                while (true) {
                    int packetID = input.read(); // Read packet ID
                    handlePacket(packetID, input);
                }
            } catch (IOException e) {
                System.out.println("[ERROR] Disconnected from server.");
            }
        }).start();
    }


    public void sendPacket(int packetID, String data) throws IOException {
        output.write(packetID);
        output.writeUTF(String.format("%04d", data.length()));
        output.writeUTF(data);
    }


    private void handlePacket(int packetID, DataInputStream input) {
        System.out.println("[DEBUG] Received Packet With ID: " + packetID);
        if (packetID >= 0 && packetID < packetHandlers.size()) {
            packetHandlers.get(packetID).accept(input, this);
        } else {
            System.out.println("[ERROR] Unknown packet ID: " + packetID);
        }
    }


    private void handleHandshake(DataInputStream input) throws IOException {
        String response = input.readUTF();
        System.out.println("[DEBUG] Handshake response: " + response);

        if (response.equals("ok")) {
            System.out.println("[DEBUG] Moving to next screen...");

            // Switch to the "ctg.fxml" screen
            Main.switchScene("ctg.fxml", "resultText");
        }
    }



    private void handleJoinRoom(DataInputStream input) throws IOException {

        // Read the full packet as a string
        String receivedData = input.readUTF();
        System.out.println("[SERVER] Room joined: " + receivedData);

        // Split packet by "," to extract values
        String[] dataParts = receivedData.split(",");
        int roomID = Integer.parseInt(dataParts[0]); // First value is the room ID
        if (roomID == -1) {
            Platform.runLater(() -> {
                ConnectController.getInstance().roomErrorLabel.setText("You are doing something wrong\nTry changing names");
                ConnectController.getInstance().roomErrorLabel.setVisible(true);
            });
            return;
        }
        String player1 = dataParts[1]; // Second value is always player 1
        String player2 = (dataParts.length > 2) ? dataParts[2] : ""; // Third value (if exists) is player 2

        if (player1.contains(Main.playerOne)) {
            playerSymbol = 'X';
        } else {
            playerSymbol = 'O';
        }
        System.out.println("[DEBUG] Assigned Player Symbol: " + playerSymbol);

        System.out.println("[DEBUG] Parsed Room ID: " + roomID);
        System.out.println("[DEBUG] Player 1: " + player1);
        System.out.println("[DEBUG] Player 2: " + (player2.isEmpty() ? "Waiting for player..." : player2));


        // Store the room details in HelloApplication for access in RoomController
        Main.roomID = roomID;
        Main.playerOne = player1;
        Main.playerTwo = player2;

        Main.switchScene("room.fxml", "resultText");

        if (!player2.isEmpty()){
            Platform.runLater(() -> {
                RoomController.getInstance().currentTurnLabel.setText("Current Turn: X");
            });
        }

    }

    private void handleMove(DataInputStream input) throws IOException {
        String data = input.readUTF();
        System.out.println("[SERVER] Move received: " + data);

        String[] moveParts = data.split(",");
        if (moveParts.length != 3) {
            System.out.println("[ERROR] Invalid move format.");
            return;
        }

        int row, col;
        char symbol;

        try {
            row = Integer.parseInt(moveParts[0]);
            col = Integer.parseInt(moveParts[1]);
            symbol = moveParts[2].charAt(0);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to parse move data.");
            return;
        }

        if (row == -1 || col == -1) {
            System.out.println("[ERROR] Server rejected the move. Illegal move attempted.");
            return;
        }

        System.out.println("[DEBUG] Move -> Row: " + row + ", Col: " + col + ", Symbol: " + symbol);
        RoomController roomController = RoomController.getInstance();

        Platform.runLater(() -> {
            roomController.updateBoard(row, col, symbol);
            roomController.updateTurnLabel((symbol == 'X') ? 'O' : 'X'); // Update turn

        });
    }


    private void handleGameEnd(DataInputStream input) throws IOException {
        String data = input.readUTF();
        System.out.println("[SERVER] GameEnd: " + data);

        // Read the data
        String[] dataParts = data.split(",");
        if (dataParts.length == 0) {
            System.out.println("[ERROR] Invalid game end packet.");
            return;
        }

        String resultText;

        // Determine the game result
        if (dataParts[0].equals("DRAW")) {
            resultText = "It's a Draw!";
        } else if (dataParts[0].equals("WIN")) {
            char winnerSymbol = dataParts[1].charAt(0);
            resultText = winnerSymbol + " Wins!";
        } else {
            System.out.println("[ERROR] Unknown game result.");
            return;
        }

        Main.switchScene("game-result.fxml", resultText);
        Main.client.sendPacket(4, "");
    }


    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 2546);
        client.connectToServer();
    }
}
