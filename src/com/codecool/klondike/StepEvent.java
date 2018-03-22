package com.codecool.klondike;

public class StepEvent {

    public Card card;
    public Pile previousPile;
    public EventType et;

    public StepEvent(Card card, Pile pile, EventType event) {
        this.card = card;
        this.previousPile = pile;
        this.et = event;
    }

    public enum EventType {
        MOVE,
        FLIP,
        BOTH
    }
}
