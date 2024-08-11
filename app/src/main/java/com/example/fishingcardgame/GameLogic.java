package com.example.fishingcardgame;

import java.util.ArrayList;
import java.util.Arrays;

public class GameLogic {
    private Player humanPlayer;
    private ArrayList<Player> botPlayers;
    private Deck deck;

    public GameLogic() {
        deck = new Deck();
        humanPlayer = new Player("Human");
        botPlayers = new ArrayList<>(Arrays.asList(
                new Player("Alice"),
                new Player("Bob"),
                new Player("Charlie")
        ));

        for (int i = 0; i < 5; i++) {
            humanPlayer.drawCard(deck);
            for (Player bot : botPlayers) {
                bot.drawCard(deck);
            }
        }
    }

    public void playTurn(Player askingPlayer, Player askedPlayer, String rank) {
        if (askedPlayer.hasCard(rank)) {
            ArrayList<Card> givenCards = askedPlayer.giveCards(rank);
            for (Card card : givenCards) {
                askingPlayer.addCard(card);
            }
        } else {
            askingPlayer.drawCard(deck);
        }
    }

    public Player getHumanPlayer() {
        return humanPlayer;
    }

    public ArrayList<Player> getBotPlayers() {
        return botPlayers;
    }

    public Deck getDeck() {
        return deck;
    }
}

