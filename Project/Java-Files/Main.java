package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.application.Platform;

public class Main extends Application {

    public static Client client = null;
    public static Stage primaryStage;
    public static int roomID;
    public static String playerOne;
    public static String playerTwo;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage; // Store stage reference
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Tic-Tac-Toe");
        stage.setScene(scene);
        stage.show();
    }

    public static void switchScene(String fxmlFile, String resultText) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlFile));
                Scene newScene = new Scene(loader.load());
                primaryStage.setScene(newScene);
                primaryStage.setWidth(600);
                primaryStage.setHeight(400);
                primaryStage.setResizable(true);
                primaryStage.show();

                if (fxmlFile.equals("game-result.fxml")) {
                    GameResultController controller = loader.getController();
                    //controller.setGameResult(resultText);
                }
                else if (fxmlFile.equals("room.fxml")) {
                    primaryStage.setWidth(480);
                    primaryStage.setHeight(480);
                    primaryStage.setResizable(false);

                    RoomController roomController = loader.getController();
                    roomController.setupRoom(roomID, playerOne, playerTwo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public static void main(String[] args) {
        launch();
    }
}
