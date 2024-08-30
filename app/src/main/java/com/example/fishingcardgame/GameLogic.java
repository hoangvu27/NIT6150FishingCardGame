
package com.example.fishingcardgame;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * The GameLogic class is responsible for handling the game flow, including managing
 * players, scoring, and turns. It interfaces with the Deck and Player classes to
 * control the state and progression of the game.
 */
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
    private Player scoringPlayer = null;
    private List<String> collectedRanks = new ArrayList<String>();

    private GameListener gameListener;  // Interface to notify UI about game events

    /**
     * Initializes the GameLogic with the necessary components.
     */
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
        void onScoreUpdate(int humanScore, int[] botScores);
        void onGameOver(String winnerMessage);

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

    /**
     * start the game
     */
    public void startGame() {
        setupRound();
//        testAnimation();
        playRound();  //  enable button
//         endRound();  // prepare a new round & clean up old round or end game

    }

    private void testAnimation() {
        gameListener.showCardAnimation();
    }

    /**
     *  Setup the round by shuffling the deck and distributing cards
     */
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

    }

    /**
     * Initially distribute 5 cards to each player
     */
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

    /**
     * Determine the turn order for the current round
     */
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

    /**
     * Notify UI that a new round is played. It is a part of process of setting up a new round
     */
    void playRound() {
        gameListener.playRound();
    }

    /**
     * Play human turn
     * @param target the player is asked by human
     * @param rankAsked the rank that human player asks
     */
    // Handle the human's turn through UI input
    public void humanTurn(Player target, String rankAsked) {
        // There will be no updateHandView either for human turn or for bot turn
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
            if (!deck.isEmpty()) {
                Card temp = deck.drawCard();
                humanPlayer.addCard(temp);
                if (deck.isEmpty()) { gameListener.onDeckEmpty(); }
                this.cardIndex = 0;
                gameListener.onCardDistributed(humanPlayer ,temp);
            }

            requestSuccess = false;
            setNextPlayer(this.currentPlayer);
            gameListener.disableButtons();
        }
        score = checkForCollectedSets(humanPlayer);
        gameListener.requestResult(humanPlayer , requestSuccess, rankAsked, target, numberCardReceived, score);
    }

    /**
     *  Simulate a bot's turn, returns true if the turn should continue
     * @param bot the bot who is currently playing
     */
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
            if (!deck.isEmpty()) {
                Card temp = deck.drawCard();
                bot.addCard(temp);
                if (deck.isEmpty()) { gameListener.onDeckEmpty(); }

                this.cardIndex = 0;
                gameListener.onCardDistributed(bot ,temp);
            }

            requestSuccess = false;
            if (currentPlayer == getCharliePlayer()) {
                gameListener.enableButtons();
            }
            setNextPlayer(this.currentPlayer);
        }
        score = checkForCollectedSets(bot);  // Check for any collected sets
        gameListener.requestResult(bot , requestSuccess, rankAsked, target, numberCardReceived, score);
    }

    /**
     * Check for collected sets of 4 cards of the same rank
     * @param player the player who might have collected a set of 4 cards
     * @return true if a set of 4 cards of same rank has been collected
     */
    private boolean checkForCollectedSets(Player player) {
        boolean score = false;
        collectedRanks.clear();
        collectedRanks = player.checkForSets();
        for (String rank : collectedRanks) {
            if (player.isHuman()) {
                humanScore++;
            } else {
                int botIndex = botPlayers.indexOf(player);
                botScores[botIndex]++;
            }
//            collectedCards = player.removeSet(rank);  //  THIS ALREADY REMOVE A SET OF 4 CARDS.
            // THUS, HAND VIEW WILL BE UPDATED ACCORDINGLY
            totalRoundPoint++;

            score = true;
            this.scoringPlayer = player;
            // need to setText status
            gameListener.disableButtons();
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

    /**
     *  Determine the final winner after all rounds
     */
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

    /**
     * Get all players (human + bots)
     * @return  all players (human + bots)
     */
    private List<Player> getAllPlayers() {
        List<Player> allPlayers = new ArrayList<>(botPlayers);
        allPlayers.add(humanPlayer);
        return allPlayers;
    }

    /**
     * Get valid targets for a bot
     * @param bot the bot that seeks valid targets to ask
     * @return valid targets for a bot
     */
    private List<Player> getValidTargets(Player bot) {
        List<Player> validTargets = new ArrayList<>();
        for (Player tempPlayer : botPlayers) {
            if  (tempPlayer.getHand().size() > 0 ) {
                validTargets.add(tempPlayer);
            }
        }
        if (humanPlayer.getHand().size() > 0 ) {
            validTargets.add(humanPlayer);
        }
        validTargets.remove(bot);  // Bot cannot ask itself
        return validTargets;
    }

    /**
     * Get all bot players
     * @return all bot players
     */
    List<Player> getBotPlayers() {
        return botPlayers;
    }

    /**
     * Get human player
     * @return human player
     */
    Player getHumanPlayer() {
        return humanPlayer;
    }

    /**
     * Get deck
     * @return deck
     */
    Deck getDeck() {
        return deck;
    }

    /**
     * Get Alice player
     * @return Alice player
     */
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

    /**
     * Check if a round is over
     * @return true if round is over
     */
    public boolean isRoundOver() {
        return totalRoundPoint == 13 && turnOrder.get(0) != getCharliePlayer();
    }

    /**
     * Check if game is over
     * @return true if game is over
     */
    public boolean isGameOver() {
        return totalRoundPoint == 13 && turnOrder.get(0) == getCharliePlayer();
    }

    /**
     * Trigger a new round
     * @param startNextRound true if a new round is ready to begin
     */
    public void setStartNextRound(boolean startNextRound) {
        this.startNextRound = startNextRound;
        currentRound++ ;
    }

    /**
     * Check if next round has begun
     * @return true if a next round is about to begin
     */
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

    public Player getScoringPlayer() {
        return scoringPlayer;
    }

    public void setScoringPlayer(Player scoringPlayer) {
        this.scoringPlayer = scoringPlayer;
    }

    public List<String> collectedRanks() {
        return this.collectedRanks;
    }

    public int getHumanScore() {
        return humanScore;
    }

    public int[] getBotScores() {
        return botScores;
    }
}
