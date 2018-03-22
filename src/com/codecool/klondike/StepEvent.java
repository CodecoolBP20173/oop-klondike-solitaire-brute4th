package com.codecool.klondike;

import java.util.ArrayList;
import java.util.List;

public class StepEvent {

    public List<Card> cards = new ArrayList<>();
    public Pile previousPile;
    public EventType et;

    StepEvent(Card card, Pile pile, EventType event) {
        this.cards.add(card);
        this.previousPile = pile;
        this.et = event;
    }

    StepEvent(List<Card> cards, Pile pile, EventType event){
        this.cards.addAll(cards);
        this.previousPile = pile;
        this.et = event;
    }

    StepEvent(EventType event){
        this.et = event;
    }

    public enum EventType {
        MOVE,
        FLIP,
        BOTH,
        MULTIPLE,
        STOCK
    }
}
