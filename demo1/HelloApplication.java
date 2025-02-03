package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.application.Platform;

public class HelloApplication extends Application {

    public static Client client = null;
    public static Stage primaryStage; // Store the main stage

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage; // Store stage reference
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Tic-Tac-Toe");
        stage.setScene(scene);
        stage.show();
    }

    public static void switchScene(String fxmlFile) {
        Platform.runLater(() -> {  // Run this on the JavaFX UI thread
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
                Scene newScene = new Scene(loader.load(), 600, 400); // Set new scene size
                primaryStage.setScene(newScene);
                primaryStage.setWidth(600);  // Adjust width
                primaryStage.setHeight(400); // Adjust height
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
