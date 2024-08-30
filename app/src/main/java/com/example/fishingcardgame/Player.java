package com.example.fishingcardgame;

import android.util.Log;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * The Player class is responsible for possible actions that player  object can perform
 */
public class Player {
    private String name;
    private List<Card> hand;
    private int score;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.score = 0;
    }

    /**
     * Get name of player
     * @return name of player
     */
    public String getName() {
        return name;
    }

    /**
     * Get all cards of player
     * @return all cards of player
     */
    public List<Card> getHand() {
        return hand;
    }

    /**
     * Add new card to player hand
     * @param card the card that will be added
     */
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

    /**
     * Check if a player has a rank
     * @param rank the rank that will be used to check
     * @return true if a player has the rank
     */
    public boolean hasRank(String rank) {
        for (Card card : hand) {
            if (card.getRank().equals(rank)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all cards with a rank from a player hand
     * @param rank the rank that is in request
     * @return all cards that have the rank
     */
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

    /**
     * Helper method to remove a collected set from the player's hand
     * @param rank that will be removed
     * @return a set of 4 cards of the same rank
     */
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

    /**
     * Clear all hand of player
     */
    public void clearHand() {
        hand.clear();
    }

    /**
     * Check if player is human
     * @return true if player is human
     */
    public boolean isHuman() {
        return name.equals("Human");
    }
}
