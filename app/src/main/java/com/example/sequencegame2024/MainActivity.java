package com.example.sequencegame2024;

// Import necessary Android classes
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

// Main activity class for the application
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the layout defined in activity_main.xml
        setContentView(R.layout.activity_main);

        // Find the 'Quit' button by its ID
        Button btnQuit = findViewById(R.id.btnQuit);
        // Set an OnClickListener to handle button clicks
        btnQuit.setOnClickListener(v -> {
            // Close the activity and all activities in the stack
            finishAffinity();
            // Terminate the app
            System.exit(0);
        });
    }

    // Method to handle the click event for starting the touch game
    public void startRotateGame (View view) {
        // Create an Intent to start the TouchSequenceGameActivity
        Intent intent = new Intent(this, SensorSequenceGameActivity.class);
        // Start the activity defined in the intent
        startActivity(intent);
    }

    // Method to handle the click event for starting the touch game
    public void startTouchGame(View view) {
        // Create an Intent to start the TouchSequenceGameActivity
        Intent intent = new Intent(this, TouchSequenceGameActivity.class);
        // Start the activity defined in the intent
        startActivity(intent);
    }

    // Method to handle the click event for viewing high scores
    public void viewScores(View view) {
        // Create an Intent to start the HighScoresActivity
        Intent intent = new Intent(this, HighScoresActivity.class);
        // Start the activity defined in the intent
        startActivity(intent);
    }
}