package com.example.demo1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class Server {
    private  static int numberName = 1;
    private static int nextRoomID = 1;
    private static final int PORT = 2546;
    private static ServerSocket serverSocket;
    private static final Map<Integer, List<String>> rooms = new ConcurrentHashMap<>();
    private static final Map<Integer, List<ClientThread>> roomClients = new ConcurrentHashMap<>();
    private static final Map<Integer, GameState> roomGameStates = new ConcurrentHashMap<>();

    private static final List<BiConsumer<DataInputStream, ClientThread>> packetHandlers = new ArrayList<>();

    public static void main(String[] args) {
        registerPacketHandlers();
        startServer();
    }

    private static void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                new ClientThread(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Server error: " + e.getMessage());
        }
    }

    private static void registerPacketHandlers() {
        packetHandlers.add((input, client) -> {
            try { handleHandshake(input, client); }
            catch (IOException e) { e.printStackTrace(); }
        });

        packetHandlers.add((input, client) -> {
            try { handleJoinRoom(input, client); }
            catch (IOException e) { e.printStackTrace(); }
        });
        packetHandlers.add((input, client) -> {
            try { handleCreateRoom(input, client); }
            catch (IOException e) { e.printStackTrace(); }
        });

        packetHandlers.add((input, client) -> {
            try { handleMove(input, client); }
            catch (IOException e) { e.printStackTrace(); }
        });

        packetHandlers.add((input, client) -> {
            try { handleGameEnd(input, client); }
            catch (IOException e) { e.printStackTrace(); }
        });
    }


    public static void handlePacket(int packetID, int dataLength, DataInputStream input, ClientThread client) throws IOException {
        if (packetID >= 0 && packetID < packetHandlers.size()) {
            packetHandlers.get(packetID).accept(input, client);
        } else {
            System.out.println("[ERROR] Unknown packet ID: " + packetID);
        }
    }

    private static void handleHandshake(DataInputStream input, ClientThread client) throws IOException {
        System.out.println("[HANDSHAKE] Handling handshake...");
        String receivedData = input.readUTF();
        System.out.println("[HANDSHAKE] Received data: " + receivedData);
        // Send handshake response
        client.sendPacket(0, "ok");
    }

    private static void handleJoinRoom(DataInputStream input, ClientThread client) throws IOException {
        System.out.println("Handling join room...");
        String receivedData = input.readUTF();
        System.out.println("[JOIN ROOM] Received data: " + receivedData);

        // Read room ID and player name
        String[] dataParts = receivedData.split(",");
        if (dataParts.length != 2) {
            System.out.println("[ERROR] Invalid join room packet format.");
            client.sendPacket(1, "-1");
            return;
        }

        int roomID;
        try {
            roomID = Integer.parseInt(dataParts[0]);
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid room ID format.");
            client.sendPacket(1, "-1");
            return;
        }

        String playerName = dataParts[1];

        // Check if the room exists
        if (!rooms.containsKey(roomID) || !roomClients.containsKey(roomID)) {
            System.out.println("[ERROR] Room does not exist");
            client.sendPacket(1, "-1");
            return;
        }


        List<String> playerNames = rooms.get(roomID);
        List<ClientThread> clients = roomClients.get(roomID);

        // Check if the room is full
        if (!playerNames.get(1).isEmpty()) {
            System.out.println("[ERROR] Room is full!");
            client.sendPacket(1, "-1");
            return;
        }

        // Check if the player name fucks up my crippled X-O assignment lol
        if (playerNames.get(0).contains(playerName)) {
            System.out.println("[ERROR] Invalid name!");
            client.sendPacket(1, "-1");
            return;
        }


        // Assign X to first player, O to second player
        if (playerNames.get(0).startsWith("X")) {
            playerName = "O \t " + playerName;  // Second player is 'O'
        } else {
            playerNames.set(0, "X \t " + playerNames.get(0));  // Ensure first player has 'X'
            playerName = "O \t " + playerName;  // Assign 'O' to second player
        }

        playerNames.set(1, playerName);
        clients.add(client);

        System.out.println("[JOIN ROOM] " + playerName + " joined Room " + roomID);

        // Switch game state from 'W' (waiting) to 'X' (first player starts)
        GameState gameState = roomGameStates.get(roomID);
        gameState.switchTurn();


        // Notify both clients about the room update
        for (ClientThread c : clients) {
            c.sendPacket(1, roomID + "," + playerNames.get(0) + "," + playerNames.get(1));
        }
    }

    private static void handleCreateRoom(DataInputStream input, ClientThread client) throws IOException {
        System.out.println("Handling Create room...");
        String playerName = input.readUTF();
        System.out.println("[CREATE ROOM] Received player name: " + playerName);

        // Validate player name
        if (playerName == null || playerName.trim().isEmpty() || playerName.contains(",")) {
            System.out.println("[ERROR] Invalid player name. Setting name to Player." + numberName);
            playerName = "Player" + numberName++;
        }

        int roomID = nextRoomID++;
        roomGameStates.put(roomID, new GameState());
        roomClients.put(roomID, new ArrayList<>(List.of(client)));
        rooms.put(roomID, new ArrayList<>(Arrays.asList(playerName, ""))); // Second player slot empty
        System.out.println("[CREATE ROOM] Room " + roomID + " created with player: " + playerName);

        client.sendPacket(1, roomID + "," + playerName);
    }

    private static void handleMove(DataInputStream input, ClientThread client) throws IOException {
        System.out.println("Handling move...");
        String receivedData = input.readUTF();
        System.out.println("[MOVE] Received data: " + receivedData);

        String[] dataParts = receivedData.split(",");
        if (dataParts.length != 3) {
            System.out.println("[ERROR] Invalid move format.");
            client.sendPacket(2, "-1,-1,*"); // Invalid move response
            return;
        }

        int row, col;
        char symbol;
        try {
            row = Integer.parseInt(dataParts[0]);
            col = Integer.parseInt(dataParts[1]);
            symbol = dataParts[2].charAt(0);
        } catch (Exception e) {
            System.out.println("[ERROR] Invalid move data.");
            client.sendPacket(2, "-1,-1,*"); // Invalid move response
            return;
        }

        int roomID = -1;
        for (Map.Entry<Integer, List<ClientThread>> entry : roomClients.entrySet()) {
            if (entry.getValue().contains(client)) {
                roomID = entry.getKey();
                break;
            }
        }

        if (roomID == -1) {
            System.out.println("[ERROR] Client not in any room.");
            client.sendPacket(2, "-1,-1,*");
            return;
        }
        GameState gameState = roomGameStates.get(roomID);
        List<ClientThread> clients = roomClients.get(roomID);

        if (gameState.getCurrentTurn() != symbol) {
            System.out.println("[ERROR] Not " + symbol + "'s turn!");
            client.sendPacket(2, "-1,-1,*");
            return;
        }

        if (row < 0 || row > 2 || col < 0 || col > 2 || !gameState.isCellEmpty(row, col)) {
            System.out.println("[ERROR] Invalid move.");
            client.sendPacket(2, "-1,-1,*");
            return;
        }


        // Update board and switch turn
        gameState.makeMove(row, col, symbol);
        gameState.switchTurn();

        // Notify both players
        for (ClientThread c : clients) {
            c.sendPacket(2, row + "," + col + "," + symbol);
        }

        char winner = gameState.checkWinner();
        if (winner != '\0') {
            for (ClientThread c : clients) {
                c.sendPacket(3, "WIN," + winner);
            }
        } else if (gameState.isBoardFull()) {
            for (ClientThread c : clients) {
                c.sendPacket(3, "DRAW");
            }
        }

    }

    private static void handleGameEnd(DataInputStream input, ClientThread client) throws IOException {
        System.out.println("Game Ended..");
        String receivedData = input.readUTF();
        System.out.println("[LEAVE ROOM] Received data: " + receivedData);
        Integer roomIDToRemove = null;

        for (Map.Entry<Integer, List<ClientThread>> entry : roomClients.entrySet()) {
            if (entry.getValue().contains(client)) {
                roomIDToRemove = entry.getKey();
                break;
            }
        }

        if (roomIDToRemove != null) {
            System.out.println("[INFO] Client was in room " + roomIDToRemove + ". Removing from room.");

            // Remove the client from the room
            List<ClientThread> clientsInRoom = roomClients.get(roomIDToRemove);
            clientsInRoom.remove(client);

            // If the room is now empty, remove it completely
            if (clientsInRoom.isEmpty()) {
                System.out.println("[INFO] Room " + roomIDToRemove + " is now empty. Removing from server.");
                rooms.remove(roomIDToRemove);
                roomClients.remove(roomIDToRemove);
                roomGameStates.remove(roomIDToRemove);
            }
        } else {
            System.out.println("[WARNING] Client was not in any room.");
        }

        // Signal client to disconnect naturally
        client.interrupt();

    }
    public static Map<Integer, List<String>> getRooms() {
        return rooms;
    }
}



class ClientThread extends Thread {
    private final Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private String clientID;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            clientID = socket.getRemoteSocketAddress().toString();

            System.out.println("[INFO] com.example.demo1.Client connected: " + clientID);

            while (true) {
                int packetID = input.read(); // Read packet ID
                int dataLength = Integer.parseInt(input.readUTF()); // Read length

                Server.handlePacket(packetID, dataLength, input, this);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Client disconnected: " + clientID);
        } finally {
            closeConnection();
        }
    }

    public void sendPacket(int packetID, String data) {
        try {
            output.write(packetID);
            output.writeUTF(data);
            output.flush(); // Ensure data is actually sent
            System.out.println("[DEBUG] Sent packet ID " + packetID + " with data: " + data);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to send packet to client: " + clientID);
        }
    }
    private void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Closing connection: " + e.getMessage());
        }
    }



    public String getClientID() {
        return clientID;
    }
}
