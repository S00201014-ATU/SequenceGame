package com.example.sequencegame2024;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    private int score;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private Button goToHighScores;
    private Button playAgainButton;
    private Button playAgainCenterButton;
    private EditText nameInput;

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
        scoreView.setText("Your Score Was " + score);

        // Find the views in the layout
        goToHighScores = findViewById(R.id.btnViewHighScores);
        playAgainButton = findViewById(R.id.btnPlayAgain);
        playAgainCenterButton = findViewById(R.id.btnPlayAgainCenter);
        nameInput = findViewById(R.id.etName);

        // Check if the score is in the top five and show/hide the input layout
        if (isTopFiveScore(score)) {
            nameInput.setVisibility(View.VISIBLE);
            findViewById(R.id.buttonContainer).setVisibility(View.VISIBLE);
            playAgainCenterButton.setVisibility(View.GONE);

            // Set up the button to save the high score
            goToHighScores.setOnClickListener(v -> {
                saveHighScore();
                showHighScores(true); // Pass true to indicate coming from GameOver
            });
        } else {
            nameInput.setVisibility(View.GONE);
            findViewById(R.id.buttonContainer).setVisibility(View.GONE);
            playAgainCenterButton.setVisibility(View.VISIBLE);
        }

        // Set up the button to start a new game
        playAgainButton.setOnClickListener(v -> {
            saveHighScore();
            Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close this activity
        });

        playAgainCenterButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close this activity
        });

        // Add a TextWatcher to validate the name input
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validateNameInput();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Initial validation
        validateNameInput();
    }

    private void validateNameInput() {
        String playerName = nameInput.getText().toString().trim();
        boolean isValid = !playerName.isEmpty() && !playerName.matches(".*\\d.*");
        goToHighScores.setEnabled(isValid);
        playAgainButton.setEnabled(isValid);
    }

    private void saveHighScore() {
        // Get the player's name from the input field
        String playerName = nameInput.getText().toString().trim();

        // Check if the name is empty or contains numbers
        if (playerName.isEmpty() || playerName.matches(".*\\d.*")) {
            return;
        }

        // Insert the name and score into the database
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, playerName);
        values.put(DatabaseHelper.COLUMN_SCORE, score);

        db.insert(DatabaseHelper.TABLE_HIGHSCORES, null, values);
    }

    private void showHighScores(boolean fromGameOver) {
        // Start the HighScoresActivity to show the updated high scores
        Intent intent = new Intent(GameOverActivity.this, HighScoresActivity.class);
        intent.putExtra("FROM_GAME_OVER", fromGameOver);
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
