package com.codecool.klondike;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertWindow {

    private static final Image defaultVictoryImage = new Image("file:resources/victory_screen/default.jpg");
    private static final Image horrorVictoryImage = new Image("file:resources/victory_screen/freedy.jpg");
    private static Image victoryImage = defaultVictoryImage;


    public static void setVictoryImage(boolean isHorror){
        if (isHorror){
            victoryImage = horrorVictoryImage;
        } else {
            victoryImage = defaultVictoryImage;
        }
    }

    public static void display(String title, String message) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(250);

        Label label = new Label();
        label.setText(message);
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> window.close());

        Image background = victoryImage;
        ImageView iv = new ImageView();

        iv.setImage(background);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(iv, closeButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }

}
