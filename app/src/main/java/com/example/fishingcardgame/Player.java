package com.example.fishingcardgame;

import android.util.Log;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Player {
    private String name;
    private List<Card> hand;
    private int score;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void addCards(List<Card> cards) {
        hand.addAll(cards);
    }

    public void sortHand() {
        hand.sort(Comparator.comparingInt(card -> rankToIndex(card.getRank())));
    }

    public List<String> getValidRanks() {
        List<String> ranks = new ArrayList<>();
        for (Card card : hand) {
            if (!ranks.contains(card.getRank())) {
                ranks.add(card.getRank());
            }
        }
        return ranks;
    }

    public boolean hasRank(String rank) {
        for (Card card : hand) {
            if (card.getRank().equals(rank)) {
                return true;
            }
        }
        return false;
    }

    public List<Card> giveCards(String rank) {
        List<Card> cardsToGive = new ArrayList<>();
        hand.removeIf(card -> {
            if (card.getRank().equals(rank)) {
                cardsToGive.add(card);
                return true;
            }
            return false;
        });
        return cardsToGive;
    }

    public List<String> checkForSets() {
        List<String> collectedRanks = new ArrayList<>();
        int[] rankCounts = new int[13];  // Index represents rank (2-10, J, Q, K, A)

        try {
            for (Card card : hand) {
                int rankIndex = rankToIndex(card.getRank());
                rankCounts[rankIndex]++;
            }

            for (int i = 0; i < rankCounts.length; i++) {
                if (rankCounts[i] == 4) {
                    collectedRanks.add(indexToRank(i));
                }
            }
        } catch (Exception e) {
            Log.e("Player", "Error in checkForSets: " + e.getMessage());
        }

        return collectedRanks;
    }

    // Helper method to remove a collected set from the player's hand
    public ArrayList<Card> removeSet(String rank) {
        ArrayList<Card> cardsRemoved = new ArrayList<>();
        Iterator<Card> iterator = hand.iterator();  // Use an iterator for safe removal

        // Iterate through the hand and remove cards of the given rank
        while (iterator.hasNext()) {
            Card aCard = iterator.next();
            if (aCard.getRank().equals(rank)) {
                cardsRemoved.add(aCard);  // Add the card to the removed list
                iterator.remove();  // Safely remove the card from the hand
            }
        }

        return cardsRemoved;  // Return the list of removed cards
    }


    private int rankToIndex(String rank) {
        switch (rank) {
            case "2": return 0;
            case "3": return 1;
            case "4": return 2;
            case "5": return 3;
            case "6": return 4;
            case "7": return 5;
            case "8": return 6;
            case "9": return 7;
            case "10": return 8;
            case "J": return 9;
            case "Q": return 10;
            case "K": return 11;
            case "A": return 12;
            default: return -1;
        }
    }

    private String indexToRank(int index) {
        switch (index) {
            case 0: return "2";
            case 1: return "3";
            case 2: return "4";
            case 3: return "5";
            case 4: return "6";
            case 5: return "7";
            case 6: return "8";
            case 7: return "9";
            case 8: return "10";
            case 9: return "J";
            case 10: return "Q";
            case 11: return "K";
            case 12: return "A";
            default: return null;
        }
    }

    public void clearHand() {
        hand.clear();
    }

    public boolean isHuman() {
        return name.equals("Human");
    }
}
