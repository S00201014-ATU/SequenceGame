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
import java.util.Map;

public class GameOverActivity extends AppCompatActivity {

    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Get the score from the intent
        score = getIntent().getIntExtra("score", 0);

        TextView scoreView = findViewById(R.id.tvScore);
        scoreView.setText("Your Score: " + score);

        Button saveScoreButton = findViewById(R.id.btnSaveScore);
        saveScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHighScore();
            }
        });
    }

    private void saveHighScore() {
        EditText nameInput = findViewById(R.id.etName);
        String playerName = nameInput.getText().toString();

        SharedPreferences prefs = getSharedPreferences("highscores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        List<HighScore> highScores = new ArrayList<>();

        // Load existing scores
        for (int i = 0; i < 5; i++) {
            String player = prefs.getString("player" + i, null);
            int score = prefs.getInt("score" + i, 0);
            if (player != null) {
                highScores.add(new HighScore(score, player));
            }
        }

        // Add new score
        highScores.add(new HighScore(score, playerName));

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

        // Save top 5 scores
        for (int i = 0; i < highScores.size() && i < 5; i++) {
            HighScore highScore = highScores.get(i);
            editor.putString("player" + i, highScore.getName());
            editor.putInt("score" + i, highScore.getScore());
        }

        editor.apply();

        // Navigate to HighScoresActivity
        Intent intent = new Intent(GameOverActivity.this, HighScoresActivity.class);
        startActivity(intent);
        finish();
    }

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
