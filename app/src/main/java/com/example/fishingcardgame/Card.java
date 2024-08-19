package com.example.fishingcardgame;

public class Card {
    private String suit;
    private String rank;
    public static int cardWidth = 60;
    public static int cardHeight = 90;

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    public String getImageName() {
        return "card_" + this.getRank().toLowerCase() + "_of_" + this.getSuit().toLowerCase();
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }

    public Card getCard() {
        return this;
    }

}
