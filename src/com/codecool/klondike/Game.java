package com.codecool.klondike;

import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
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

    private static MediaPlayer player;
    private static Media music;

    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
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
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        draggedCards.add(card);

        card.getDropShadow().setRadius(20);
        card.getDropShadow().setOffsetX(10);
        card.getDropShadow().setOffsetY(10);

        card.toFront();
        card.setTranslateX(offsetX);
        card.setTranslateY(offsetY);
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

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        initPiles();
        dealCards();
        musicPlayer("resources/audio/ambient.mp3");
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
        if (destPile.getPileType() == Pile.PileType.TABLEAU) {
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
        if (destPile.getPileType() == Pile.PileType.FOUNDATION) {
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

    public ComboBox<String> setComboBox() {
        ComboBox<String> themes = new ComboBox<String>();
        themes.setPromptText("Theme");
        themes.getItems().addAll("Green", "Horror");
        themes.getSelectionModel().selectFirst();
        themes.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                if (themes.getValue() == "Green") {
                    changeTheme("table/green.png", "resources/audio/ambient.mp3", "default/");
                }
                if (themes.getValue() == "Horror") {
                    changeTheme("table/red.jpg", "resources/audio/Doom.mp3", "horror/");
                }
            }
        });
        return themes;
    }

    public void musicPlayer(String musicFile) {
        if (player != null){player.dispose();}
        music = new Media(new File(musicFile).toURI().toString());
        player = new MediaPlayer(music);
        player.setAutoPlay(true);
        player.setCycleCount(Timeline.INDEFINITE); //It puts MediaPlayer in an infinite loop
    }

    public void changeTheme(String bgImgUrl, String musicFile, String themeUrl){
        setTableBackground(new Image(bgImgUrl));
        musicPlayer(musicFile);
        Card.loadCardImages(themeUrl);
        for (int i = 0; i < deck.size(); i++) {
            Card currentCard = deck.get(i);
            currentCard.changeCardTheme();
        }
    }
}
