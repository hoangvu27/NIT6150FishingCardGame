package com.example.fishingcardgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Player {
    private String name;
    private ArrayList<Card> hand;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public void drawCard(Deck deck) {
        Card card = deck.drawCard();
        if (card != null) {
            hand.add(card);
        }
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public boolean hasCard(String rank) {
        for (Card card : hand) {
            if (card.getRank().equals(rank)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Card> giveCards(String rank) {
        ArrayList<Card> givenCards = new ArrayList<>();
        for (int i = hand.size() - 1; i >= 0; i--) {
            if (hand.get(i).getRank().equals(rank)) {
                givenCards.add(hand.remove(i));
            }
        }
        return givenCards;
    }

    public String getName() {
        return name;
    }
}
