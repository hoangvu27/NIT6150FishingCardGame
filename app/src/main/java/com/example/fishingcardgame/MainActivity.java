package com.example.fishingcardgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GameLogic.GameListener {

    private GameLogic gameLogic;
    //    private LinearLayout playerHandView;
//    private LinearLayout aliceCardsView;
//    private LinearLayout bobCardsView;
//    private LinearLayout charlieCardsView;
    private ViewGroup playerHandView, aliceCardsView, bobCardsView, charlieCardsView;
    private Spinner rankSpinner;
    private Spinner botSpinner;
    private Button nextButton;
    private TextView statusText;
    private int cardWidth;
    private int cardHeight;
    private TextView humanScoreTextView, aliceScoreTextView, bobScoreTextView, charlieScoreTextView;
    private boolean isHumanTurn = false;
    private Player currentPlayer;

    // Fields to track the bot's action
    private Player askingBot;
    private Player targetBot;
    private String requestedRank;
    private boolean waitingForBotActionResult = false;
    private ViewGroup deckLayout;
    private Map<Player, ViewGroup> handViews;
//    private int cardIndex;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Convert dp to pixels
        cardWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Card.cardWidth, getResources().getDisplayMetrics());
        cardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Card.cardHeight, getResources().getDisplayMetrics());


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

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonClicked();
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
        ArrayAdapter<String> botAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, botNames);
        botAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        botSpinner.setAdapter(botAdapter);
        gameLogic.startGame();


    }

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
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, gameLogic.getHumanPlayer().getValidRanks());
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        rankSpinner.setAdapter(adapter);
                    }
                }, 2000);


            }
        });
    }

    private void onNextButtonClicked() {
        if (gameLogic.isGameOver()) {
//         notify UI
//            clear UI
//            quit button to log out game  !!!
//            ADD  QUIT  BUTTON  ON RIGHT CORNER
            nextButton.setEnabled(false);
            gameLogic.determineWinner();
        }
        if (gameLogic.isRoundOver() && gameLogic.isStartNextRound() == false) {
//          ALl handView should be empty, but clear all UI if necessary
            statusText.setText("Round is over. Click next to start next round");
            gameLogic.setStartNextRound(true);
            return;
        }
        if (gameLogic.isStartNextRound()) {
            gameLogic.setupRound();
            gameLogic.playRound();
            Log.d("tag", "this is next round");
            return;
        }
        if (gameLogic.currentPlayer.getHand().size() == 0) {
            statusText.setText("Player has no card. Turn skipped");
            gameLogic.setNextPlayer(gameLogic.currentPlayer);
            return;
        }

        if (gameLogic.currentPlayer.isHuman()) {
//            if (currentPlayer.getHand().size() == 0) { };
            updateHumanHandView();
            Player targetBot = getSelectedBot();
            String selectedRank = getSelectedRank();

            if (targetBot != null && selectedRank != null) {
                // Notify the game logic to process the human's turn
                gameLogic.humanTurn(targetBot, selectedRank);
            } else {
                statusText.setText("Please select a bot and a rank.");
            }
        } else {
            // Let the game logic handle the bot's turn
            updateBotHandView(gameLogic.currentPlayer);
            gameLogic.botTurn(gameLogic.currentPlayer);
        }
        String humanCard ="";
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

    public void updateHumanHandView() {
        // this method also sort humanHand
        Card.RankComparator comparator = new Card().new RankComparator();
        Collections.sort(gameLogic.getHumanPlayer().getHand(), comparator);
        LinearLayout humanHandView = findViewById(R.id.playerHand); // Ensure this ID matches your layout

        // Clear the current hand view before updating
        humanHandView.removeAllViews();

        // Loop through the human's hand and add each card as an ImageView
        for (Card card : gameLogic.getHumanPlayer().getHand()) {
            ImageView cardView = new ImageView(this);

            // Set the appropriate drawable based on the card's rank and suit
            cardView.setImageResource(getCardDrawable(card));  // This is a custom method, see below

            // Set the layout params (size, margins, etc.)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
            params.setMargins(-cardWidth *3 / 4, 0, 0, 0); // Overlap cards slightly, adjust as needed
            // set (0,0,0,0) ???
            cardView.setLayoutParams(params);

            humanHandView.addView(cardView);
            //  THERE  MIGHT BE DELAY SO THAT CARD APPEAR AFTER TRANSFER ANIMATION
        }
    }

    public void updateBotHandView(Player bot) {
        // Find the human player's hand view (LinearLayout or whichever container you're using)
        ViewGroup tempHandView = handViews.get(bot);
        Card.RankComparator comparator = new Card().new RankComparator();
        Collections.sort(bot.getHand(), comparator);

        // Clear the current hand view before updating
        tempHandView.removeAllViews();

        // Loop through the human's hand and add each card as an ImageView
        for (Card card : bot.getHand()) {
            ImageView cardView = new ImageView(this);
            cardView.setImageResource(R.drawable.card_back);

            if (bot == gameLogic.getBobPlayer()) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
                params.setMargins(-cardWidth * 3 / 4, 0, 0, 0); // Overlap cards slightly, adjust as needed
                // set (0,0,0,0) ???
                cardView.setLayoutParams(params);
            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardHeight, cardWidth);
                params.setMargins(0, -cardWidth * 3 / 4, 0, 0); // Overlap cards slightly, adjust as needed
                // set (0,0,0,0) ???
                cardView.setLayoutParams(params);
                cardView.setRotation(90);
            }

            // Add the card view to the human hand view
            tempHandView.addView(cardView);
        }
    }

    private int getCardDrawable(Card card) {
        // Convert the card object (rank and suit) to the appropriate drawable resource
        String cardName = "card_" + card.getRank().toLowerCase() + "_of_" + card.getSuit().toLowerCase();
        int drawableResourceId = getResources().getIdentifier(cardName, "drawable", getPackageName());
//        return ContextCompat.getDrawable(this, drawableResourceId);
        return drawableResourceId;
    }


    // Helper method to get the selected bot from the Spinner
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

    private String getSelectedRank() {
        return rankSpinner.getSelectedItem().toString();
    }


    @Override
    public void onScoreUpdate(int humanScore, int[] botScores) {
        // Update the scores for human and each bot
        humanScoreTextView.setText(String.valueOf(humanScore));
        aliceScoreTextView.setText(String.valueOf(botScores[0]));
        bobScoreTextView.setText(String.valueOf(botScores[1]));
        charlieScoreTextView.setText(String.valueOf(botScores[2]));
    }

    @Override
    public void onBotAsk(Player bot, Player target, String rankAsked) {
        // Display the bot's action in the bot actions text view
        String actionText = bot.getName() + " asks " + target.getName() + " for rank " + rankAsked + ".";
        statusText.setText(actionText);
    }

    @Override
    public void onGameOver(String winnerMessage) {
        // Display the winner message and disable the next button
        statusText.setText(winnerMessage);
        nextButton.setEnabled(false);
    }

    @Override
    public void playerEmpty() {
        statusText.setText("Player has no card left. Turn skipped");
    }

    public void disableButtons() {
        rankSpinner.setEnabled(false);
        botSpinner.setEnabled(false);
    }

    public void enableButtons() {
        rankSpinner.setEnabled(true);
        botSpinner.setEnabled(true);
    }

    @SuppressLint("ResourceType")
    @Override
    public void showCardAnimation() {
        @SuppressLint("WrongViewCast") FrameLayout rootView = findViewById(R.id.rootLayout);
        ViewOverlay overlay = rootView.getOverlay();
        AnimatorSet animatorSet = new AnimatorSet();

        ImageView cardView = new ImageView(this);
//        cardView.setTag(card);
        cardView.setImageResource(R.drawable.card_back);
        LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(cardHeight, cardWidth);
        cardView.setLayoutParams(newParams);
        cardView.setX(80);
        cardView.setY(80);
        int[] sourcePosition = new int[2];
        cardView.getLocationOnScreen(sourcePosition);

        ImageView animatedCardView = new ImageView(this);
        animatedCardView.setImageResource(R.drawable.card_back);
        animatedCardView.setLayoutParams(new FrameLayout.LayoutParams(cardView.getWidth(), cardView.getHeight()));

        animatedCardView.setX(sourcePosition[0]);
        animatedCardView.setY(sourcePosition[1]);

//        overlay.add(animatedCardView);

//        int[] targetPosition = new int[2];
//        playerHandView.getLocationOnScreen(targetPosition);
        ObjectAnimator moveX = ObjectAnimator.ofFloat(animatedCardView, "x", 200);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(animatedCardView, "y", 200);

        animatorSet.playTogether(moveX, moveY);
        animatorSet.setDuration(1000);  // Move animation duration
        animatorSet.start();
    }

    @Override
    public void onCardDistributed(Player player, Card card) {
//        Log.d("tag", card.getRank() + " _ " + card.getSuit() + " onCardDistributed method");
        ImageView cardView = new ImageView(this);
        cardView.setTag(card);
        cardView.setImageResource(R.drawable.card_back);
//        cardView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.card_back));
        if (player == gameLogic.getAlicePlayer() || player == gameLogic.getCharliePlayer()) {
            LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(cardHeight, cardWidth);
            // Apply a negative margin to overlap the cards
            if (player.getHand().size() - 1 > 0) {
                newParams.setMargins(0, -(cardWidth * 3 /4), 0, 0);
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

    private void animateCardToHand(final ImageView cardView, ViewGroup handView,
                                   boolean isHumanPlayer, Card card, Player player, ViewGroup rootView) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidthPx = displayMetrics.widthPixels;
        int screenHeightPx = displayMetrics.heightPixels;

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
            destinationY = 30;
        } else {
//            destinationX = screenWidthPx / 2 - 300 +  handCardIndex * (cardWidth * 3 / 4);  // Adjust for overlap
            destinationX = screenWidthPx / 2 - cardWidth;
            destinationY = screenHeightPx - 2 * cardHeight;
        }
        // Create animations to move the card to the hand
        ObjectAnimator moveX = ObjectAnimator.ofFloat(cardView, "x", destinationX);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(cardView, "y", destinationY);

        AnimatorSet moveSet = new AnimatorSet();
        moveSet.playTogether(moveX, moveY);
        moveSet.setDuration(1000);
        moveSet.start();
        moveSet.setStartDelay(gameLogic.cardIndex * 500);


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

    // Method to apply a flip animation to the card when it reaches the human player's hand
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

    public ImageView getCardViewFromHand(Card card, ViewGroup handView) {
        for (int i = 0; i < handView.getChildCount(); i++) {
            ImageView cardView = (ImageView) handView.getChildAt(i);
            // Assuming cardView has a tag or a method to compare with the Card object
            Card associatedCard = (Card) cardView.getTag();  // Assuming you set the card as a tag earlier
            if (associatedCard != null && associatedCard.equals(card)) {
                return cardView;
            }
        }
        return null;  // If no match is found
    }


    public void transferCardAnimation(Player askedPlayer, Player askingPlayer, List<Card> cardsTrasnfered) {
        ViewGroup sourceHand = handViews.get(askedPlayer);
        ViewGroup targetHand = handViews.get(askingPlayer);
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);

        AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animations = new ArrayList<>();
        ArrayList<ImageView> cardViews = new ArrayList<ImageView>();
        ArrayList<ImageView> animatedCardViewList = new ArrayList<ImageView>();

        for (Card card : cardsTrasnfered) {
            ImageView cardView = getCardViewFromHand(card, sourceHand);  // Using the map approach
            if (cardView != null) {
                animatedCardViewList.add(cardView);

                ImageView animatedCardView = new ImageView(this);
                @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(card.getImageName(), "drawable", getPackageName());
                animatedCardView.setImageResource(resId);

                animatedCardView.setLayoutParams(new LinearLayout.LayoutParams(cardView.getLayoutParams().width, cardView.getLayoutParams().height));

                int[] location = new int[2];
                cardView.getLocationOnScreen(location);
                animatedCardView.setX(location[0]);
                animatedCardView.setY(location[1]);

                sourceHand.removeView((cardView));
                animatedCardViewList.add(animatedCardView);
                rootView.addView(animatedCardView);

//                rootView.addView(cardView);


//                Log.d("pos", cardView.getX() + "_" + cardView.getY());
//                Log.d("pos", "root: " + rootView.getX() + "_" + rootView.getY());

//                targetHand.getChildAt(targetHand.getChildCount() - 1);  // ALTHOUGH TARGET HANDS RECEIVED CARDS, TARGET VIEW NOT YET GET CARDS
                // ASSUMING THAT targetHand.getChildAt WILL WORK

                //  2 PROBLEMS WITH BELOW CODE:
                // FIRST, SAME TARGET LOCATIONS FOR ALL CARDS  ->  this can be solved if updateBotHand(...) is used
                // LOCATION IS TARGET HAND, NOT THE LAST CARDS OF HAND

                //  ==> EASIEST SOLUTION IS TO CLONE CARDS FOR ANIMATION  & updateBotHand
                //  updateBotHand & updateHumanHand can turn card face to up or down
                int[] targetPosition = new int[2];
                targetHand.getChildAt(targetHand.getChildCount()-1).getLocationOnScreen(targetPosition);

                // Create animations to move the card from source hand to target hand
//                ObjectAnimator moveX = ObjectAnimator.ofFloat(cardView, "x", targetPosition[0]);
//                ObjectAnimator moveY = ObjectAnimator.ofFloat(cardView, "y", targetPosition[1]);

                ObjectAnimator moveX = ObjectAnimator.ofFloat(animatedCardView, "x", targetPosition[0]);
                ObjectAnimator moveY = ObjectAnimator.ofFloat(animatedCardView, "y", targetPosition[1]);

                animations.add(moveX);
                animations.add(moveY);
            }
        }
        animatorSet.playTogether(animations);
        animatorSet.setDuration(1000);  // 1-second animation duration for all cards
        animatorSet.start();

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Remove the card views from the source hand and add them to the target hand
                for (ImageView cardView : animatedCardViewList) {
                    if (cardView != null) {
//                        rootView.removeView(cardView);
//                        sourceHand.removeView((cardView));
//                        targetHand.addView(cardView);
//                        cardView.setX(0);
//                        cardView.setY(0); // is this necessary ???
                    }
                }
                for (int i = 0; i < animatedCardViewList.size(); i++) {
                    rootView.removeView(animatedCardViewList.get(i));
                }
                if (askingPlayer.isHuman()) {
                    updateHumanHandView();
                    updateBotHandView(askedPlayer);
                } else if (askedPlayer.isHuman()) {
                    updateHumanHandView();
                    updateBotHandView(askingPlayer);
                } else {
                    updateBotHandView(askedPlayer);
                    updateBotHandView(askingPlayer);
                }

            }
        });

        //  EACH CARD WILL BE TRANSFERED SEPARATELY, NOT AT THE SAME TIME
        //  IF HUMAN GIVE CARD, THEN FLIP CARD
        // IF HUMAN RECEIVE CARD, THEN FLIP CARD

        // NOT SURE HOW ANIMATION ENDS
        // -> 2 WAYS: CLONE CARDS FOR ANIMATION & UPDATE HANDS ,
        // OR MOVE CARDS FROM A HAND TO ANOTHER HAND

        // Clone the card view to create a movable image for the animation

    }

    public void scorePoint(Player scoringPlayer, ArrayList<Card> collectedCards) {
        ViewGroup scoringHand = handViews.get(scoringPlayer);
//        int[] deckPosition = new int[2];
//        deckLayout.getLocationOnScreen(deckPosition);
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);

        AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animations = new ArrayList<>();
        ArrayList<ImageView> cardViews = new ArrayList<>();

        for (Card aCard : collectedCards) {
            ImageView tempCardView = getCardViewFromHand(aCard, scoringHand);
            cardViews.add(tempCardView);

            ImageView animatedCardView = new ImageView(this);
            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(aCard.getImageName(), "drawable", getPackageName());
            animatedCardView.setImageResource(resId);
            animatedCardView.setLayoutParams(new LinearLayout.LayoutParams(tempCardView.getLayoutParams().width, tempCardView.getLayoutParams().height));
            if (scoringPlayer == gameLogic.getCharliePlayer() || scoringPlayer == gameLogic.getAlicePlayer()) {
                animatedCardView.setRotation(90);
            }

            int[] cardPosition = new int[2];
            tempCardView.getLocationOnScreen(cardPosition);
            animatedCardView.setX(cardPosition[0]);
            animatedCardView.setY(cardPosition[1]);

            scoringHand.removeView(tempCardView);
            rootView.addView(animatedCardView);

            //  HANDVIEW ALREADY HAVE  CARDVIEW
            ObjectAnimator moveX = null;
            ObjectAnimator moveY = null;
            if (scoringPlayer.isHuman()) {
                moveX = ObjectAnimator.ofFloat(animatedCardView, "x", cardPosition[0]);
                moveY = ObjectAnimator.ofFloat(animatedCardView, "y", cardPosition[1] - 125);
            } else if (scoringPlayer == gameLogic.getBobPlayer()) {
                moveX = ObjectAnimator.ofFloat(animatedCardView, "x", cardPosition[0]);
                moveY = ObjectAnimator.ofFloat(animatedCardView, "y", cardPosition[1] + 125);
            } else if (scoringPlayer == gameLogic.getAlicePlayer()) {
                moveX = ObjectAnimator.ofFloat(animatedCardView, "x", cardPosition[0] + 125);
                moveY = ObjectAnimator.ofFloat(animatedCardView, "y", cardPosition[1]);
            } else if (scoringPlayer == gameLogic.getCharliePlayer()) {
                moveX = ObjectAnimator.ofFloat(animatedCardView, "x", cardPosition[0] - 125);
                moveY = ObjectAnimator.ofFloat(animatedCardView, "y", cardPosition[1]);
            }
            if (moveX != null && moveY != null) {
                animations.add(moveX);
                animations.add(moveY);
            }
        }
        animatorSet.playTogether(animations);
        animatorSet.setDuration(1000);  // Set the duration for all animations
        animatorSet.start();

        // After the animation completes, update the UI
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Remove the cards from the player's hand and add them to the deck view
                for (ImageView cardView : cardViews) {
//                      // Remove from player's hand
                    //  ASSUMING THAT CARDS DISAPPEAR AT THE END OF ANIMATION
                }
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, 1000);  // 1 second delay, adjust as needed
    }

    public void requestResult(Player askingPlayer, boolean requestSuccess, String rankAsked, Player target, int numberOfCards) {
        String message = askingPlayer.getName() + " asked " + target.getName() + " about rank " + rankAsked + "\n";
        if (requestSuccess == false) {
            message += target.getName() + " does not have " + rankAsked + "\n" + askingPlayer.getName() + " turn is over";
        } else {
            message += target.getName() + " has " + numberOfCards + " rank " + rankAsked + "\n" + askingPlayer.getName() + " continues";
        }
        statusText.setText(message);
    }

    @Override
    public void onTurnEnd() {
        // Clear the bot actions text view to indicate the end of the turn
        statusText.setText("");
    }
}

