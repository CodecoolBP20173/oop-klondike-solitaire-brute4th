package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.sql.Time;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        startGame(primaryStage);
    }

    public static void startGame(Stage primaryStage) {
        Card.loadCardImages();
        Game game = new Game(primaryStage);
        game.setTableBackground(new Image("/table/green.png"));

        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        game.getChildren().add(game.setRestartButton(primaryStage));
        primaryStage.show();

        game.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) {
                Thread t1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        game.cheat();
                    }

                });
                t1.start();
                //System.out.println("Cheat button pressed!");
            }
        });
    }

}
