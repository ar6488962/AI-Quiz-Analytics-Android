package com.example.quizapp;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        int score = getIntent().getIntExtra("score", 0);
        int correct = getIntent().getIntExtra("correct", 0);
        int wrong = getIntent().getIntExtra("wrong", 0);
        long timeTaken = getIntent().getLongExtra("timeTaken", 0);
        int questionCount = getIntent().getIntExtra("questionCount", 10);
        String username = getIntent().getStringExtra("username");
        int maxStreak = getIntent().getIntExtra("maxStreak", 0);
        String badgeEarned = getIntent().getStringExtra("badgeEarned");

        TextView scoreTv = findViewById(R.id.finalScore);
        TextView correctTv = findViewById(R.id.correctCount);
        TextView wrongTv = findViewById(R.id.wrongCount);
        TextView accuracyTv = findViewById(R.id.accuracy);
        TextView timeTv = findViewById(R.id.timeTaken);
        TextView gradeBadge = findViewById(R.id.gradeBadge);
        TextView gradeEmoji = findViewById(R.id.gradeEmoji);
        MaterialCardView badgeCard = findViewById(R.id.badgeCard);
        TextView badgeNameTv = findViewById(R.id.badgeName);
        PieChart pieChart = findViewById(R.id.pieChart);

        // Calculate grade
        int accuracy = questionCount > 0 ? (correct * 100) / questionCount : 0;
        setGrade(gradeEmoji, gradeBadge, accuracy);

        // Animate score count-up
        animateScore(scoreTv, score);

        // Set stats
        correctTv.setText(String.valueOf(correct));
        wrongTv.setText(String.valueOf(wrong));
        accuracyTv.setText(accuracy + "%");

        long min = timeTaken / 60;
        long sec = timeTaken % 60;
        timeTv.setText(min > 0 ? min + "m " + sec + "s" : sec + "s");

        // Badge earned banner
        if (badgeEarned != null && !badgeEarned.isEmpty()) {
            badgeCard.setVisibility(View.VISIBLE);
            badgeNameTv.setText(badgeEarned);
        }

        setupChart(pieChart, correct, wrong, questionCount);

        // Buttons
        MaterialButton shareBtn = findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(v -> shareScore(username, score, correct, questionCount, accuracy));

        MaterialButton leaderboardBtn = findViewById(R.id.leaderboardBtn);
        leaderboardBtn.setOnClickListener(v ->
            startActivity(new Intent(this, LeaderboardActivity.class)));

        MaterialButton homeBtn = findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            intent.putExtra("username", username);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void animateScore(TextView tv, int finalScore) {
        ValueAnimator animator = ValueAnimator.ofInt(0, finalScore);
        animator.setDuration(1200);
        animator.addUpdateListener(a -> {
            int val = (int) a.getAnimatedValue();
            tv.setText(String.valueOf(val));
            // Dynamically color the score
            if (val < 0) tv.setTextColor(Color.parseColor("#FF1744"));
            else if (val >= 30) tv.setTextColor(Color.parseColor("#00C853"));
            else tv.setTextColor(Color.parseColor("#6C35DE"));
        });
        animator.start();
    }

    private void setGrade(TextView emojiTv, TextView textTv, int accuracy) {
        if (accuracy >= 90) {
            emojiTv.setText("🏆");
            textTv.setText("Outstanding! 🌟");
        } else if (accuracy >= 70) {
            emojiTv.setText("😊");
            textTv.setText("Great Job!");
        } else if (accuracy >= 50) {
            emojiTv.setText("👍");
            textTv.setText("Good Effort!");
        } else {
            emojiTv.setText("💪");
            textTv.setText("Keep Practicing!");
        }
    }

    private void shareScore(String username, int score, int correct, int total, int accuracy) {
        String text = "🧠 Quiz Pro Results\n"
            + "Player: " + (username != null ? username : "Student") + "\n"
            + "Score: " + score + " pts\n"
            + "Correct: " + correct + "/" + total + "\n"
            + "Accuracy: " + accuracy + "%\n"
            + "Can you beat me? Download Quiz Pro! 🚀";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Share your score"));
    }

    private void setupChart(PieChart chart, int correct, int wrong, int total) {
        int unattempted = total - correct - wrong;
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (correct > 0) entries.add(new PieEntry(correct, "Correct"));
        if (wrong > 0) entries.add(new PieEntry(wrong, "Wrong"));
        if (unattempted > 0) entries.add(new PieEntry(unattempted, "Skipped"));

        int[] colors = {
            Color.parseColor("#00C853"),
            Color.parseColor("#FF1744"),
            Color.parseColor("#9E9E9E")
        };

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(13f);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.setCenterText("Performance");
        chart.setCenterTextSize(14f);
        chart.setHoleRadius(45f);
        chart.setTransparentCircleRadius(50f);
        chart.getLegend().setTextSize(12f);
        chart.animateY(1200);
        chart.invalidate();
    }
}
