package com.example.sequencegame2024;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Name of the database
    private static final String DATABASE_NAME = "highscores.db";
    // Version of the database
    private static final int DATABASE_VERSION = 1;

    // Name of the high scores table
    public static final String TABLE_HIGHSCORES = "highscores";
    // Column name for the unique ID
    public static final String COLUMN_ID = "_id";
    // Column name for the player's name
    public static final String COLUMN_NAME = "name";
    // Column name for the player's score
    public static final String COLUMN_SCORE = "score";

    // SQL statement to create the high scores table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_HIGHSCORES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_SCORE + " INTEGER" +
                    ");";

    // Constructor for the DatabaseHelper
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statement to create the table
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIGHSCORES);
        // Create the table again
        onCreate(db);
    }
}
