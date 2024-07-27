package com.example.sequencegame2024;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    // Variable to hold the player's score
    private int score;
    // Helper to manage database creation and version management
    private DatabaseHelper dbHelper;
    // Reference to the SQLite database
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Create a new DatabaseHelper instance
        dbHelper = new DatabaseHelper(this);
        // Get a writable database
        db = dbHelper.getWritableDatabase();

        // Get the score from the Intent
        score = getIntent().getIntExtra("score", 0);

        // Find the TextView for displaying the score
        TextView scoreView = findViewById(R.id.tvScore);
        // Set the score text
        scoreView.setText("Your Score: " + score);

        // Find the Button for saving the score
        Button saveScoreButton = findViewById(R.id.btnSaveScore);
        saveScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to save the high score when the button is clicked
                saveHighScore();
            }
        });
    }

    private void saveHighScore() {
        // Find the EditText for entering the player's name
        EditText nameInput = findViewById(R.id.etName);
        // Get the name from the EditText
        String playerName = nameInput.getText().toString().trim();

        // Check if the name is empty
        if (playerName.isEmpty()) {
            // Show a message if the name is empty
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the name contains numbers
        if (playerName.matches(".*\\d.*")) {
            // Show a message if the name contains numbers
            Toast.makeText(this, "No numbers allowed in name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new ContentValues object
        ContentValues values = new ContentValues();
        // Add the player's name to the ContentValues
        values.put(DatabaseHelper.COLUMN_NAME, playerName);
        // Add the score to the ContentValues
        values.put(DatabaseHelper.COLUMN_SCORE, score);

        // Insert the values into the high scores table
        db.insert(DatabaseHelper.TABLE_HIGHSCORES, null, values);

        // Create an Intent to start the HighScoresActivity
        Intent intent = new Intent(GameOverActivity.this, HighScoresActivity.class);
        // Start the HighScoresActivity
        startActivity(intent);
        // Finish the current activity
        finish();
    }
}
