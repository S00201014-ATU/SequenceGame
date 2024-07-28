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
    private ImageView circleRed, circleYellow, circleBlue, circleGreen;
    private boolean userTurn = false;

    private float[] initialCoordinates = new float[3];
    private SensorEvent lastSensorEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_sequence_game);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        tvScore = findViewById(R.id.tvScore);
        circleRed = findViewById(R.id.redUp);
        circleYellow = findViewById(R.id.yellowDown);
        circleBlue = findViewById(R.id.blueLeft);
        circleGreen = findViewById(R.id.greenRight);

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
            lastSensorEvent = event;
            handleAccelerometer(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private void handleAccelerometer(float[] values) {
        float x = values[0];
        float y = values[1];
        float z = values[2];

        String direction = null;
        float rotationThreshold = 5.0f; // Larger threshold for significant rotation
        float returnThreshold = 1.5f; // Threshold for returning to initial position

        // Adjust for landscape mode
        float adjustedX = -y;
        float adjustedY = x;

        Log.d("SensorSequenceGame", "Accelerometer readings: adjustedX=" + adjustedX + ", adjustedY=" + adjustedY + ", z=" + z);

        // Detecting significant rotation
        if (Math.abs(adjustedX) > rotationThreshold) {
            direction = adjustedX > 0 ? "Left" : "Right";
        } else if (Math.abs(adjustedY) > rotationThreshold) {
            direction = adjustedY > 0 ? "Down" : "Up";
        }

        Log.d("SensorSequenceGame", "Detected direction: " + direction);

        // Check if the device is close to the initial position
        if (Math.abs(adjustedX - initialCoordinates[0]) < returnThreshold &&
                Math.abs(adjustedY - initialCoordinates[1]) < returnThreshold &&
                Math.abs(z - initialCoordinates[2]) < returnThreshold && direction != null) {
            Log.d("SensorSequenceGame", "Device returned to initial position. Registering direction: " + direction);
            handleDirectionInput(direction);
        } else {
            Log.d("SensorSequenceGame", "Device has not returned to initial position or no direction detected yet.");
        }
    }

    private void handleDirectionInput(String direction) {
        playerSequence.add(direction);
        Log.d("SensorSequenceGame", "Player sequence: " + playerSequence);

        if (playerSequence.size() <= gameSequence.size()) {
            if (playerSequence.get(playerSequence.size() - 1).equals(gameSequence.get(playerSequence.size() - 1))) {
                if (playerSequence.size() == gameSequence.size()) {
                    score += 2;
                    tvScore.setText("Score: " + score);
                    soundLevelComplete.start();
                    Toast.makeText(this, "Round complete!", Toast.LENGTH_SHORT).show();
                    soundLevelComplete.setOnCompletionListener(mp -> startNewRound());
                } else {
                    resetInitialCoordinates();
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
        gameSequence.clear();

        for (int i = 0; i < 2; i++) {
            gameSequence.add(randomDirection());
        }

        Log.d("SensorSequenceGame", "Game sequence: " + gameSequence);

        resetInitialCoordinates();
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
                    userTurn = true;
                }
            }
        };

        handler.post(displayRunnable);
    }

    private void highlightDirection(String direction) {
        final ImageView arrowView;

        switch (direction) {
            case "Up":
                arrowView = circleRed;
                break;
            case "Down":
                arrowView = circleYellow;
                break;
            case "Left":
                arrowView = circleBlue;
                break;
            case "Right":
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
        // Capture the current sensor values as the new initial position
        if (lastSensorEvent != null) {
            initialCoordinates[0] = -lastSensorEvent.values[1]; // Adjust for landscape mode
            initialCoordinates[1] = lastSensorEvent.values[0]; // Adjust for landscape mode
            initialCoordinates[2] = lastSensorEvent.values[2];
            Log.d("SensorSequenceGame", "Initial coordinates set: adjustedX=" + initialCoordinates[0] + ", adjustedY=" + initialCoordinates[1] + ", z=" + initialCoordinates[2]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundLevelComplete != null) soundLevelComplete.release();
        if (soundWrongAnswer != null) soundWrongAnswer.release();
        if (soundGameOver != null) soundGameOver.release();
    }
}
