package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.application.Platform;

import java.io.IOException;

public class RoomController {
    @FXML private Label roomIdLabel;
    @FXML private Label playerOneLabel;
    @FXML private Label playerTwoLabel;
    @FXML protected Label currentTurnLabel;

    @FXML private Button cell00, cell01, cell02, cell10, cell11, cell12, cell20, cell21, cell22;
    private Button[][] boardButtons;
    private static RoomController instance;

    public RoomController() {
        instance = this;
    }

    public static RoomController getInstance() {
        return instance;
    }


    @FXML
    private void handleCellClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String cellId = clickedButton.getId();

        // Convert cellId to row and column
        int row = Character.getNumericValue(cellId.charAt(4));
        int col = Character.getNumericValue(cellId.charAt(5));

        char symbol = Main.client.playerSymbol;

        System.out.println("[DEBUG] Clicked Cell: " + cellId + " (Row: " + row + ", Col: " + col + ")");

        // Send packet to server
        try {
            Main.client.sendPacket(3, row + "," + col + "," + symbol);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to send move packet: " + e.getMessage());
        }

    }

    public void initialize() {
        setupRoom(Main.roomID, Main.playerOne, Main.playerTwo);
        // Initialize board button matrix
        boardButtons = new Button[][] {
                {cell00, cell01, cell02},
                {cell10, cell11, cell12},
                {cell20, cell21, cell22}
        };
    }

    public void setupRoom(int roomID, String player1, String player2) {
        roomIdLabel.setText("Room ID: " + roomID);
        currentTurnLabel.setText("Waiting for other player to join...");
        playerOneLabel.setText(player1);
        playerTwoLabel.setText(player2);
    }

    public void updateBoard(int row, int col, char symbol) {
        Platform.runLater(() -> {
            boardButtons[row][col].setText(String.valueOf(symbol));
            boardButtons[row][col].setDisable(true); // Prevent multiple clicks
        });

        // Preserve existing styles and only update text color
        String currentStyle = boardButtons[row][col].getStyle();
        String colorStyle = (symbol == 'X') ? "-fx-text-fill: red;" : "-fx-text-fill: green;";
        boardButtons[row][col].setStyle(currentStyle + colorStyle);
    }

    public void updateTurnLabel(char nextTurn) {
        Platform.runLater(() -> currentTurnLabel.setText("Current Turn: " + nextTurn));
    }


}
