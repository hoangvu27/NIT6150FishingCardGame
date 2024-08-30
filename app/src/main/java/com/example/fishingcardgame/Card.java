package com.example.fishingcardgame;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Card  {
    private String suit;
    private String rank;
    public static int cardWidth = 60;
    public static int cardHeight = 90;

    public Card() {

    }

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public class RankComparator implements Comparator<Card> {
        private final List<String> RANK_ORDER = Arrays.asList(
                "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"
        );

        @Override
        public int compare(Card card1, Card card2) {
            int rank1Index = RANK_ORDER.indexOf(card1.getRank());
            int rank2Index = RANK_ORDER.indexOf(card2.getRank());

            return Integer.compare(rank1Index, rank2Index);  // Ascending order by rank
        }
    };

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    /**
     * Get the source name of image for the card
     * @return the name of image source for the card
     */
    public String getImageName() {
        return "card_" + this.getRank().toLowerCase() + "_of_" + this.getSuit().toLowerCase();
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }

    /**
     * Get the card
     * @return the card
     */
    public Card getCard() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; // If the same reference, they're equal
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false; // Different classes cannot be equal
        }

        Card otherCard = (Card) obj; // Cast obj to Card
        // Compare rank and suit for logical equality
        return rank.equals(otherCard.rank) && suit.equals(otherCard.suit);
    }

    @Override
    public int hashCode() {
        // Generate a hash code based on rank and suit
        return Objects.hash(rank, suit);
    }

}
