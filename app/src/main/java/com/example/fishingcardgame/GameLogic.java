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
//        deck = new Deck();
        humanPlayer = new Player("Human");
        botPlayers = new ArrayList<>();
        botPlayers.add(new Player("Alice"));
        botPlayers.add(new Player("Bob"));
        botPlayers.add(new Player("Charlie"));
        botScores = new int[3];  // Alice, Bob, Charlie
    }

    // Interface to communicate with the Android UI
    public interface GameListener {
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
                           Player target, int numberCardReceived, boolean score);

        void disableButtons();

        void enableButtons();

        void transferCardAnimation(Player humanPlayer, Player target, List<Card> cardsReceived);

        void scorePoint(Player aPlayer, ArrayList<Card> collectedCards );

        void updateBotHandView(Player bot);

        void updateSpinner();

        void onDeckEmpty();

        void refillDeck();
    }

    // Simulates starting the game
    public void startGame() {
        setupRound();
//        testAnimation();
        playRound();  //  enable button
//         endRound();  // prepare a new round & clean up old round or end game

    }

    private void testAnimation() {
        gameListener.showCardAnimation();
    }

    // Setup the round by shuffling the deck and distributing cards
    void setupRound() {
        deck = new Deck();
        gameListener.refillDeck();
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


    // Handle the human's turn through UI input
    public void humanTurn(Player target, String rankAsked) {
        boolean requestSuccess;
        boolean score = false;
        int numberCardReceived = 0;
        if (target.hasRank(rankAsked)) {
            List<Card> cardsReceived = target.giveCards(rankAsked);
            humanPlayer.addCards(cardsReceived);
            requestSuccess = true;
            numberCardReceived = cardsReceived.size();
            gameListener.transferCardAnimation(target , humanPlayer, cardsReceived);
        } else {
            Card temp = deck.drawCard();
            humanPlayer.addCard(temp);
            if (deck.isEmpty()) { gameListener.onDeckEmpty(); }

            this.cardIndex = 0;
            gameListener.onCardDistributed(humanPlayer ,temp);
            requestSuccess = false;

            setNextPlayer(this.currentPlayer);
            if (currentPlayer == getCharliePlayer()) {
                gameListener.disableButtons();
            }
        }
        score = checkForCollectedSets(humanPlayer);
        gameListener.requestResult(humanPlayer , requestSuccess, rankAsked, target, numberCardReceived, score);
    }

    // Simulate a bot's turn, returns true if the turn should continue
    void botTurn(Player bot) {
        List<Player> validTargets = getValidTargets(bot);
        Player target = validTargets.get((int) (Math.random() * validTargets.size()));
        String rankAsked = bot.getValidRanks().get((int) (Math.random() * bot.getValidRanks().size()));

        boolean requestSuccess;
        int numberCardReceived = 0;
        boolean score = false;

        if (target.hasRank(rankAsked)) {
            List<Card> cardsReceived = target.giveCards(rankAsked);
            bot.addCards(cardsReceived);
            requestSuccess = true;
            numberCardReceived = cardsReceived.size();
            gameListener.transferCardAnimation(target , bot, cardsReceived);
        } else {
            Card temp = deck.drawCard();
            bot.addCard(temp);
            if (deck.isEmpty()) { gameListener.onDeckEmpty(); }

            this.cardIndex = 0;
            requestSuccess = false;
            gameListener.onCardDistributed(bot ,temp);

            if (currentPlayer == getCharliePlayer()) {
                gameListener.enableButtons();
            }
            setNextPlayer(this.currentPlayer);
        }
        score = checkForCollectedSets(bot);  // Check for any collected sets
        gameListener.requestResult(bot , requestSuccess, rankAsked, target, numberCardReceived, score);
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
    private boolean checkForCollectedSets(Player player) {
        boolean score = false;
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
            gameListener.onScoreUpdate(humanScore, botScores);
            score = true;
        }
        if (player == humanPlayer) {
            gameListener.updateSpinner();
        }
        return score;
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
