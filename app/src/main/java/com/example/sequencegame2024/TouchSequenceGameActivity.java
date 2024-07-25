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

// Activity class for handling the touch sequence game
public class TouchSequenceGameActivity extends AppCompatActivity {

    // Declare buttons and text view for UI components
    private Button btnRed, btnGreen, btnBlue, btnYellow;
    private TextView tvScore;

    // Lists to hold the game sequence and player's sequence
    private List<Integer> gameSequence = new ArrayList<>();
    private List<Integer> playerSequence = new ArrayList<>();

    // Index for keeping track of the current position in the sequence
    private int index = 0;

    // Random number generator for creating game sequences
    private Random random = new Random();

    // Handler for scheduling tasks
    private Handler handler = new Handler();

    // MediaPlayer instances for sound effects
    private MediaPlayer soundLevelComplete;
    private MediaPlayer soundWrongAnswer;
    private MediaPlayer soundGameOver;

    // Variables for scoring
    private int score = 0;
    private int pointsPerRound = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity
        setContentView(R.layout.activity_touch_sequence_game);

        // Initialise buttons and text view
        btnRed = findViewById(R.id.btnRed);
        btnGreen = findViewById(R.id.btnGreen);
        btnBlue = findViewById(R.id.btnBlue);
        btnYellow = findViewById(R.id.btnYellow);
        tvScore = findViewById(R.id.tvScore);

        // Initialise media players for sound effects
        soundLevelComplete = MediaPlayer.create(this, R.raw.level_complete);
        soundWrongAnswer = MediaPlayer.create(this, R.raw.wrong_answer);
        soundGameOver = MediaPlayer.create(this, R.raw.game_over);

        // Set onClick listeners for buttons
        btnRed.setOnClickListener(v -> playerMove(0));
        btnGreen.setOnClickListener(v -> playerMove(1));
        btnBlue.setOnClickListener(v -> playerMove(2));
        btnYellow.setOnClickListener(v -> playerMove(3));

        // Start the first round of the game
        startNewRound();
    }

    // Method to start a new round in the game
    private void startNewRound() {
        // Clear the player's sequence for the new round
        playerSequence.clear();

        // Add two new random colours to the game sequence
        gameSequence.add(random.nextInt(4));
        gameSequence.add(random.nextInt(4));

        // Play the sequence for the player to follow
        playSequence();
    }

    // Method to play the game sequence with visual feedback
    private void playSequence() {
        // Reset the index for tracking the sequence
        index = 0;

        // Schedule tasks to highlight buttons in sequence
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index < gameSequence.size()) {
                    // Highlight the current button in the sequence
                    highlightButton(gameSequence.get(index));

                    // Move to the next button in the sequence
                    index++;

                    // Schedule the next button highlight after 1 second
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    // Method to highlight a button based on its colour
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
            // Temporarily change the button's background colour to white
            btn.setBackgroundColor(Color.WHITE);
            // Change the button's background colour back to its original colour after 500 milliseconds
            handler.postDelayed(() -> {
                if(btnFinal == btnRed) {
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

    // Method to handle the player's move
    private void playerMove(int colour) {
        // Add the player's move to the sequence
        playerSequence.add(colour);

        // Check if the player's sequence is correct so far
        if (playerSequence.size() <= gameSequence.size()) {
            // Check if the most recent move is correct
            if (playerSequence.get(playerSequence.size() - 1).equals(gameSequence.get(playerSequence.size() - 1))) {
                // If the player's sequence matches the game sequence
                if (playerSequence.size() == gameSequence.size()) {
                    // Increase the score and update the display
                    score += pointsPerRound;
                    tvScore.setText("Score: " + score);
                    // Play sound for level completion
                    soundLevelComplete.start();
                    Toast.makeText(TouchSequenceGameActivity.this, "Round complete!", Toast.LENGTH_SHORT).show();
                    // Start a new round after the sound finishes
                    soundLevelComplete.setOnCompletionListener(mp -> startNewRound());
                }
            } else {
                // Play sound for a wrong answer
                soundWrongAnswer.start();
                soundWrongAnswer.setOnCompletionListener(mp -> {
                    // Play game over sound
                    soundGameOver.start();
                    soundGameOver.setOnCompletionListener(mp1 -> {
                        // Start the GameOverActivity and pass the score
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
