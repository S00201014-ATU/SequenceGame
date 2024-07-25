package com.example.sequencegame2024;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GameOverActivity extends AppCompatActivity {

    // Variable to hold the user's score
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Retrieve the score passed from the previous activity
        score = getIntent().getIntExtra("Score", 0);

        // Set up the TextView to display the user's score
        TextView scoreView = findViewById(R.id.tvScore);
        scoreView.setText("Your Score: " + score);

        // Set up the button to save the score
        Button saveScoreButton = findViewById(R.id.btnSaveScore);
        saveScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to save the high score
                saveHighScore();
            }
        });
    }

    // Method to save the high score
    private void saveHighScore() {
        // Get the player's name from the EditText
        EditText etName = findViewById(R.id.etName);
        String playerName = etName.getText().toString();

        // Retrieve SharedPreferences for storing high scores
        SharedPreferences prefs = getSharedPreferences("High Scores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Create a list to hold the high scores
        List<HighScore> highScores = new ArrayList<>();

        // Load existing high scores from SharedPreferences
        for (int i = 0; i < 5; i++) {
            String player = prefs.getString("Player" + i, null);
            int score = prefs.getInt("Score" + i, 0);
            if (player != null) {
                highScores.add(new HighScore(score, player));
            }
        }

        // Add the new score to the list
        highScores.add(new HighScore(score, playerName));

        // Sort the high scores in descending order, then by name if scores are equal
        Collections.sort(highScores, new Comparator<HighScore>() {
            @Override
            public int compare(HighScore o1, HighScore o2) {
                int scoreCompare = Integer.compare(o2.getScore(), o1.getScore());
                if (scoreCompare == 0) {
                    return o1.getName().compareTo(o2.getName());
                }
                return scoreCompare;
            }
        });

        // Save the top 5 high scores back to SharedPreferences
        for (int i = 0; i < highScores.size() && i < 5; i++) {
            HighScore highScore = highScores.get(i);
            editor.putString("Player" + i, highScore.getName());
            editor.putInt("Score" + i, highScore.getScore());
        }

        // Apply the changes to SharedPreferences
        editor.apply();

        // Start the HighScoresActivity to display the high scores
        Intent intent = new Intent(GameOverActivity.this, HighScoresActivity.class);
        startActivity(intent);
        finish();
    }

    // Inner class to represent a high score
    private class HighScore {
        private int score;
        private String name;

        public HighScore(int score, String name) {
            this.score = score;
            this.name = name;
        }

        public int getScore() {
            return score;
        }

        public String getName() {
            return name;
        }
    }
}
