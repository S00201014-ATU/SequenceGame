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

    // Variables for managing sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;

    // Lists for game sequence and player sequence
    private List<Integer> gameSequence = new ArrayList<>();
    private List<Integer> playerSequence = new ArrayList<>();
    private int index = 0;
    private int score = 0;
    private Random random = new Random();
    private Handler handler = new Handler();

    // Variables for sound effects
    private MediaPlayer soundLevelComplete;
    private MediaPlayer soundWrongAnswer;
    private MediaPlayer soundGameOver;

    // UI elements
    private TextView tvScore;
    private ImageView circleRed, circleYellow, circleBlue, circleGreen;
    private boolean userTurn = false;
    private boolean firstRound = true;

    // Variables for sensor values
    private float[] initialCoordinates = new float[3];
    private SensorEvent lastSensorEvent;
    private long lastLogTime = 0;
    private long lastDirectionTime = 0;
    private static final long DIRECTION_DEBOUNCE_TIME = 500; // 500 milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_sequence_game);

        // Initialise sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Initialise UI elements
        tvScore = findViewById(R.id.tvScore);
        circleRed = findViewById(R.id.redCircle);
        circleYellow = findViewById(R.id.yellowCircle);
        circleBlue = findViewById(R.id.blueCircle);
        circleGreen = findViewById(R.id.greenCircle);

        // Initialise sound effects
        soundLevelComplete = MediaPlayer.create(this, R.raw.level_complete);
        soundWrongAnswer = MediaPlayer.create(this, R.raw.wrong_answer);
        soundGameOver = MediaPlayer.create(this, R.raw.game_over);

        // Start the first round
        startNewRound();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the app is active
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener when the app is paused
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Handle sensor events if it's the user's turn and the event is from the accelerometer
        if (userTurn && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lastSensorEvent = event;
            handleAccelerometer(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used, but required for interface
    }

    private void handleAccelerometer(float[] values) {
        // Get x, y, z values from the accelerometer
        float x = values[0];
        float y = values[1];
        float z = values[2];

        Integer direction = null;
        float movementThreshold = 2.0f; // Threshold for detecting movement
        float returnThreshold = 1.0f; // Threshold for detecting return to initial position

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime > 1000) {
            // Log sensor readings every second
            Log.d("SensorSequenceGame", "Accelerometer readings: x=" + x + ", y=" + y + ", z=" + z);
            lastLogTime = currentTime;
        }

        // Determine direction of movement based on threshold
        if (Math.abs(x - initialCoordinates[0]) > movementThreshold) {
            direction = x > initialCoordinates[0] ? 3 : 2;
        } else if (Math.abs(y - initialCoordinates[1]) > movementThreshold) {
            direction = y > initialCoordinates[1] ? 1 : 0;
        }

        Log.d("SensorSequenceGame", "Detected direction: " + direction);

        // Handle direction if detected and debounce time has passed
        if (direction != null && currentTime - lastDirectionTime > DIRECTION_DEBOUNCE_TIME) {
            lastDirectionTime = currentTime;
            highlightDirection(direction);
            handleDirectionInput(direction);
        }
    }

    private void handleDirectionInput(int direction) {
        // Handle the player's move
        playerMove(direction);
    }

    private void playerMove(int color) {
        // Add player's move to the sequence
        playerSequence.add(color);
        Log.d("SensorSequenceGame", "Player sequence: " + playerSequence);

        if (playerSequence.size() <= gameSequence.size()) {
            // Check if player's move matches the game sequence
            if (playerSequence.get(playerSequence.size() - 1).equals(gameSequence.get(playerSequence.size() - 1))) {
                if (playerSequence.size() == gameSequence.size()) {
                    // Player completed the sequence
                    if (firstRound) {
                        score += 4;
                        firstRound = false;
                    } else {
                        score += 2;
                    }
                    tvScore.setText("Score: " + score);
                    soundLevelComplete.start();
                    Toast.makeText(this, "Round complete!", Toast.LENGTH_SHORT).show();
                    soundLevelComplete.setOnCompletionListener(mp -> startNewRound());
                }
            } else {
                // Player made a mistake
                soundWrongAnswer.start();
                soundWrongAnswer.setOnCompletionListener(mp -> {
                    soundGameOver.start();
                    soundGameOver.setOnCompletionListener(mp1 -> {
                        // Go to Game Over screen
                        Intent intent = new Intent(this, GameOverActivity.class);
                        intent.putExtra("score", score);
                        startActivity(intent);
                        finish();
                    });
                });
            }
        }
    }

    private void startNewRound() {
        // Clear player's sequence and add new moves to the game sequence
        playerSequence.clear();

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

        // Reset initial sensor values and play the game sequence
        resetInitialCoordinates();
        playSequence();
    }

    private void playSequence() {
        // Show the game sequence to the player
        userTurn = false;
        index = 0;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index < gameSequence.size()) {
                    highlightDirection(gameSequence.get(index));
                    index++;
                    handler.postDelayed(this, 1000);
                } else {
                    userTurn = true;
                    Toast.makeText(SensorSequenceGameActivity.this, "Your turn to recall the sequence", Toast.LENGTH_SHORT).show();
                }
            }
        }, 1000);
    }

    private void highlightDirection(int direction) {
        // Highlight the direction (red, yellow, blue, green) for a short time
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
            arrowView.setColorFilter(Color.WHITE);
            handler.postDelayed(() -> arrowView.clearColorFilter(), 500);
        }
    }

    private void resetInitialCoordinates() {
        // Reset initial sensor values for detecting movement
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
        // Release sound resources
        if (soundLevelComplete != null) soundLevelComplete.release();
        if (soundWrongAnswer != null) soundWrongAnswer.release();
        if (soundGameOver != null) soundGameOver.release();
    }
}
