package com.example.demo1;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.function.BiConsumer;

public class Client {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private final String serverAddress;
    private final int serverPort;
    private static final List<BiConsumer<DataInputStream, Client>> packetHandlers = new ArrayList<>();

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
            try { client.handleLeaveRoom(input); }
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
            HelloApplication.switchScene("ctg.fxml");
        }
    }



    private void handleJoinRoom(DataInputStream input) throws IOException {
        System.out.println("[SERVER] Room joined: " + input.readUTF());
    }

    private void handleMove(DataInputStream input) throws IOException {
        System.out.println("[SERVER] Move received: " + input.readUTF());
    }

    private void handleLeaveRoom(DataInputStream input) throws IOException {
        System.out.println("[SERVER] Left room: " + input.readUTF());
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 2546);
        client.connectToServer();
    }
}
