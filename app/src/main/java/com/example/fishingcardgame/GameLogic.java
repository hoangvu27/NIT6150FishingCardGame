package com.example.fishingcardgame;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    private Deck deck;
    private Player humanPlayer;
    private List<Player> botPlayers;
    private int currentRound = 1;
    private List<Player> turnOrder;
    private int humanScore = 0;
    private int[] botScores;

    private GameListener gameListener;  // Interface to notify UI about game events

    public GameLogic(GameListener listener) {
        this.gameListener = listener;
        deck = new Deck();
        humanPlayer = new Player("Human");
        botPlayers = new ArrayList<>();
        botPlayers.add(new Player("Alice"));
        botPlayers.add(new Player("Bob"));
        botPlayers.add(new Player("Charlie"));
        botScores = new int[3];  // Alice, Bob, Charlie
    }

    // Interface to communicate with the Android UI
    public interface GameListener {
        void onTurnStart(Player currentPlayer, List<String> validRanks);
        void onTurnEnd();
        void onScoreUpdate(int humanScore, int[] botScores);
        void onBotAsk(Player bot, Player target, String rankAsked);
        void onGameOver(String winnerMessage);
    }

    // Simulates starting the game
    public void startGame() {
        for (currentRound = 1; currentRound <= 4; currentRound++) {
            setupRound();
            playRound();
//            endRound();
        }
        determineWinner();
    }

    // Setup the round by shuffling the deck and distributing cards
    private void setupRound() {
        deck.shuffle();
        distributeInitialCards();
        determineTurnOrder();
    }

    // Distribute 5 cards to each player
    private void distributeInitialCards() {
        for (Player player : getAllPlayers()) {
            player.clearHand();
            for (int i = 0; i < 5; i++) {
                player.addCard(deck.drawCard());
            }
        }
    }

    // Determine the turn order for the current round
    private void determineTurnOrder() {
        if (currentRound == 1) {
            turnOrder = List.of(humanPlayer, botPlayers.get(0), botPlayers.get(1), botPlayers.get(2));
        } else if (currentRound == 2) {
            turnOrder = List.of(botPlayers.get(0), botPlayers.get(1), botPlayers.get(2), humanPlayer);
        } else if (currentRound == 3) {
            turnOrder = List.of(botPlayers.get(1), botPlayers.get(2), humanPlayer, botPlayers.get(0));
        } else if (currentRound == 4) {
            turnOrder = List.of(botPlayers.get(2), humanPlayer, botPlayers.get(0), botPlayers.get(1));
        }
    }

    // Simulate a round of gameplay
    private void playRound() {
        boolean roundOver = false;

        while (!roundOver) {
            for (Player player : turnOrder) {
                if (player.getHand().isEmpty()) {
                    continue;  // Skip players with no cards
                }

                boolean continueTurn = true;
                while (continueTurn && !isRoundOver()) {
                    player.sortHand();  // Sort hand before each turn

                    if (player.isHuman()) {
                        gameListener.onTurnStart(humanPlayer, humanPlayer.getValidRanks());
                        // Wait for UI to trigger the human's move
                        return;  // Exit the loop to wait for human input
                    } else {
                        continueTurn = botTurn(player);
                    }

                    // Notify the UI to update scores after each turn
                    gameListener.onScoreUpdate(humanScore, botScores);

                    if (isRoundOver()) {
                        roundOver = true;
                        break;
                    }
                }

                // End the round or game if all cards are collected
                if (roundOver) {
                    gameListener.onTurnEnd();
                    break;
                }
            }
        }
    }

    // Handle the human's turn through UI input
    public void humanTurn(Player target, String rankAsked) {
        boolean continueTurn = false;

        if (target.hasRank(rankAsked)) {
            List<Card> cardsReceived = target.giveCards(rankAsked);
            humanPlayer.addCards(cardsReceived);
            checkForCollectedSets(humanPlayer);  // Check for any collected sets
            continueTurn = true;  // Allow the human to continue the turn
        } else {
            humanPlayer.addCard(deck.drawCard());
            checkForCollectedSets(humanPlayer);
            continueTurn = false;  // End the turn as the human must draw a card
        }

        // If the turn continues, trigger it again, otherwise proceed with the next player's turn
        if (continueTurn) {
            playRound();  // Continue the round
        } else {
            gameListener.onTurnEnd();  // Notify UI to end the turn
        }
    }

    // Simulate a bot's turn, returns true if the turn should continue
    private boolean botTurn(Player bot) {
        List<Player> validTargets = getValidTargets(bot);
        Player target = validTargets.get((int) (Math.random() * validTargets.size()));
        String rankAsked = bot.getValidRanks().get((int) (Math.random() * bot.getValidRanks().size()));

        // Notify UI about the bot's action
        gameListener.onBotAsk(bot, target, rankAsked);

        if (target.hasRank(rankAsked)) {
            List<Card> cardsReceived = target.giveCards(rankAsked);
            bot.addCards(cardsReceived);
            checkForCollectedSets(bot);  // Check for any collected sets
            return true;  // Continue the turn
        } else {
            bot.addCard(deck.drawCard());
            checkForCollectedSets(bot);
            return false;  // End the turn
        }
    }

    // Check if the round is over
    private boolean isRoundOver() {
        for (Player player : getAllPlayers()) {
            if (!player.getHand().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // Check for collected sets of 4 cards of the same rank
    private void checkForCollectedSets(Player player) {
        List<String> collectedRanks = player.checkForSets();
        for (String rank : collectedRanks) {
            if (player.isHuman()) {
                humanScore++;
            } else {
                int botIndex = botPlayers.indexOf(player);
                botScores[botIndex]++;
            }
            player.removeSet(rank);
        }
        gameListener.onScoreUpdate(humanScore, botScores);  // Notify UI to update the scores
    }

    // End the round and clear the players' hands
    private void endRound() {
        // Notify UI that the round has ended and update the scores
        gameListener.onScoreUpdate(humanScore, botScores);
    }

    // Determine the final winner after all rounds
    private void determineWinner() {
        StringBuilder winnerMessage = new StringBuilder("Game Over!\nFinal Scores:\n");
        winnerMessage.append("Human: ").append(humanScore).append("\n");
        winnerMessage.append("Alice: ").append(botScores[0]).append("\n");
        winnerMessage.append("Bob: ").append(botScores[1]).append("\n");
        winnerMessage.append("Charlie: ").append(botScores[2]).append("\n");

        int highestScore = Math.max(humanScore, Math.max(botScores[0], Math.max(botScores[1], botScores[2])));
        List<String> winners = new ArrayList<>();

        if (humanScore == highestScore) winners.add("Human");
        if (botScores[0] == highestScore) winners.add("Alice");
        if (botScores[1] == highestScore) winners.add("Bob");
        if (botScores[2] == highestScore) winners.add("Charlie");

        winnerMessage.append("Winner(s): ").append(String.join(", ", winners));

        gameListener.onGameOver(winnerMessage.toString());
    }

    // Get all players (human + bots)
    private List<Player> getAllPlayers() {
        List<Player> allPlayers = new ArrayList<>(botPlayers);
        allPlayers.add(humanPlayer);
        return allPlayers;
    }

    // Get valid targets for a bot
    private List<Player> getValidTargets(Player bot) {
        List<Player> validTargets = new ArrayList<>(botPlayers);
        validTargets.add(humanPlayer);
        validTargets.remove(bot);  // Bot cannot ask itself
        return validTargets;
    }
}
