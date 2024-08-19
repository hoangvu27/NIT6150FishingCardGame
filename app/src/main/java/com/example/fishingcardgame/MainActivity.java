package com.example.fishingcardgame;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fishingcardgame.Card;
import com.example.fishingcardgame.GameLogic;
import com.example.fishingcardgame.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private GameLogic game;
    private LinearLayout playerHandView;
    private LinearLayout aliceCardsView;
    private LinearLayout bobCardsView;
    private LinearLayout charlieCardsView;
    private Spinner rankSpinner;
    private Spinner botSpinner;
    private Button askButton;
    private TextView statusText;
    private int cardWidth;
    private int cardHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Convert dp to pixels
        cardWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Card.cardWidth, getResources().getDisplayMetrics());
        cardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Card.cardHeight, getResources().getDisplayMetrics());

        game = new GameLogic();

        playerHandView = findViewById(R.id.playerHand);
        aliceCardsView = findViewById(R.id.aliceCards);
        bobCardsView = findViewById(R.id.bobCards);
        charlieCardsView = findViewById(R.id.charlieCards);
        rankSpinner = findViewById(R.id.rankSpinner);
        botSpinner = findViewById(R.id.botSpinner);
        askButton = findViewById(R.id.askButton);
        statusText = findViewById(R.id.statusText);

        updatePlayerHandView();
        updateBotCardsView();
        updateRankSpinner();
        updateBotSpinner();

        askButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rank = rankSpinner.getSelectedItem().toString();
                String botName = botSpinner.getSelectedItem().toString();
                Player botPlayer = null;
                for (Player bot : game.getBotPlayers()) {
                    if (bot.getName().equals(botName)) {
                        botPlayer = bot;
                        break;
                    }
                }

                if (botPlayer != null) {
                    game.playTurn(game.getHumanPlayer(), botPlayer, rank);
                    updatePlayerHandView();
                    updateBotCardsView();
                    botTurn();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid bot selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePlayerHandView() {
        playerHandView.removeAllViews();
        for (int i = 0; i < game.getHumanPlayer().getHand().size(); i++) {
            Card card = game.getHumanPlayer().getHand().get(i);
            ImageView cardView = new ImageView(this);
            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier( card.getImageName() , "drawable", getPackageName());
            cardView.setImageResource(resId);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
            // Apply a negative margin to overlap the cards
            if (i > 0) {
                params.setMargins(-(cardWidth / 2), 0, 0, 0);
            } else {
                params.setMargins(0, 0, 0, 0);
            }
            cardView.setLayoutParams(params);
            playerHandView.addView(cardView);
        }
        updateRankSpinner();
    }

    private void updateBotCardsView() {
        aliceCardsView.removeAllViews();
        bobCardsView.removeAllViews();
        charlieCardsView.removeAllViews();

        for (int i = 0; i < game.getBotPlayers().get(0).getHand().size(); i++) {
            Card card = game.getBotPlayers().get(0).getHand().get(i);
            ImageView cardBackView = new ImageView(this);
            cardBackView.setImageResource(R.drawable.card_back);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardHeight , cardWidth);
            // Apply a negative margin to overlap the cards
            if (i > 0) {
                params.setMargins(0, -(cardHeight / 2), 0, 0);
            } else {
                params.setMargins(0, 0, 0, 0);
            }
            cardBackView.setLayoutParams(params);
            cardBackView.setRotation(90);
            aliceCardsView.addView(cardBackView);
        }

        for (int i = 0; i < game.getBotPlayers().get(1).getHand().size(); i++) {
            ImageView cardBackView = new ImageView(this);
            cardBackView.setImageResource(R.drawable.card_back);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
            // Apply a negative margin to overlap the cards
            if (i > 0) {
                params.setMargins(-(cardWidth / 2), 0, 0, 0);
            } else {
                params.setMargins(0, 0, 0, 0);
            }
            cardBackView.setLayoutParams(params);
            bobCardsView.addView(cardBackView);
        }

        for (int i = 0; i < game.getBotPlayers().get(2).getHand().size(); i++) {
            ImageView cardBackView = new ImageView(this);
            cardBackView.setImageResource(R.drawable.card_back);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardHeight , cardWidth);
            // Apply a negative margin to overlap the cards
            if (i > 0) {
                params.setMargins(0, -(cardHeight / 2), 0, 0);
            } else {
                params.setMargins(0, 0, 0, 0);
            }
            cardBackView.setLayoutParams(params);
            cardBackView.setRotation(90);
            charlieCardsView.addView(cardBackView);
        }
    }

    private void updateRankSpinner() {
        Set<String> ranks = new HashSet<>();
        for (Card card : game.getHumanPlayer().getHand()) {
            ranks.add(card.getRank());
        }
        List<String> rankList = new ArrayList<>(ranks);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rankList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rankSpinner.setAdapter(adapter);
    }

    private void updateBotSpinner() {
        List<String> botNames = new ArrayList<>();
        for (Player bot : game.getBotPlayers()) {
            botNames.add(bot.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, botNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        botSpinner.setAdapter(adapter);
    }

    private void botTurn() {
        Random random = new Random();
        for (Player bot : game.getBotPlayers()) {
            if (bot.getHand().isEmpty()) continue;
            String rank = bot.getHand().get(random.nextInt(bot.getHand().size())).getRank();
            Player target = game.getHumanPlayer();
            if (random.nextBoolean() && !game.getBotPlayers().isEmpty()) {
                target = game.getBotPlayers().get(random.nextInt(game.getBotPlayers().size()));
            }
            game.playTurn(bot, target, rank);
            updatePlayerHandView();
            updateBotCardsView();
            statusText.setText(bot.getName() + " asked " + target.getName() + " for " + rank);
        }
    }
}
