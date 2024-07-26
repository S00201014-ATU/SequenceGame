package com.example.sequencegame2024;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class HighScoresActivity extends AppCompatActivity {

    // Helper to manage database creation and version management
    private DatabaseHelper dbHelper;
    // Reference to the SQLite database
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        // Create a new DatabaseHelper instance
        dbHelper = new DatabaseHelper(this);
        // Get a readable database
        db = dbHelper.getReadableDatabase();

        // Find the LinearLayout for displaying the scores
        LinearLayout scoresLayout = findViewById(R.id.layoutScores);

        // Get the list of high scores
        List<HighScore> highScores = getHighScores();

        // Display the top 5 scores
        for (int i = 0; i < Math.min(highScores.size(), 5); i++) {
            // Get each high score
            HighScore highScore = highScores.get(i);
            // Create a new TextView for each score
            TextView scoreView = new TextView(this);
            // Set the text of the TextView to show the rank, name, and score
            scoreView.setText((i + 1) + ". " + highScore.getName() + ": " + highScore.getScore());
            // Set the text size
            scoreView.setTextSize(18);
            // Set padding for the TextView
            scoreView.setPadding(0, 10, 0, 10);
            // Add the TextView to the LinearLayout
            scoresLayout.addView(scoreView);
        }

        // Find the Button for returning to the home screen
        Button homeButton = findViewById(R.id.btnHome);
        homeButton.setOnClickListener(v -> {
            // Create an Intent to start the MainActivity
            Intent intent = new Intent(HighScoresActivity.this, MainActivity.class);
            // Start the MainActivity
            startActivity(intent);
            // Finish the current activity
            finish();
        });
    }

    // Method to get the list of high scores from the database
    private List<HighScore> getHighScores() {
        List<HighScore> highScores = new ArrayList<>();
        // Query the database for high scores, ordered by score descending and name ascending
        Cursor cursor = db.query(DatabaseHelper.TABLE_HIGHSCORES, null, null, null, null, null, DatabaseHelper.COLUMN_SCORE + " DESC, " + DatabaseHelper.COLUMN_NAME + " ASC");

        // Iterate through the results and add each high score to the list
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            int score = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCORE));
            highScores.add(new HighScore(score, name));
        }
        // Close the cursor to release resources
        cursor.close();
        return highScores;
    }

    // Inner class to represent a high score
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
