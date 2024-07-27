package com.example.sequencegame2024;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    private int score;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private Button saveScoreButton;
    private EditText nameInput;
    private LinearLayout playAgainLayout;
    private LinearLayout scoreInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Initialise the database helper and writable database
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        // Get the score from the intent that started this activity
        score = getIntent().getIntExtra("score", 0);

        // Find the TextView to display the score and set its text
        TextView scoreView = findViewById(R.id.tvScore);
        scoreView.setText("Your Score: " + score);

        // Find the views in the layout
        saveScoreButton = findViewById(R.id.btnSaveScore);
        nameInput = findViewById(R.id.etName);
        playAgainLayout = findViewById(R.id.layoutPlayAgain);
        scoreInputLayout = findViewById(R.id.layoutScoreInput);

        // Check if the score is in the top five and show/hide the input layout
        if (isTopFiveScore(score)) {
            scoreInputLayout.setVisibility(View.VISIBLE);
            // Set up the button to save the high score
            saveScoreButton.setOnClickListener(v -> saveHighScore());
        } else {
            scoreInputLayout.setVisibility(View.GONE);
        }

        // Set up the button to start a new game
        Button playAgainButton = findViewById(R.id.btnPlayAgain);
        playAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close this activity
        });
    }

    private void saveHighScore() {
        // Get the player's name from the input field
        String playerName = nameInput.getText().toString().trim();

        // Check if the name is empty or contains numbers
        if (playerName.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (playerName.matches(".*\\d.*")) {
            Toast.makeText(this, "No numbers allowed in name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert the name and score into the database
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, playerName);
        values.put(DatabaseHelper.COLUMN_SCORE, score);

        db.insert(DatabaseHelper.TABLE_HIGHSCORES, null, values);

        // Start the HighScoresActivity to show the updated high scores
        Intent intent = new Intent(GameOverActivity.this, HighScoresActivity.class);
        startActivity(intent);
        finish(); // Close this activity
    }

    private boolean isTopFiveScore(int score) {
        // Query the database for the top scores
        Cursor cursor = db.query(DatabaseHelper.TABLE_HIGHSCORES, null, null, null, null, null,
                DatabaseHelper.COLUMN_SCORE + " DESC, " + DatabaseHelper.COLUMN_NAME + " ASC");
        boolean isTopFive = false;
        int count = 0;

        // Check if the current score is in the top five
        while (cursor.moveToNext() && count < 5) {
            int highScore = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCORE));
            if (score >= highScore) {
                isTopFive = true;
                break;
            }
            count++;
        }
        cursor.close();
        return isTopFive;
    }
}
