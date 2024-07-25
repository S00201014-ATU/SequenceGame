package com.example.sequencegame2024;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HighScoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);  // Sets the content view to the layout for this activity

        // Find the LinearLayout where the high scores will be displayed
        LinearLayout scoresLayout = findViewById(R.id.layoutScores);

        // Retrieve the shared preferences for storing high scores
        SharedPreferences prefs = getSharedPreferences("High Scores", Context.MODE_PRIVATE);
        List<HighScore> highScores = new ArrayList<>();

        // Load the top 5 high scores from shared preferences
        for (int i = 0; i < 5; i++) {
            String player = prefs.getString("Player" + i, null);
            int score = prefs.getInt("Score" + i, 0);
            if (player != null) {
                highScores.add(new HighScore(score, player));
            }
        }

        // Sort the high scores in descending order, with ties broken by player name
        Collections.sort(highScores, new Comparator<HighScore>() {
            @Override
            public int compare(HighScore o1, HighScore o2) {
                int scoreCompare = Integer.compare(o2.getScore(), o1.getScore());  // Compare scores in descending order
                if (scoreCompare == 0) {
                    return o1.getName().compareTo(o2.getName());  // If scores are equal, compare names in ascending order
                }
                return scoreCompare;
            }
        });

        // Display the top high scores in the LinearLayout
        for (int i = 0; i < Math.min(highScores.size(), 5); i++) {
            HighScore highScore = highScores.get(i);
            TextView scoreView = new TextView(this);
            scoreView.setText((i + 1) + ". " + highScore.getName() + ": " + highScore.getScore());  // Format: rank. name: score
            scoreView.setTextSize(18);  // Set the text size
            scoreView.setPadding(0, 10, 0, 10);  // Add padding around the text
            scoresLayout.addView(scoreView);  // Add the TextView to the layout
        }

        // Set up the Home button to navigate back to the MainActivity
        Button homeButton = findViewById(R.id.btnHome);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HighScoresActivity.this, MainActivity.class);
            startActivity(intent);  // Start the MainActivity
            finish();  // Finish this activity to remove it from the back stack
        });
    }

    // Static inner class to represent a high score
    private static class HighScore {
        private final int score;  // The score
        private final String name;  // The player's name

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
