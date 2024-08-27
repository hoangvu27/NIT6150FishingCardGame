package com.example.fishingcardgame;

import android.util.Log;

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
    boolean continueTurn = false;
    private boolean nextButtonClicked = false;
    private int totalRoundPoint = 0; // if this reaches 13, next button either starts next round or ends game./
    protected Player currentPlayer = null;
    private boolean startNextRound = false;
    protected int cardIndex = 0;  //  this is for delay for initial animation

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
        void onHumanTurnStart(Player currentPlayer, List<String> validRanks);
        void onTurnEnd();
        void onScoreUpdate(int humanScore, int[] botScores);
        void onBotAsk(Player bot, Player target, String rankAsked);
        void onGameOver(String winnerMessage);

        void playerEmpty();

        void onCardDistributed(Player player, Card card);

        void showCardAnimation();


        void playRound();

        void updateHumanHandView();

        void requestResult(Player askingPlayer, boolean requestSuccess, String rankAsked,
                           Player target, int numberCardReceived);

        void disableButtons();

        void enableButtons();

        void transferCardAnimation(Player humanPlayer, Player target, List<Card> cardsReceived);

        void scorePoint(Player aPlayer, ArrayList<Card> collectedCards );

        void updateBotHandView(Player bot);
    }

    // Simulates starting the game
    public void startGame() {
        setupRound();
        playRound();  //  enable button
//         endRound();  // prepare a new round & clean up old round or end game

    }

    // Setup the round by shuffling the deck and distributing cards
    void setupRound() {
        determineTurnOrder();
        currentPlayer = turnOrder.get(0);
        deck.shuffle();
        distributeInitialCards();
        totalRoundPoint = 0; // reset total round points
        for (Card card : humanPlayer.getHand()) {
            Log.d("card", "Human card: " + card.getRank() + " _ " + card.getSuit() );
        }
        for (Card card : getAlicePlayer().getHand()) {
            Log.d("card", "Alice card: " + card.getRank() + " _ " + card.getSuit() );
        }
        for (Card card : getBobPlayer().getHand()) {
            Log.d("card", "Bob card: " + card.getRank() + " _ " + card.getSuit() );
        }
        for (Card card : getCharliePlayer().getHand()) {
            Log.d("card", "Charlie card: " + card.getRank() + " _ " + card.getSuit() );
        }

//        testAnimation();
    }


//    private void testAnimation() {
//        gameListener.showCardAnimation();
//    }

    // Distribute 5 cards to each player
    private void distributeInitialCards() {
        this.cardIndex = 0;
        for (int j=0; j < turnOrder.size(); j++ ) {
            turnOrder.get(j).clearHand();
        }

        for (int i = 0; i < 5; i++) {
            for (int j=0; j < turnOrder.size(); j++ ) {
                Card aCard = deck.drawCard();
                turnOrder.get(j).addCard(aCard);
                gameListener.onCardDistributed(turnOrder.get(j), aCard);  // Notify UI to animate card distribution
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

    void playRound() {
        gameListener.playRound();
    }

    // Simulate a round of gameplay
//    void playRound() {
//    boolean roundOver = false;
//        while (roundOver == false) {
//            for (Player player : turnOrder) {
//                if (player.getHand().isEmpty()) {
//                    gameListener.playerEmpty();
//                    // !!!  NEED TO NOTIFY GAMELISTER
//                    continue;  // Skip players with no cards
//                }
//
//                continueTurn = true;
//                while (continueTurn == true && !isRoundOver()) {
//                    player.sortHand();  // UPDATE UI
//                    Log.d("tag", "after sort hand, inside playRound");
////                    nextButtonClicked = false;
//                    if (player.isHuman()) {
////                        waitingForHumanInput = true;
//                        gameListener.onHumanTurnStart(humanPlayer, humanPlayer.getValidRanks());
//                        // Wait for UI to trigger the human's move
////                        continueTurn = humanTurn(targetBot, chosenRank);
//                        return;
//                    } else {
//                        continueTurn = botTurn(player);
//                    }
//
//                    // Notify the UI to update scores after each turn
//                    gameListener.onScoreUpdate(humanScore, botScores);
//
//                    if (isRoundOver()) {
//                        roundOver = true;
//                        break;
//                    }
//
//                }
//
//                // End the round or game if all cards are collected
//                if (roundOver) {
//                    gameListener.onTurnEnd();
//                    break;
//                }
//            }
//        }
//    }

    // Handle the human's turn through UI input
    public void humanTurn(Player target, String rankAsked) {
        boolean requestSuccess;
        int numberCardReceived = 0;
        if (target.hasRank(rankAsked)) {
            List<Card> cardsReceived = target.giveCards(rankAsked);
            humanPlayer.addCards(cardsReceived);
//            checkForCollectedSets(bot);  // Check for any collected sets
            // Continue the turn. Thus, currentPlayer is same
            requestSuccess = true;
            numberCardReceived = cardsReceived.size();
            gameListener.transferCardAnimation(target , humanPlayer, cardsReceived);
        } else {
            Card temp = deck.drawCard();
            humanPlayer.addCard(temp);
            this.cardIndex = 0;
            gameListener.onCardDistributed(humanPlayer ,temp);
            gameListener.updateHumanHandView();
//            checkForCollectedSets(bot);
            setNextPlayer(this.currentPlayer);
//            if ( currentPlayer == getAlicePlayer()) { currentPlayer = getBobPlayer() ;}
//            else if ( currentPlayer == getBobPlayer() ) { currentPlayer = getCharliePlayer() ; }
//            else { currentPlayer = getHumanPlayer(); }
            requestSuccess = false;
            if (currentPlayer == getCharliePlayer()) {
                gameListener.enableButtons();
            }
        }
        checkForCollectedSets(humanPlayer);  // Check for any collected sets
        gameListener.requestResult(humanPlayer , requestSuccess, rankAsked, target, numberCardReceived);
    }

    // Simulate a bot's turn, returns true if the turn should continue
    void botTurn(Player bot) {

    }

    // Check if the round is over
//    private boolean isRoundOver() {
//        for (Player player : getAllPlayers()) {
//            if (!player.getHand().isEmpty()) {
//                return false;
//            }
//        }
//        return true;
//    }

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
            ArrayList<Card> collectedCards = player.removeSet(rank);
            totalRoundPoint++;
            gameListener.scorePoint(player, collectedCards);
        }
        gameListener.onScoreUpdate(humanScore, botScores);  // Notify UI to update the scores
    }

    // End the round and clear the players' hands
    private void endRound() {
        // Notify UI that the round has ended and update the scores
        gameListener.onScoreUpdate(humanScore, botScores);
    }

    // Determine the final winner after all rounds
    void determineWinner() {
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
        for (Player tempPlayer : validTargets) {
            if (tempPlayer.getHand().size() == 0 ) {
                validTargets.remove(tempPlayer);  //  players with no card should not be asked
            }
        }
        return validTargets;
    }

    List<Player> getBotPlayers() {
        return botPlayers;
    }

    Player getHumanPlayer() {
        return humanPlayer;
    }

    Deck getDeck() {
        return deck;
    }

    public Player getAlicePlayer() {
        return botPlayers.get(0);
    }

    public Player getBobPlayer() {
        return botPlayers.get(1);
    }

    public Player getCharliePlayer() {
        return botPlayers.get(2);
    }

    public int getTotalRoundPoint() {
        return totalRoundPoint;
    }

    public boolean isRoundOver() {
        return totalRoundPoint == 13 && turnOrder.get(0) != getCharliePlayer();
    }
    public boolean isGameOver() {
        return totalRoundPoint == 13 && turnOrder.get(0) == getCharliePlayer();
    }

    public void setStartNextRound(boolean startNextRound) {
        this.startNextRound = startNextRound;
    }

    public boolean isStartNextRound() {
        return startNextRound;
    }

    public void setNextPlayer(Player aPlayer){
        int temp = turnOrder.indexOf(aPlayer);
        if ( temp == 3 ) {
            this.currentPlayer =  turnOrder.get(0);
        } else {
            this.currentPlayer =  turnOrder.get(temp + 1);
        }
    }
}
