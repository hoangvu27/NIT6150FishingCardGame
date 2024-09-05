/**
 * MainActivity handles the main user interface and interactions for the game.
 * It includes animations, event listeners, and manages the overall gameplay
 * experience.
 */

package com.example.fishingcardgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MainActivity sets up the game UI, handles user interactions, and manages
 * animations. It extends AppCompatActivity and implements required listeners.
 */
public class MainActivity extends AppCompatActivity implements GameLogic.GameListener {

    // Constants and fields with appropriate naming conventions
    private static final int ANIMATION_DURATION = 500;

    private GameLogic gameLogic;
    private ViewGroup playerHandView, aliceCardsView, bobCardsView, charlieCardsView;
    private Spinner rankSpinner;
    private Spinner botSpinner;
    private Button nextButton;
    private TextView statusText;
    private int cardWidth;
    private int cardHeight;
    private TextView humanScoreTextView, aliceScoreTextView, bobScoreTextView, charlieScoreTextView;

    private ViewGroup deckLayout;
    private Map<Player, ViewGroup> handViews;
    private Map<Card, ImageView> cardViewMap = new HashMap<>();
    private int screenWidthPx = 0;
    private int screenHeightPx = 0;
    private ArrayList<ImageView> animatedCardViewList = new ArrayList<ImageView>();
    private ImageButton gameRulesButton, settingsButton = null;

    private VideoView videoView;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Convert dp to pixels
        cardWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Card.cardWidth, getResources().getDisplayMetrics());
        cardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Card.cardHeight, getResources().getDisplayMetrics());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidthPx = displayMetrics.widthPixels;
        screenHeightPx = displayMetrics.heightPixels;

        deckLayout = findViewById(R.id.deckLayout);
        playerHandView = findViewById(R.id.playerHand);
        aliceCardsView = findViewById(R.id.aliceCards);
        bobCardsView = findViewById(R.id.bobCards);
        charlieCardsView = findViewById(R.id.charlieCards);


        rankSpinner = findViewById(R.id.rankSpinner);
        botSpinner = findViewById(R.id.botSpinner);
        nextButton = findViewById(R.id.nextButton);
        statusText = findViewById(R.id.bot_actions_text_view);
        humanScoreTextView = findViewById(R.id.human_score_text_view);
        aliceScoreTextView = findViewById(R.id.alice_score_text_view);
        bobScoreTextView = findViewById(R.id.bobScore);
        charlieScoreTextView = findViewById(R.id.charlieScore);

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        gameRulesButton = findViewById(R.id.gameRulesButton);
//        videoView = findViewById(R.id.videoView);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonClicked();
            }
        });

        gameRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGameRulesDialog();
            }
        });

        gameLogic = new GameLogic(MainActivity.this);
        handViews = new HashMap<>();
        handViews.put(gameLogic.getHumanPlayer(), playerHandView);
        handViews.put(gameLogic.getAlicePlayer(), aliceCardsView);
        handViews.put(gameLogic.getBobPlayer(), bobCardsView);
        handViews.put(gameLogic.getCharliePlayer(), charlieCardsView);
        // Now all views have been laid out, start the game logic
        List<String> botNames = new ArrayList<>();
        botNames.add(gameLogic.getAlicePlayer().getName());
        botNames.add(gameLogic.getBobPlayer().getName());
        botNames.add(gameLogic.getCharliePlayer().getName());
        ArrayAdapter<String> botAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, botNames);
        botAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        botSpinner.setAdapter(botAdapter);
        gameLogic.startGame();
    }

    /**
     * Method to display the Settings dialog
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        builder.setItems(new CharSequence[]{"Quit Game"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Quit Game option
                        quitGame();
                        break;
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Close the dialog when "Cancel" is clicked
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Method to handle quitting the game
     */
    private void quitGame() {
        Toast.makeText(this, "Quitting Game...", Toast.LENGTH_SHORT).show();
        // Add logic to quit the game, such as finishing the activity
        finish(); // This will close the activity and effectively quit the game
    }

    /**
     * Method to display the Game Rules dialog
     */
    private void showGameRulesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Rules");
        builder.setMessage("1. Rule 1: Game has 4 rounds. Each round starts with different player. Turn order is clockwise. " +
                "\n2. Rule 2: Each player can only ask another player about a rank that they have. " +
                        "If asked player has the rank, asking player get all cards with the rank, and "
                        + "continues turn. " + "If not, asking player draws a card " +
                "\n3. Rule 3: A round only ends when no player holds any card on hands. " +
                        "Player with most points when game ends will become a winner" +
                "\n4. Rule 4: Turn will be skipped if deck is empty and player has no card." +
                "\n5. Rule 5: A player score 1 point if collecting a set of 4 cards with same rank");
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Close the dialog when "Close" is clicked
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Hide deck view when deck is empty
     */
    @Override
    public void onDeckEmpty() {
        deckLayout.setVisibility(View.GONE);  // Hide the deck when no cards are left
    }

    /**
     * Refill deck after a round is over
     */
    public void refillDeck() {
        deckLayout.setVisibility(View.VISIBLE);
    }

    /**
     * A part of setting up a new round
     */
    public void playRound() {
        View rootView = findViewById(android.R.id.content);

        // Attach the OnGlobalLayoutListener to the root view
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nextButton.setEnabled(true);
                        botSpinner.setEnabled(true);
                        rankSpinner.setEnabled(true);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.spinner_item, gameLogic.getHumanPlayer().getValidRanks());
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        android.R.layout.simple_spinner_dropdown_item
                        rankSpinner.setAdapter(adapter);
                    }
                }, 2000);
            }
        });
    }

    /**
     * Update the rank spinner button
     */
    public void updateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.spinner_item, gameLogic.getHumanPlayer().getValidRanks());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rankSpinner.setAdapter(adapter);
    }



    /**
     * Give all functions and actions for the next button
     */
    private void onNextButtonClicked() {
        if (gameLogic.getScoringPlayer() != null) {
//            if (gameLogic.getTotalRoundPoint() == 1 && !playOnce  ) {
//                if (videoView.getVisibility() == View.VISIBLE ) {
//                    stopAndHideVideo(); // skip video by clicking "next" button
//                } else {
//                    playVideo();
//                }
//                return;
//            }

            String message = gameLogic.getScoringPlayer().getName() + " has collected 4 cards of " + gameLogic.getScoringRank() + " and get 1 score";
            statusText.setText(message);
            String collectedRank = gameLogic.collectedRanks().get(0);
            scorePoint(gameLogic.getScoringPlayer() , gameLogic.getScoringPlayer().removeSet(collectedRank));
            onScoreUpdate(gameLogic.getHumanScore(), gameLogic.getBotScores());
            if (gameLogic.currentPlayer.isHuman() ) {
                enableButtons();
                updateSpinner();
                // only enable buttons if this is Charlie scoring animation
            }
            gameLogic.setScoringPlayer(null);
            gameLogic.setScoringRank("");

            return;
        }

        if (gameLogic.isGameOver()) {
            gameLogic.determineWinner();
        }
        if (gameLogic.isRoundOver() && gameLogic.isStartNextRound() == false) {
//          ALl handView should be empty, but clear all UI if necessary
            statusText.setText("Round is over. Click next to start next round");
            gameLogic.setStartNextRound(true);
            cardViewMap.clear();
            return;
        }
        if (gameLogic.isStartNextRound()) {
            gameLogic.setupRound();
            gameLogic.playRound();
            gameLogic.setStartNextRound(false);
            Log.d("tag", "this is next round");
            return;
        }
        if (gameLogic.currentPlayer.getHand().size() == 0 && gameLogic.getDeck().isEmpty() == false ) {
            // Allows player with empty hand to draw a card and play
            Card temp = gameLogic.getDeck().drawCard();
            gameLogic.currentPlayer.addCard(temp);
        }
        if (gameLogic.currentPlayer.getHand().size() == 0 && gameLogic.getDeck().isEmpty() ) {
            statusText.setText("Deck is empty and current player has no card. Turn skipped");
            gameLogic.setNextPlayer(gameLogic.currentPlayer);
            return;
        }

        if (gameLogic.currentPlayer.isHuman()) {
            updateHumanHandView(); // update hand view here may be ineffective
            Player targetBot = getSelectedBot();
            String selectedRank = getSelectedRank();

            if (targetBot != null && selectedRank != null) {
                gameLogic.humanTurn(targetBot, selectedRank);
//                refreshCard = true;
//                refreshHandButton.setEnabled(true);
//                nextButton.setEnabled(false);

            } else {
                statusText.setText("Please select a bot and a rank.");
            }
        } else {
            updateBotHandView(gameLogic.currentPlayer);
            gameLogic.botTurn(gameLogic.currentPlayer);
        }
        String humanCard = "";
        String aliceCard = "";
        String bobCard = "";
        String charlieCard = "";
        for (Card tempCard : gameLogic.getHumanPlayer().getHand()) {
            humanCard += tempCard.getRank() + "_" + tempCard.getSuit() + " ";
        }
        for (Card tempCard : gameLogic.getAlicePlayer().getHand()) {
            aliceCard += tempCard.getRank() + "_" + tempCard.getSuit() + " ";
        }
        for (Card tempCard : gameLogic.getBobPlayer().getHand()) {
            bobCard += tempCard.getRank() + "_" + tempCard.getSuit() + " ";
        }
        for (Card tempCard : gameLogic.getCharliePlayer().getHand()) {
            charlieCard += tempCard.getRank() + "_" + tempCard.getSuit() + " ";
        }
        Log.d("card", "Human " + humanCard);
        Log.d("card", "Alice " + aliceCard);
        Log.d("card", "Bob " + bobCard);
        Log.d("card", "Charlie " + charlieCard);
    }

    /**
     * Update and refresh hand view for human players
     */
    public void updateHumanHandView() {
        // this method also sort humanHand
        Card.RankComparator comparator = new Card().new RankComparator();
        Collections.sort(gameLogic.getHumanPlayer().getHand(), comparator);
        LinearLayout humanHandView = findViewById(R.id.playerHand); // Ensure this ID matches your layout

        humanHandView.removeAllViews();

        for (int i = 0; i < gameLogic.getHumanPlayer().getHand().size(); i++) {
            Card card = gameLogic.getHumanPlayer().getHand().get(i);
            ImageView oldView = cardViewMap.get(card);
            if (oldView.getParent() != null) {
                ViewGroup parent = (ViewGroup) oldView.getParent();
                parent.removeView(oldView);
            }

            ImageView newCardView = new ImageView(MainActivity.this);
            newCardView.setTag(card);
            cardViewMap.put(card, newCardView);
            newCardView.setImageResource(getCardDrawable(card));  // This is a custom method, see below
            // Set the layout params (size, margins, etc.)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
            params.setMargins(-cardWidth / 2, 0, 0, 0);
            newCardView.setLayoutParams(params);
            humanHandView.addView(newCardView);
            newCardView.setX(0);
            newCardView.setY(0);

       }
    }

    /**
     * Update hand view for a bot
     * @param bot the bot that will get UI update and refresh
     */
    public void updateBotHandView(Player bot) {
        ViewGroup tempHandView = handViews.get(bot);
        Card.RankComparator comparator = new Card().new RankComparator();
        if ( bot.getHand().size() > 0 ) {
            Collections.sort(bot.getHand(), comparator);
        }
        tempHandView.removeAllViews();

        for (int i = 0; i < bot.getHand().size(); i++) {
            Card card = bot.getHand().get(i);
            ImageView oldView = cardViewMap.get(card);
            // sometimes, oldView just turns to null
            if (oldView != null && oldView.getParent() != null) {
                ViewGroup parent = (ViewGroup) oldView.getParent();
                parent.removeView(oldView);
            }

            ImageView newCardView = new ImageView(MainActivity.this);
//                    cardViewMap.remove(card);
            newCardView.setTag(card);
            cardViewMap.put(card, newCardView);
            newCardView.setImageResource(R.drawable.card_back);
//            newCardView.setImageResource(getCardDrawable(card));
            LinearLayout.LayoutParams params = null;
            if ( bot == gameLogic.getAlicePlayer() || bot == gameLogic.getCharliePlayer() ) {
                params = new LinearLayout.LayoutParams(cardHeight , cardWidth );
                params.setMargins(0, -cardWidth * 5 / 8 , 0, 0);
                newCardView.setRotation(90);
            } else {
                params = new LinearLayout.LayoutParams(cardWidth ,cardHeight  );
                params.setMargins(- cardWidth / 2, 0  , 0, 0);
            }
            if ( i==0 ) {
                params.setMargins(0, 0  , 0, 0);
            }
            newCardView.setLayoutParams(params);

            tempHandView.addView(newCardView);
            newCardView.setX(0);
            newCardView.setY(0);
        }
    }

    /**
     * A helper method to get image for the card
     * @param card the card that will display an image
     * @return return image for the card
     */
    private int getCardDrawable(Card card) {
        // Convert the card object (rank and suit) to the appropriate drawable resource
        String cardName = "card_" + card.getRank().toLowerCase() + "_of_" + card.getSuit().toLowerCase();
        int drawableResourceId = getResources().getIdentifier(cardName, "drawable", getPackageName());
//        return ContextCompat.getDrawable(this, drawableResourceId);
        return drawableResourceId;
    }


    /**
     * Get bot for spinner so that human can ask
     * @return all bots to ask
     */
    private Player getSelectedBot() {
        String selectedBotName = botSpinner.getSelectedItem().toString();

        switch (selectedBotName) {
            case "Alice":
                return gameLogic.getBotPlayers().get(0);
            case "Bob":
                return gameLogic.getBotPlayers().get(1);
            case "Charlie":
                return gameLogic.getBotPlayers().get(2);
            default:
                return null;
        }
    }

    /**
     * Get ranks for spinner button
     * @return all available rank in the spinner so that human player can select to ask
     */
    private String getSelectedRank() {
        if (rankSpinner == null || rankSpinner.getSelectedItem() == null ) {
            return null;
        }
        return rankSpinner.getSelectedItem().toString();
    }

    /**
     * Update the score board
     * @param humanScore human score
     * @param botScores scores of each bot
     */
    @Override
    public void onScoreUpdate(int humanScore, int[] botScores) {
        // Update the scores for human and each bot
        humanScoreTextView.setText(String.valueOf(humanScore));
        aliceScoreTextView.setText(String.valueOf(botScores[0]));
        bobScoreTextView.setText(String.valueOf(botScores[1]));
        charlieScoreTextView.setText(String.valueOf(botScores[2]));
    }

    @Override
    public void onGameOver(String winnerMessage) {
        // Display the winner message and disable the next button
        statusText.setText(winnerMessage);
        nextButton.setEnabled(false);
    }

    /**
     * Disable all spinner buttons
     */
    public void disableButtons() {
        rankSpinner.setEnabled(false);
        botSpinner.setEnabled(false);
        rankSpinner.setVisibility(View.INVISIBLE);
        botSpinner.setVisibility(View.INVISIBLE);
    }

    /**
     * Enable all spinner buttons
     */
    public void enableButtons() {
        rankSpinner.setEnabled(true);
        botSpinner.setEnabled(true);
        rankSpinner.setVisibility(View.VISIBLE);
        botSpinner.setVisibility(View.VISIBLE);
    }

    /**
     * Test animation
     */
    @SuppressLint("ResourceType")
    @Override
    public void showCardAnimation() {

    }

    /**
     * show animation of drawing card or distributing cards
     * @param player the player that will receive cards
     * @param card a card that is distributed from deck to a player
     */
    @Override
    public void onCardDistributed(Player player, Card card) {
        ImageView cardView = new ImageView(this);
        cardView.setTag(card); // it seems that this method of setting tag to find cardView not workcardView.setImageResource(R.drawable.card_back);
        cardViewMap.put(card, cardView);
        cardView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.card_back));
        if (player == gameLogic.getAlicePlayer() || player == gameLogic.getCharliePlayer()) {
            LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(cardHeight, cardWidth);
            // Apply a negative margin to overlap the cards
            if (player.getHand().size() - 1 > 0) {
                newParams.setMargins(0, -(cardWidth * 3 / 4), 0, 0);
            } else {
                newParams.setMargins(0, 0, 0, 0);
            }
            cardView.setLayoutParams(newParams);
            cardView.setRotation(90);
        } else if (player == gameLogic.getBobPlayer()) {
            LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(cardWidth, cardHeight);
            if (player.getHand().size() - 1 > 0) {
                newParams.setMargins(-(cardWidth * 3 / 4), 0, 0, 0);
            } else {
                newParams.setMargins(0, 0, 0, 0);
            }
            cardView.setLayoutParams(newParams);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
            if (gameLogic.getHumanPlayer().getHand().size() - 1 > 0) {
                params.setMargins(-(cardWidth * 3 / 4), 0, 0, 0);
            } else {
                params.setMargins(0, 0, 0, 0);
            }
            cardView.setLayoutParams(params);
        }
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);  // Root layout of the activity
        ViewGroup handView = handViews.get(player);
        if (handView != null) {
            animateCardToHand(cardView, handView, player.isHuman(), card, player, rootView);
        }
        gameLogic.cardIndex++;
    }

    /**
     * show animation of a card move from deck to hand
     * @param cardView the view of a card
     * @param handView the view of a player hand
     * @param isHumanPlayer true if it is human player
     * @param card the card that is being distributed to player hand
     * @param player the player who will get the card
     * @param rootView the root of relative view of the app
     */
    private void animateCardToHand(final ImageView cardView, ViewGroup handView,
                                   boolean isHumanPlayer, Card card, Player player, ViewGroup rootView) {

        cardView.setX(screenWidthPx / 2); // X of deckLayout
        cardView.setY(screenHeightPx / 2);  // Y of deckLayout
        rootView.addView(cardView);  // Add the card to the root layout

        float destinationX;
        float destinationY;
        // Calculate the final position of the card in the player's hand
        if (player == gameLogic.getAlicePlayer()) {
            destinationX = 30;  // instead of 0, should be 40 or 50
            destinationY = screenHeightPx / 2 - cardWidth;  // middle point
        } else if (player == gameLogic.getCharliePlayer()) {
            destinationX = screenWidthPx - cardHeight;  // screenWidthPx is in pixel while 200 is in dp. Plus. width and height of card
            destinationY = screenHeightPx / 2 - cardWidth;
        } else if (player == gameLogic.getBobPlayer()) {
            destinationX = screenWidthPx / 2 - cardWidth;
            destinationY = 5;
        } else {
//            destinationX = screenWidthPx / 2 - 300 +  handCardIndex * (cardWidth * 3 / 4);  // Adjust for overlap
            destinationX = screenWidthPx / 2 - cardWidth;
            destinationY = screenHeightPx - cardHeight;
        }
        // Create animations to move the card to the hand
        ObjectAnimator moveX = ObjectAnimator.ofFloat(cardView, "x", destinationX);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(cardView, "y", destinationY);

        AnimatorSet moveSet = new AnimatorSet();
        moveSet.playTogether(moveX, moveY);
        moveSet.setDuration(2 * ANIMATION_DURATION);
        moveSet.start();
        moveSet.setStartDelay(gameLogic.cardIndex * ANIMATION_DURATION);

        if (isHumanPlayer) {
            // If it's the human player, apply a flip animation when the card reaches the hand
            moveSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    applyFlipAnimation(cardView, card);
                    rootView.removeView(cardView);
                    handView.addView(cardView);
                    cardView.setX(0);  // Reset X to be inside the handView
                    cardView.setY(0);
                }
            });
        } else {
            // For bots, just add the card without flipping
            moveSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rootView.removeView(cardView);
                    handView.addView(cardView);
                    cardView.setX(0);  // Reset X to be inside the handView
                    cardView.setY(0);
                }
            });
        }

    }

    /**
     *  Method to apply a flip animation to the card when it reaches the human player's hand
     * @param cardView view of a flipping card
     * @param card the card that will be flipped on human hand
     */
    private void applyFlipAnimation(final ImageView cardView, Card card) {
        ObjectAnimator flip1 = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0f);  // Flip to "invisible" side
        flip1.setDuration(250);  // Half of the total flip duration

        flip1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Change the card's image to reveal the front after the first half of the flip
                @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(card.getImageName(), "drawable", getPackageName());
                cardView.setImageResource(resId);
                // Flip back to visible side
                ObjectAnimator flip2 = ObjectAnimator.ofFloat(cardView, "scaleX", 0f, 1f);
                flip2.setDuration(250);  // Second half of the flip duration
                flip2.start();
            }
        });
        flip1.start();  // Start the first half of the flip
    }

    /**
     * show animation of transferring cards from one hand to another hand
     * @param askedPlayer the player that will give cards
     * @param askingPlayer the player who will receive cards
     * @param cardsTrasnfered all cards that will be in transfer
     */
    public void transferCardAnimation(Player askedPlayer, Player askingPlayer, List<Card> cardsTrasnfered) {
        ViewGroup sourceHand = handViews.get(askedPlayer);
        ViewGroup targetHand = handViews.get(askingPlayer);
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        targetHand.removeAllViews();

        AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animations = new ArrayList<>();
        ArrayList<ImageView> cardViews = new ArrayList<ImageView>();
        animatedCardViewList.clear();

        for (int i = 0; i < cardsTrasnfered.size(); i++) {
//            getCardViewFromHand(card, sourceHand);
            Card card = cardsTrasnfered.get(i);
            ImageView cardView = cardViewMap.get(card);
            if (askingPlayer.isHuman()) {
                @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(card.getImageName(), "drawable", getPackageName());
                cardView.setImageResource(resId);
            }
            if (askedPlayer.isHuman()) {
                cardView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.card_back));
            }
            cardView.setRotation(0);
            cardViews.add(cardView);
            if (cardView != null) {
                ImageView animatedCardView = new ImageView(this);
                @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(card.getImageName(), "drawable", getPackageName());
                animatedCardView.setImageResource(resId);
                animatedCardView.setLayoutParams(new LinearLayout.LayoutParams(cardView.getLayoutParams().width, cardView.getLayoutParams().height));

                int[] location = new int[2];
                cardView.getLocationOnScreen(location);
                animatedCardView.setX(location[0]);
                animatedCardView.setY(location[1]);

                sourceHand.removeView(cardView);
                rootView.addView(animatedCardView);
                animatedCardViewList.add(animatedCardView);

                int[] targetPosition = new int[2];
                targetHand.getLocationOnScreen(targetPosition);

                ObjectAnimator moveX = null;
                ObjectAnimator moveY = null;
                float destinationX;
                float destinationY;
                if (askingPlayer.isHuman()) {
                    destinationX = screenWidthPx / 2 - cardWidth + i * cardWidth;
                    destinationY = screenHeightPx - cardHeight;
                    moveX = ObjectAnimator.ofFloat(animatedCardView, "x", destinationX);
                    moveY = ObjectAnimator.ofFloat(animatedCardView, "y", destinationY);
                } else {
                    moveX = ObjectAnimator.ofFloat(animatedCardView, "x", targetPosition[0]);
                    moveY = ObjectAnimator.ofFloat(animatedCardView, "y", targetPosition[1]);
                }

                animations.add(moveX);
                animations.add(moveY);
            }
        }
        animatorSet.playTogether(animations);
        animatorSet.setDuration(2 * ANIMATION_DURATION);  // 1-second animation duration for all cards
        animatorSet.start();

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override

            public void onAnimationEnd(Animator animation) {
                // Remove the card views from the source hand and add them to the target hand
                for (ImageView cardView : cardViews) {
                    if (cardView != null) {
//                        sourceHand.removeView(cardView);
//                        rootView.removeView(cardView);
//                        targetHand.addView(cardView);
//                        cardView.setX(0);
//                        cardView.setY(0);
                    }
                }
                for (int i = 0; i < animatedCardViewList.size(); i++) {
                    rootView.removeView(animatedCardViewList.get(i));
                }
                if (askingPlayer.isHuman()) {
                    updateHumanHandView();
                    updateBotHandView(askedPlayer);
//                    playerHandView.setVisibility(View.INVISIBLE);
                } else if (askedPlayer.isHuman()) {
                    updateHumanHandView();
                    updateBotHandView(askingPlayer);
                } else {
                    updateBotHandView(askedPlayer);
                    updateBotHandView(askingPlayer);
                }
            }

        });

    }

    /**
     * show animation when a player collects a set of 4 cars with same rank. It also updates score board
     * @param scoringPlayer the player who get 1 point
     * @param collectedCards a set of cards with same rank
     */
    public void scorePoint(Player scoringPlayer, ArrayList<Card> collectedCards) {
        ViewGroup scoringHand = handViews.get(scoringPlayer);
//        int[] deckPosition = new int[2];
//        deckLayout.getLocationOnScreen(deckPosition);
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidthPx = displayMetrics.widthPixels;
        int screenHeightPx = displayMetrics.heightPixels;

        AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animations = new ArrayList<>();
        ArrayList<ImageView> cardViews = new ArrayList<>();
        ArrayList<ImageView> animatedCardViewList = new ArrayList<>();

        for (int i = 0; i < collectedCards.size(); i++) {
//            getCardViewFromHand(aCard, scoringHand);
            Card aCard = collectedCards.get(i);
            ImageView tempCardView = cardViewMap.get(aCard);
            cardViews.add(tempCardView);

            ImageView animatedCardView = new ImageView(this);
            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(aCard.getImageName(), "drawable", getPackageName());
            animatedCardView.setImageResource(resId);
            animatedCardView.setLayoutParams(new LinearLayout.LayoutParams(tempCardView.getWidth(), tempCardView.getHeight()));
            if (scoringPlayer == gameLogic.getCharliePlayer() || scoringPlayer == gameLogic.getAlicePlayer()) {
//                animatedCardView.setRotation(90);
            }

            float sourceX;
            float sourceY;
            // Calculate the final position of the card in the player's hand
            if (scoringPlayer == gameLogic.getAlicePlayer()) {
                sourceX = 30 + i * cardWidth;  // instead of 0, should be 40 or 50
                sourceY = screenHeightPx / 2;
//                sourceY = screenHeightPx / 2 -  cardWidth + i * tempCardView.getLayoutParams().width ;  // cardWidth is different from param
//                cardWidth ;
                // tempCardView.getLayoutParams().width
//              tempCardView.getWidth();
            } else if (scoringPlayer == gameLogic.getCharliePlayer()) {
                sourceX = screenWidthPx - cardHeight - i * cardWidth;
                sourceY = screenHeightPx / 2;
//                sourceY = screenHeightPx / 2 -  cardWidth + i * tempCardView.getLayoutParams().width;
            } else if (scoringPlayer == gameLogic.getBobPlayer()) {
                sourceX = screenWidthPx / 2 -  cardWidth + i * cardWidth;
                sourceY = 5;
            } else {
//            destinationX = screenWidthPx / 2 - 300 +  handCardIndex * (cardWidth * 3 / 4);  // Adjust for overlap
                sourceX = screenWidthPx / 2 -  cardWidth + i * cardWidth;
                sourceY = screenHeightPx - 2 * cardHeight;
            }
            animatedCardView.setX(sourceX);
            animatedCardView.setY(sourceY);

            animatedCardViewList.add(animatedCardView);
            rootView.addView(animatedCardView);

            int[] deckPosition = new int[2];
            deckLayout.getLocationOnScreen(deckPosition);

            //  HANDVIEW ALREADY HAVE  CARDVIEW
            ObjectAnimator moveX = null;
            ObjectAnimator moveY = null;
            if (scoringPlayer.isHuman()) {
                moveX = ObjectAnimator.ofFloat(animatedCardView, "x", sourceX);
                moveY = ObjectAnimator.ofFloat(animatedCardView, "y", deckPosition[1]);
            } else if (scoringPlayer == gameLogic.getBobPlayer()) {
                moveX = ObjectAnimator.ofFloat(animatedCardView, "x", sourceX);
                moveY = ObjectAnimator.ofFloat(animatedCardView, "y", deckPosition[1]);
            } else if (scoringPlayer == gameLogic.getAlicePlayer()) {
                moveX = ObjectAnimator.ofFloat(animatedCardView, "x", deckPosition[0] + i * cardWidth);
                moveY = ObjectAnimator.ofFloat(animatedCardView, "y", sourceY);
            } else if (scoringPlayer == gameLogic.getCharliePlayer()) {
                moveX = ObjectAnimator.ofFloat(animatedCardView, "x", deckPosition[0] - i * cardWidth);
                moveY = ObjectAnimator.ofFloat(animatedCardView, "y", sourceY);
            }
            if (moveX != null && moveY != null) {
                animations.add(moveX);
                animations.add(moveY);
            }
        }
        animatorSet.playTogether(animations);
        animatorSet.setDuration(4 * ANIMATION_DURATION);  // Set the duration for all animations
        animatorSet.start();
//        for (ImageView tempCardView : cardViews ) {
//            scoringHand.removeView(tempCardView);
//        }

        // After the animation completes, update the UI
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                for (ImageView animatedCardView : animatedCardViewList) {
                    rootView.removeView(animatedCardView);
                }
                for (ImageView tempCardView : cardViews) {
                    scoringHand.removeView(tempCardView);
                }
            }
        });
    }

    /**
     * Give a status in text to tell human player what actions taken and results of each turn of human and bot
     * @param askingPlayer the player who is asking
     * @param requestSuccess true if a player successfully take cards from another player. Otherwise, false
     * @param rankAsked the player is being asked
     * @param target the card rank that a player is aksing
     * @param numberOfCards number of cards are given from one player to another player
     * @param score true if a set has been collected. Otherwise return false
     */
    public void requestResult(Player askingPlayer, boolean requestSuccess, String rankAsked, Player target,
                              int numberOfCards, boolean score) {
        String message = askingPlayer.getName() + " asked " + target.getName() + " about rank " + rankAsked + ". ";
        if (requestSuccess == false) {
            message += "\n" + target.getName() + " does not have " + rankAsked + ". " + askingPlayer.getName() + " draws a card. Turn is over";
        } else {
            message += "\n" + target.getName() + " has " + numberOfCards + " rank " + rankAsked +  ". " + askingPlayer.getName() + " continues turn.";
        }
        statusText.setText(message);
    }

//    private void playVideo() {
//        // Set the video source from raw resources
//        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.baka;
//        Uri uri = Uri.parse(videoPath);
//        videoView.setVideoURI(uri);
//
//        // Show the VideoView and Next button
//        videoView.setVisibility(View.VISIBLE);
//
//        // Start the video
//        videoView.start();
//
//        // Hide the video and button when the video ends
//        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                stopAndHideVideo();
//            }
//        });
//    }
//
//    private void stopAndHideVideo() {
//        // Stop the video playback and release resources
//        if (videoView.isPlaying()) {
//            videoView.stopPlayback(); // Stops video playback and releases resources
//        }
//        videoView.setVisibility(View.GONE); // Hide the VideoView when not in use
//    }
}

