package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
        if(card.getContainingPile().getPileType() == Pile.PileType.TABLEAU && card.isFaceDown() && card == card.getContainingPile().getTopCard()){
            card.flip();
            System.out.println(card + " flipped");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        if (activePile.getPileType() == Pile.PileType.DISCARD && activePile.getTopCard() != card) {
            return;
        }
        if (activePile.getPileType() == Pile.PileType.TABLEAU) {
            if (card.isFaceDown()) {
                return;
            }
        }
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;
        draggedCards.clear();
        makeCardMove(card, offsetX, offsetY);
        card.getDropShadow().setRadius(20);
        card.getDropShadow().setOffsetX(10);
        card.getDropShadow().setOffsetY(10);

        if (activePile.getTopCard() != card) {
            List<Card> tailCards = activePile.getCards();
            int index = tailCards.indexOf(card);
            for (int i = index + 1; i< tailCards.size();i++) {
                offsetX = e.getSceneX() - dragStartX;
                offsetY = e.getSceneY() - dragStartY;
                makeCardMove(tailCards.get(i), offsetX, offsetY);
            }
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        if (pile != null) {
            handleValidMove(card, pile);
        } else {
            pile = getValidIntersectingPile(card, foundationPiles);
            if (pile != null) {
                handleValidMove(card, pile);
            } else {
                draggedCards.forEach(MouseUtil::slideBack);
                draggedCards.clear();
            }
        }
    };

    public void makeCardMove(Card card, double offsetX, double offsetY) {
        draggedCards.add(card);
        card.toFront();
        card.setTranslateX(offsetX);
        card.setTranslateY(offsetY);
    };


    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game(Stage primaryStage) {
        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        initPiles();
        dealCards();

    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        System.out.println(discardPile.numOfCards());
        List<Card> cards = discardPile.getCards();
        Collections.reverse(cards);
        for (Card card : cards) {
            card.flip();
            stockPile.addCard(card);
        }
        discardPile.clear();
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        Card top = destPile.getTopCard();
        if(destPile.getPileType()==Pile.PileType.TABLEAU) {
            if (top == null) {
                if (card.getRank() != 13) {
                    return false;
                }
            } else {
                if (top.getRank() - 1 != card.getRank() || !Card.isOppositeColor(card, top)) {
                    return false;
                }
            }
        }
        if(destPile.getPileType()==Pile.PileType.FOUNDATION) {
            if (top == null) {
                if (card.getRank() != 1) {
                    return false;
                }
            } else {
                if (top.getRank() + 1 != card.getRank() || !Card.isSameSuit(card, top)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

        for (int i = 0; i < tableauPiles.size(); i++) {
            for (int j = 0; j < i; j++) {
                stockPile.getTopCard().moveToPile(tableauPiles.get(i));
            }
            Card top = stockPile.getTopCard();
            top.flip();
            top.moveToPile(tableauPiles.get(i));
        }
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    public Button setRestartButton(Stage primaryStage) {
        Button restartButton = new Button();
        restartButton = formatRestartButton(restartButton);
        restartButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Klondike.startGame(primaryStage);
            }
        });
        return restartButton;
    }

    private Button formatRestartButton(Button restartButton) {
        restartButton.setText("Restart");
        Image restartImage = new Image("/button.png");
        ImageView restartButtonImageView = new ImageView(restartImage);
        restartButtonImageView.setFitHeight(10);
        restartButtonImageView.setFitWidth(10);
        restartButton.setGraphic(restartButtonImageView);
        restartButton.setPrefWidth(80);
        restartButton.setPrefHeight(40);
        restartButton.setLayoutX(0);
        restartButton.setLayoutY(657);
        restartButton.setAlignment(Pos.CENTER);
        return restartButton;
    }

}
