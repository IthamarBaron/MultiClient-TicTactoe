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
    private Label roomErrorLabel;

    @FXML
    private Button joinRoomButton;

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
            System.out.println("[INFO] Creating Room for: " + playerName);

            try {
                HelloApplication.client.sendPacket(2,playerName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void onJoinRoomClick() {
        String roomCode = roomCodeField.getText().trim();
        if (roomCode.isEmpty()) {
            roomErrorLabel.setVisible(true);
        } else {
            roomErrorLabel.setVisible(false);
            System.out.println("[INFO] Attempting to join Room with Code: " + roomCode);
            // TODO: Implement join room logic
        }
    }
}

