package com.example.sequencegame2024;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SensorSequenceGameActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private List<Integer> gameSequence = new ArrayList<>(); // List to store the correct sequence of moves
    private List<Integer> playerSequence = new ArrayList<>(); // List to store the player's moves
    private int index = 0; // Keeps track of current position in the sequence
    private int score = 0; // Player's score
    private Random random = new Random(); // Used to generate random numbers
    private Handler handler = new Handler(); // Used to handle timed tasks
    private MediaPlayer soundLevelComplete; // Sound played when a level is completed
    private MediaPlayer soundWrongAnswer; // Sound played when the player makes a wrong move
    private MediaPlayer soundGameOver; // Sound played when the game is over

    private TextView tvScore; // TextView to show the score
    private ImageView circleRed, circleYellow, circleBlue, circleGreen; // Circles representing different directions
    private boolean userTurn = false; // Indicates if it is the player's turn
    private boolean firstRound = true; // Checks if it's the first round
    private boolean initialCoordinatesSet = false; // Checks if initial sensor coordinates are set

    private float[] initialCoordinates = new float[3]; // Stores initial sensor coordinates
    private SensorEvent lastSensorEvent; // Stores the last sensor event
    private long lastLogTime = 0; // Time of the last log
    private long lastDirectionTime = 0; // Time of the last direction detection
    private static final long DIRECTION_DEBOUNCE_TIME = 500; // Time to debounce direction changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_sequence_game);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        tvScore = findViewById(R.id.tvScore);
        circleRed = findViewById(R.id.redCircle);
        circleYellow = findViewById(R.id.yellowCircle);
        circleBlue = findViewById(R.id.blueCircle);
        circleGreen = findViewById(R.id.greenCircle);

        soundLevelComplete = MediaPlayer.create(this, R.raw.level_complete);
        soundWrongAnswer = MediaPlayer.create(this, R.raw.wrong_answer);
        soundGameOver = MediaPlayer.create(this, R.raw.game_over);

        startNewRound(); // Start the first round of the game
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL); // Register the sensor listener
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); // Unregister the sensor listener
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (userTurn && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lastSensorEvent = event;
            handleAccelerometer(event.values); // Handle accelerometer data
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed
    }

    private void handleAccelerometer(float[] values) {
        float x = values[0];
        float y = values[1];
        float z = values[2];

        Integer direction = null;
        float movementThreshold = 2.0f; // Threshold for detecting significant movement
        float returnThreshold = 1.0f; // Threshold for detecting return to initial position

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime > 1000) { // Log sensor data every second
            Log.d("SensorSequenceGame", "Accelerometer readings: x=" + x + ", y=" + y + ", z=" + z);
            lastLogTime = currentTime;
        }

        // Determine direction based on movement
        if (Math.abs(x - initialCoordinates[0]) > movementThreshold) {
            direction = x > initialCoordinates[0] ? 3 : 2; // Right or Left
        } else if (Math.abs(y - initialCoordinates[1]) > movementThreshold) {
            direction = y > initialCoordinates[1] ? 1 : 0; // Down or Up
        }

        Log.d("SensorSequenceGame", "Detected direction: " + direction);

        // Check if the direction is stable (debounce)
        if (direction != null && currentTime - lastDirectionTime > DIRECTION_DEBOUNCE_TIME) {
            lastDirectionTime = currentTime;
            highlightDirection(direction); // Highlight the direction
            handleDirectionInput(direction); // Handle player's input
        }
    }

    private void handleDirectionInput(int direction) {
        playerMove(direction); // Add the move to the player's sequence
    }

    private void playerMove(int color) {
        playerSequence.add(color);
        Log.d("SensorSequenceGame", "Player sequence: " + playerSequence);

        if (playerSequence.size() <= gameSequence.size()) {
            if (playerSequence.get(playerSequence.size() - 1).equals(gameSequence.get(playerSequence.size() - 1))) {
                if (playerSequence.size() == gameSequence.size()) {
                    if (firstRound) {
                        score += 4;
                        firstRound = false;
                    } else {
                        score += 2;
                    }
                    tvScore.setText("Score: " + score); // Update the score
                    sensorManager.unregisterListener(this); // Stop listening to the sensor
                    soundLevelComplete.start(); // Play level complete sound
                    Toast.makeText(this, "Round complete!", Toast.LENGTH_SHORT).show();
                    soundLevelComplete.setOnCompletionListener(mp -> {
                        sensorManager.registerListener(SensorSequenceGameActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL); // Resume sensor listening
                        startNewRound(); // Start the next round
                    });
                }
            } else {
                sensorManager.unregisterListener(this); // Stop listening to the sensor
                soundWrongAnswer.start(); // Play wrong answer sound
                soundWrongAnswer.setOnCompletionListener(mp -> {
                    soundGameOver.start(); // Play game over sound
                    soundGameOver.setOnCompletionListener(mp1 -> {
                        Intent intent = new Intent(this, GameOverActivity.class); // Start Game Over activity
                        intent.putExtra("score", score); // Pass the score
                        startActivity(intent);
                        finish();
                    });
                });
            }
        }
    }

    private void startNewRound() {
        playerSequence.clear(); // Clear the player's sequence

        // Generate a new sequence
        if (gameSequence.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                gameSequence.add(random.nextInt(4));
            }
        } else {
            for (int i = 0; i < 2; i++) {
                gameSequence.add(random.nextInt(4));
            }
        }

        Log.d("SensorSequenceGame", "Game sequence: " + gameSequence);

        if (!initialCoordinatesSet) {
            resetInitialCoordinates(); // Set initial sensor coordinates
            initialCoordinatesSet = true;
        }
        playSequence(); // Play the sequence for the player to follow
    }

    private void playSequence() {
        userTurn = false; // It's not the player's turn yet
        sensorManager.unregisterListener(this); // Stop listening to the sensor
        index = 0;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index < gameSequence.size()) {
                    highlightDirection(gameSequence.get(index)); // Highlight the direction
                    index++;
                    handler.postDelayed(this, 1000); // Wait 1 second before showing the next direction
                } else {
                    userTurn = true; // It's now the player's turn
                    Toast.makeText(SensorSequenceGameActivity.this, "Your turn!", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(() -> sensorManager.registerListener(SensorSequenceGameActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL), 2000); // Resume sensor listening after 2 seconds
                }
            }
        }, 1000);
    }

    private void highlightDirection(int direction) {
        final ImageView arrowView;

        switch (direction) {
            case 0:
                arrowView = circleRed;
                break;
            case 1:
                arrowView = circleYellow;
                break;
            case 2:
                arrowView = circleBlue;
                break;
            case 3:
                arrowView = circleGreen;
                break;
            default:
                return;
        }

        if (arrowView != null) {
            arrowView.setColorFilter(Color.WHITE); // Highlight the direction
            handler.postDelayed(() -> arrowView.clearColorFilter(), 500); // Remove the highlight after 0.5 seconds
        }
    }

    private void resetInitialCoordinates() {
        if (lastSensorEvent != null) {
            initialCoordinates[0] = lastSensorEvent.values[0];
            initialCoordinates[1] = lastSensorEvent.values[1];
            initialCoordinates[2] = lastSensorEvent.values[2];
            Log.d("SensorSequenceGame", "Initial coordinates set: x=" + initialCoordinates[0] + ", y=" + initialCoordinates[1] + ", z=" + initialCoordinates[2]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundLevelComplete != null) soundLevelComplete.release(); // Release media player resources
        if (soundWrongAnswer != null) soundWrongAnswer.release(); // Release media player resources
        if (soundGameOver != null) soundGameOver.release(); // Release media player resources
    }
}
