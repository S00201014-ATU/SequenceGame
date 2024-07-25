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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SensorSequenceGameActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private List<String> gameSequence = new ArrayList<>();
    private List<String> playerSequence = new ArrayList<>();
    private int index = 0;
    private int score = 0;
    private Random random = new Random();
    private Handler handler = new Handler();
    private MediaPlayer soundLevelComplete;
    private MediaPlayer soundWrongAnswer;
    private MediaPlayer soundGameOver;

    private TextView tvScore;
    private boolean userTurn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_sequence_game);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        tvScore = findViewById(R.id.scoreTextView);

        // Initialize media players for sound effects
        soundLevelComplete = MediaPlayer.create(this, R.raw.level_complete);
        soundWrongAnswer = MediaPlayer.create(this, R.raw.wrong_answer);
        soundGameOver = MediaPlayer.create(this, R.raw.game_over);

        startNewRound();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (userTurn && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometer(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Implement if needed
    }

    private void handleAccelerometer(float[] values) {
        float x = values[0];
        float y = values[1];

        String direction = null;

        if (Math.abs(x) > Math.abs(y)) {
            if (x < -3) {
                direction = "Right";
            } else if (x > 3) {
                direction = "Left";
            }
        } else {
            if (y < -3) {
                direction = "Up";
            } else if (y > 3) {
                direction = "Down";
            }
        }

        if (direction != null) {
            handleDirectionInput(direction);
        }
    }

    private void handleDirectionInput(String direction) {
        playerSequence.add(direction);

        if (playerSequence.size() <= gameSequence.size()) {
            if (playerSequence.get(playerSequence.size() - 1).equals(gameSequence.get(playerSequence.size() - 1))) {
                if (playerSequence.size() == gameSequence.size()) {
                    score += 2; // Points per round
                    tvScore.setText("Score: " + score);
                    soundLevelComplete.start();
                    Toast.makeText(this, "Round complete!", Toast.LENGTH_SHORT).show();
                    soundLevelComplete.setOnCompletionListener(mp -> startNewRound());
                }
            } else {
                soundWrongAnswer.start();
                soundWrongAnswer.setOnCompletionListener(mp -> {
                    soundGameOver.start();
                    soundGameOver.setOnCompletionListener(mp1 -> {
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
        playerSequence.clear();

        // Add two new random directions to the game sequence
        gameSequence.add(randomDirection());
        gameSequence.add(randomDirection());

        // Show the sequence to the player
        showSequence();
    }

    private String randomDirection() {
        int rand = random.nextInt(4);
        switch (rand) {
            case 0: return "Up";
            case 1: return "Down";
            case 2: return "Left";
            case 3: return "Right";
            default: return null;
        }
    }

    private void showSequence() {
        userTurn = false;
        index = 0;

        Runnable displayRunnable = new Runnable() {
            @Override
            public void run() {
                if (index < gameSequence.size()) {
                    highlightDirection(gameSequence.get(index));
                    index++;
                    handler.postDelayed(this, 1000);
                } else {
                    tvScore.setText("Go!");
                    userTurn = true;
                }
            }
        };

        handler.post(displayRunnable);
    }

    private void highlightDirection(String direction) {
        // Update this method to handle the visual feedback for the direction
        // For example, you might change the background color of a TextView based on direction
        tvScore.setText(direction);
    }
}
