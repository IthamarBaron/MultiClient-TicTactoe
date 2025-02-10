package com.example.demo1;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.util.Duration;
import java.util.Random;

//Haha. thank you, ChatGPT, for the cool Confetti ;)
public class GameResultController {
    @FXML private Label resultLabel;
    @FXML private Button menuButton;
    @FXML private Pane confettiPane;
    @FXML private Label xWinsLabel;
    @FXML private Label oWinsLabel;
    @FXML private Label drawsLabel;


    private static final int CONFETTI_COUNT = 40;
    private static final Random random = new Random();

    private static GameResultController instance;

    public GameResultController() {
        instance = this;
    }

    public static GameResultController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        startConfettiEffect();

    }

    public void setGameResult(String resultText, int xWins, int oWins, int draws) {
            resultLabel.setText(resultText);
            xWinsLabel.setText("X Wins: " + xWins);
            oWinsLabel.setText("O Wins: " + oWins);
            drawsLabel.setText("Draws: " + draws);
    }


    @FXML
    private void onMenuButtonClick() {
        Platform.exit();
        System.exit(0);
    }

    private void startConfettiEffect() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(150), e -> createConfetti()));
        timeline.setCycleCount(CONFETTI_COUNT);
        timeline.play();
    }
    private void createConfetti() {
        Platform.runLater(() -> {
            Circle confetti = new Circle(6, Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
            confetti.setLayoutX(random.nextInt((int) confettiPane.getWidth()));
            confetti.setLayoutY(0);

            confettiPane.getChildren().add(confetti);

            Timeline fallAnimation = new Timeline(
                    new KeyFrame(Duration.seconds(1.0), new javafx.animation.KeyValue(confetti.layoutYProperty(), confettiPane.getHeight()))
            );
            fallAnimation.setOnFinished(event -> confettiPane.getChildren().remove(confetti));
            fallAnimation.play();
        });
    }
}
