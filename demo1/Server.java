package com.example.demo1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class Server {
    private static final int PORT = 2546;
    private static ServerSocket serverSocket;
    private static final Map<Integer, List<String>> rooms = new ConcurrentHashMap<>();
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
            try { handleLeaveRoom(input, client); }
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
    }
    private static void handleCreateRoom(DataInputStream input, ClientThread client) throws IOException {
        System.out.println("Handling Create room...");
        String receivedData = input.readUTF();
        System.out.println("[Create ROOM] Received data: " + receivedData);
        //TODO: room creation logic.
        //todo: join him to the room
        //TODO: inform client of room creation and move him to the next window
        client.sendPacket(1, "ok");
    }

    private static void handleMove(DataInputStream input, ClientThread client) throws IOException {
        System.out.println("Handling move...");
        String receivedData = input.readUTF();
        System.out.println("[MOVE] Received data: " + receivedData);
    }

    private static void handleLeaveRoom(DataInputStream input, ClientThread client) throws IOException {
        System.out.println("Handling leave room...");
        String receivedData = input.readUTF();
        System.out.println("[LEAVE ROOM] Received data: " + receivedData);
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
            System.out.println("[ERROR] com.example.demo1.Client disconnected: " + clientID);
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
