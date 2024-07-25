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
        setContentView(R.layout.activity_high_scores);

        LinearLayout scoresLayout = findViewById(R.id.layoutScores);

        SharedPreferences prefs = getSharedPreferences("highscores", Context.MODE_PRIVATE);
        List<HighScore> highScores = new ArrayList<>();

        // Load existing scores
        for (int i = 0; i < 5; i++) {
            String player = prefs.getString("player" + i, null);
            int score = prefs.getInt("score" + i, 0);
            if (player != null) {
                highScores.add(new HighScore(score, player));
            }
        }

        // Sort the scores by score descending and by name ascending
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

        // Display scores
        for (int i = 0; i < Math.min(highScores.size(), 5); i++) {
            HighScore highScore = highScores.get(i);
            TextView scoreView = new TextView(this);
            scoreView.setText((i + 1) + ". " + highScore.getName() + ": " + highScore.getScore());
            scoreView.setTextSize(18); // Increase the text size for better visibility
            scoreView.setPadding(0, 10, 0, 10); // Add padding for spacing
            scoresLayout.addView(scoreView);
        }

        // Buttons for navigating


        Button homeButton = findViewById(R.id.btnHome);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HighScoresActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private static class HighScore {
        private final int score;
        private final String name;

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
