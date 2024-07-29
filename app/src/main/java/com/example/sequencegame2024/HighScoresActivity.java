package com.example.sequencegame2024;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HighScoresActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        LinearLayout scoresLayout = findViewById(R.id.layoutScores);

        List<HighScore> highScores = getHighScores();

        int highestScore = highScores.isEmpty() ? 0 : highScores.get(0).getScore();

        boolean isNewTopScore = getIntent().getBooleanExtra("IS_NEW_TOP_SCORE", false);
        boolean showCelebration = isNewTopScore && (highScores.size() > 0 && highScores.get(0).getScore() == highestScore);
        String highestScoreName = "";
        int highestScoreValue = 0;

        // Display the top 5 scores
        for (int i = 0; i < Math.min(highScores.size(), 5); i++) {
            HighScore highScore = highScores.get(i);
            TextView scoreView = new TextView(this);
            scoreView.setText((i + 1) + ". " + highScore.getName() + ": " + highScore.getScore());
            scoreView.setTextSize(18);
            scoreView.setPadding(0, 10, 0, 10);
            scoresLayout.addView(scoreView);

            // Check if this is the highest score
            if (i == 0 && showCelebration) {
                highestScoreName = highScore.getName();
                highestScoreValue = highScore.getScore();
            }
        }

        // Only show celebratory effects if this is a new top score
        if (showCelebration) {
            playCelebratoryMusic();
            flashText(highestScoreName, highestScoreValue);
        }

        Button homeButton = findViewById(R.id.btnHome);
        homeButton.setOnClickListener(v -> {
            stopCelebratoryMusic();
            Intent intent = new Intent(HighScoresActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private List<HighScore> getHighScores() {
        List<HighScore> highScores = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_HIGHSCORES, null, null, null, null, null, DatabaseHelper.COLUMN_SCORE + " DESC, " + DatabaseHelper.COLUMN_NAME + " ASC");

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            int score = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCORE));
            highScores.add(new HighScore(score, name));
        }
        cursor.close();
        return highScores;
    }

    private void playCelebratoryMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.celebratory_music); // Ensure celebratory_music.mp3 is in res/raw folder
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
    }

    private void stopCelebratoryMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void flashText(String name, int score) {
        TextView flashTextView = new TextView(this);
        flashTextView.setText("Congratulations " + name + "! New High Score: " + score);
        flashTextView.setTextSize(24);
        flashTextView.setPadding(0, 20, 0, 20);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500); // Manage the time of the blink with this parameter
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        flashTextView.startAnimation(anim);

        LinearLayout scoresLayout = findViewById(R.id.layoutScores);
        scoresLayout.addView(flashTextView, 0); // Adding it at the top

        handler = new Handler();
        runnable = () -> flashTextView.clearAnimation();
        handler.postDelayed(runnable, 30000); // Stop the animation after 30 seconds
    }

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