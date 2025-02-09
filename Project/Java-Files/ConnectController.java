package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ConnectController {

    @FXML
    private TextField nameField;

    @FXML
    private Label nameErrorLabel;

    @FXML
    private Button createRoomButton;

    @FXML
    private TextField roomCodeField;

    @FXML
    protected Label roomErrorLabel;

    @FXML
    private Button joinRoomButton;


    private static ConnectController instance;

    public ConnectController() {
        instance = this;
    }

    public static ConnectController getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        nameErrorLabel.setVisible(false);
        roomErrorLabel.setVisible(false);
    }

    @FXML
    private void onCreateRoomClick() {
        String playerName = nameField.getText().trim();
        if (playerName.length() > 15 || playerName.length() == 0) {
            nameErrorLabel.setVisible(true);
        } else {
            nameErrorLabel.setVisible(false);
            Main.playerOne = playerName;
            System.out.println("[INFO] Creating Room for: " + playerName);

            try {
                Main.client.sendPacket(2,playerName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void onJoinRoomClick() {
        String roomCode = roomCodeField.getText().trim();
        String playerName = nameField.getText().trim();

        if (!roomCode.matches("\\d+")) { // Ensure roomCode contains only digits
            roomErrorLabel.setText("Invalid room code!");
            roomErrorLabel.setVisible(true);
            return;
        }

        // Validate player name
        if (playerName.length() > 15 || playerName.isEmpty() || playerName.contains(",")) {
            nameErrorLabel.setText("Invalid name!");
            nameErrorLabel.setVisible(true);
            return;
        }
        Main.playerOne = playerName;
        nameErrorLabel.setVisible(false);
        roomErrorLabel.setVisible(false);

        System.out.println("[INFO] Attempting to join Room with Code: " + roomCode + " as " + playerName);

        // Send packet with ID 1: "roomCode,playerName"
        try {
            Main.client.sendPacket(1, roomCode + "," + playerName);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to send join room request.");
            e.printStackTrace();
        }
    }

}

