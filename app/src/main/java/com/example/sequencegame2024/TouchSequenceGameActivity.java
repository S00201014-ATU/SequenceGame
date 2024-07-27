package com.example.sequencegame2024;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TouchSequenceGameActivity extends AppCompatActivity {

    private Button btnRed, btnGreen, btnBlue, btnYellow;
    private TextView tvScore;

    private List<Integer> gameSequence = new ArrayList<>();
    private List<Integer> playerSequence = new ArrayList<>();

    private int index = 0;

    private Random random = new Random();

    private Handler handler = new Handler();

    private MediaPlayer soundLevelComplete;
    private MediaPlayer soundWrongAnswer;
    private MediaPlayer soundGameOver;

    private int score = 0;
    private int initialPoints = 4;
    private int pointsPerRound = 2;
    private int roundNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for the screen
        setContentView(R.layout.activity_touch_sequence_game);

        // Find buttons and text view in the layout
        btnRed = findViewById(R.id.btnRed);
        btnGreen = findViewById(R.id.btnGreen);
        btnBlue = findViewById(R.id.btnBlue);
        btnYellow = findViewById(R.id.btnYellow);
        tvScore = findViewById(R.id.tvScore);

        // Set up the sounds
        soundLevelComplete = MediaPlayer.create(this, R.raw.level_complete);
        soundWrongAnswer = MediaPlayer.create(this, R.raw.wrong_answer);
        soundGameOver = MediaPlayer.create(this, R.raw.game_over);

        // Define what happens when each button is clicked
        btnRed.setOnClickListener(v -> playerMove(0));
        btnGreen.setOnClickListener(v -> playerMove(1));
        btnBlue.setOnClickListener(v -> playerMove(2));
        btnYellow.setOnClickListener(v -> playerMove(3));

        // Start the first round of the game
        startNewRound();
    }

    // Starts a new round of the game
    private void startNewRound() {
        // Clear the player's sequence for the new round
        playerSequence.clear();

        // If it's the first round, add four random colours to the game sequence
        if (roundNumber == 0) {
            for (int i = 0; i < 4; i++) {
                gameSequence.add(random.nextInt(4));
            }
        } else {
            // For later rounds, add two new random colours
            for (int i = 0; i < 2; i++) {
                gameSequence.add(random.nextInt(4));
            }
        }

        // Increase the round number
        roundNumber++;

        // Show the sequence of buttons to the player
        playSequence();
    }

    // Plays the sequence of button presses to the player
    private void playSequence() {
        index = 0;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if there are more buttons to show
                if (index < gameSequence.size()) {
                    // Highlight the current button in the sequence
                    highlightButton(gameSequence.get(index));

                    // Move to the next button in the sequence
                    index++;

                    // Show the next button after 1 second
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    // Highlights a button by changing its colour
    private void highlightButton(int colour) {
        Button btn = null;
        switch (colour) {
            case 0:
                btn = btnRed;
                break;
            case 1:
                btn = btnGreen;
                break;
            case 2:
                btn = btnBlue;
                break;
            case 3:
                btn = btnYellow;
                break;
        }

        if (btn != null) {
            final Button btnFinal = btn;
            // Temporarily change the button's colour to white
            btn.setBackgroundColor(Color.WHITE);
            // Change it back to its original colour after 500 milliseconds
            handler.postDelayed(() -> {
                if (btnFinal == btnRed) {
                    btnFinal.setBackgroundColor(Color.RED);
                } else if (btnFinal == btnGreen) {
                    btnFinal.setBackgroundColor(Color.GREEN);
                } else if (btnFinal == btnBlue) {
                    btnFinal.setBackgroundColor(Color.BLUE);
                } else if (btnFinal == btnYellow) {
                    btnFinal.setBackgroundColor(Color.YELLOW);
                }
            }, 500);
        }
    }

    // Handles a player's move when a button is clicked
    private void playerMove(int colour) {
        // Add the player's move to their sequence
        playerSequence.add(colour);

        // Check if the player's sequence matches the game's sequence so far
        if (playerSequence.size() <= gameSequence.size()) {
            // Check if the most recent move is correct
            if (playerSequence.get(playerSequence.size() - 1).equals(gameSequence.get(playerSequence.size() - 1))) {
                // If the player's sequence matches the game's sequence completely
                if (playerSequence.size() == gameSequence.size()) {
                    // Determine points for this round
                    int pointsForRound = (roundNumber == 1) ? initialPoints : pointsPerRound;

                    // Update the score and display it
                    score += pointsForRound;
                    tvScore.setText("Score: " + score);

                    // Play sound to indicate the level is complete
                    soundLevelComplete.start();
                    Toast.makeText(TouchSequenceGameActivity.this, "Round complete!", Toast.LENGTH_SHORT).show();

                    // Start a new round after the sound is done
                    soundLevelComplete.setOnCompletionListener(mp -> startNewRound());
                }
            } else {
                // Play sound for a wrong move
                soundWrongAnswer.start();
                soundWrongAnswer.setOnCompletionListener(mp -> {
                    // Play sound for game over
                    soundGameOver.start();
                    soundGameOver.setOnCompletionListener(mp1 -> {
                        // Show the game over screen with the final score
                        Intent intent = new Intent(TouchSequenceGameActivity.this, GameOverActivity.class);
                        intent.putExtra("score", score);
                        startActivity(intent);
                        finish();
                    });
                });
            }
        }
    }
}
