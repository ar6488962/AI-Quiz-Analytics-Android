package com.example.quizapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "QuizMaster.db";
    private static final int DATABASE_VERSION = 5; // Bumped for badges table

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Core tables
        db.execSQL("CREATE TABLE users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT UNIQUE, " +
            "password TEXT, " +
            "role TEXT)");

        db.execSQL("CREATE TABLE attempts (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT, " +
            "score INTEGER, " +
            "date TEXT, " +
            "category TEXT, " +
            "correct INTEGER, " +
            "wrong INTEGER, " +
            "timeTaken LONG)");

        db.execSQL("CREATE TABLE attempt_details (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "attempt_id INTEGER, " +
            "question TEXT, " +
            "selected TEXT, " +
            "correct TEXT, " +
            "is_right INTEGER)");

        // ✅ NEW: Badges table
        db.execSQL("CREATE TABLE badges (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT, " +
            "badge_key TEXT, " +
            "badge_name TEXT, " +
            "badge_description TEXT, " +
            "earned_date TEXT)");

        // Default Teacher/Admin account
        ContentValues admin = new ContentValues();
        admin.put("username", "teacher");
        admin.put("password", "teacher123");
        admin.put("role", "teacher");
        db.insert("users", null, admin);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS attempts");
        db.execSQL("DROP TABLE IF EXISTS attempt_details");
        db.execSQL("DROP TABLE IF EXISTS badges");
        onCreate(db);
    }

    // ─── User Methods ───────────────────────────────────────────

    public boolean registerUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("role", role);
        return db.insert("users", null, values) != -1;
    }

    public Models.User loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM users WHERE username = ? AND password = ?",
            new String[]{username, password});
        if (cursor.moveToFirst()) {
            Models.User user = new Models.User(
                cursor.getInt(0), cursor.getString(1),
                cursor.getString(2), cursor.getString(3));
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }

    public Cursor getAllStudents() {
        return getReadableDatabase().rawQuery(
            "SELECT * FROM users WHERE role = 'student'", null);
    }

    // ─── Attempt Methods ────────────────────────────────────────

    public long insertAttempt(Models.Attempt attempt, ArrayList<Models.AttemptDetail> details) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", attempt.username);
        v.put("score", attempt.score);
        v.put("date", attempt.date);
        v.put("category", attempt.category);
        v.put("correct", attempt.correct);
        v.put("wrong", attempt.wrong);
        v.put("timeTaken", attempt.timeTaken);
        long attemptId = db.insert("attempts", null, v);

        for (Models.AttemptDetail d : details) {
            ContentValues dv = new ContentValues();
            dv.put("attempt_id", attemptId);
            dv.put("question", d.question);
            dv.put("selected", d.selectedOption);
            dv.put("correct", d.correctOption);
            dv.put("is_right", d.isCorrect ? 1 : 0);
            db.insert("attempt_details", null, dv);
        }
        return attemptId;
    }

    public Cursor getStudentHistory(String username) {
        return getReadableDatabase().rawQuery(
            "SELECT * FROM attempts WHERE username = ? ORDER BY id DESC",
            new String[]{username});
    }

    public Cursor getAttemptDetails(long attemptId) {
        return getReadableDatabase().rawQuery(
            "SELECT * FROM attempt_details WHERE attempt_id = ?",
            new String[]{String.valueOf(attemptId)});
    }

    public Cursor getAllAttempts() {
        return getReadableDatabase().rawQuery(
            "SELECT * FROM attempts ORDER BY score DESC", null);
    }

    // ─── Badge Methods ──────────────────────────────────────────

    /**
     * Check if a user already has a specific badge (by key)
     */
    public boolean hasBadge(String username, String badgeKey) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT id FROM badges WHERE username = ? AND badge_key = ?",
            new String[]{username, badgeKey});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Award a badge to a user
     */
    public void insertBadge(String username, String badgeKey, String badgeName, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("badge_key", badgeKey);
        values.put("badge_name", badgeName);
        values.put("badge_description", description);
        values.put("earned_date", date);
        db.insert("badges", null, values);
    }

    /**
     * Get all badges earned by a user
     */
    public Cursor getUserBadges(String username) {
        return getReadableDatabase().rawQuery(
            "SELECT * FROM badges WHERE username = ? ORDER BY id DESC",
            new String[]{username});
    }

    /**
     * Count badges earned by a user
     */
    public int getBadgeCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) FROM badges WHERE username = ?",
            new String[]{username});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
