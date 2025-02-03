
package com.example.demo1;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;


public class HelloController {
    @FXML
    private void initialize() {
        ipAddressField.setStyle("-fx-prompt-text-fill: black;");
    }
    @FXML
    private TextField ipAddressField;

    @FXML
    private Button connectButton;

    @FXML
    private Label statusLabel;

    @FXML
    protected void onConnectButtonClick() {
        String ipAddress = ipAddressField.getText().trim();
        if (ipAddress.isEmpty()) {
            statusLabel.setText("Please enter a valid IP address.");
        } else {
            statusLabel.setText("Connecting to " + ipAddress + "...");
            HelloApplication.client = new Client(ipAddress,2546);
            HelloApplication.client.connectToServer();
            System.out.println("Attempting to connect to: " + ipAddress);
        }
    }
}
