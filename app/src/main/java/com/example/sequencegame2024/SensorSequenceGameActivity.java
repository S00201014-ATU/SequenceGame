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

    private List<Integer> gameSequence = new ArrayList<>();
    private List<Integer> playerSequence = new ArrayList<>();
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
    private boolean firstRound = true; // Flag to check if it's the first round

    private float[] initialCoordinates = new float[3];
    private SensorEvent lastSensorEvent;
    private long lastLogTime = 0; // Timestamp for the last log
    private long lastDirectionTime = 0; // Timestamp for the last direction detection
    private static final long DIRECTION_DEBOUNCE_TIME = 500; // Debounce time in milliseconds

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

        Integer direction = null;
        float movementThreshold = 2.0f; // Larger threshold for significant movement
        float returnThreshold = 1.0f; // Threshold for returning to initial position

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime > 1000) { // Log only if 1 second has passed
            Log.d("SensorSequenceGame", "Accelerometer readings: x=" + x + ", y=" + y + ", z=" + z);
            lastLogTime = currentTime;
        }

        // Detecting significant lateral movement in landscape mode
        if (Math.abs(x - initialCoordinates[0]) > movementThreshold) {
            direction = x > initialCoordinates[0] ? 3 : 2; // In landscape mode, x-axis is Left (2)/Right (3)
        } else if (Math.abs(y - initialCoordinates[1]) > movementThreshold) {
            direction = y > initialCoordinates[1] ? 1 : 0; // In landscape mode, y-axis is Down (1)/Up (0)
        }

        Log.d("SensorSequenceGame", "Detected direction: " + direction);

        // Debounce mechanism to ensure stability in direction detection
        if (direction != null && currentTime - lastDirectionTime > DIRECTION_DEBOUNCE_TIME) {
            lastDirectionTime = currentTime;
            highlightDirection(direction);
            handleDirectionInput(direction);
        }
    }

    private void handleDirectionInput(int direction) {
        playerMove(direction);
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
                    tvScore.setText("Score: " + score);
                    sensorManager.unregisterListener(this); // Unregister the sensor listener
                    soundLevelComplete.start();
                    Toast.makeText(this, "Round complete!", Toast.LENGTH_SHORT).show();
                    soundLevelComplete.setOnCompletionListener(mp -> {
                        sensorManager.registerListener(SensorSequenceGameActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        startNewRound();
                    });
                }
            } else {
                sensorManager.unregisterListener(this); // Unregister the sensor listener
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

        resetInitialCoordinates();
        playSequence();
    }

    private void playSequence() {
        userTurn = false;
        sensorManager.unregisterListener(this); // Unregister the sensor listener
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
                    Toast.makeText(SensorSequenceGameActivity.this, "Your turn!", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(() -> sensorManager.registerListener(SensorSequenceGameActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL), 2000); // Register the sensor listener after the toast message disappears
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
            arrowView.setColorFilter(Color.WHITE);
            handler.postDelayed(() -> arrowView.clearColorFilter(), 500);
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
        if (soundLevelComplete != null) soundLevelComplete.release();
        if (soundWrongAnswer != null) soundWrongAnswer.release();
        if (soundGameOver != null) soundGameOver.release();
    }
}
