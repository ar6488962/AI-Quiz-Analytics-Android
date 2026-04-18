package com.example.quizapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.radiobutton.MaterialRadioButton;
import android.widget.RadioGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SECONDS_PER_QUESTION = 30;

    private TextView questionTv, timerTv, qNoTv, streakTv;
    private LinearProgressIndicator progressBar;
    private RadioGroup optionsGroup;
    private MaterialRadioButton[] optButtons = new MaterialRadioButton[4];
    private MaterialButton nextBtn, prevBtn, submitBtn;

    private ArrayList<Models.Question> questionsList;
    private int[] userAnswers;
    private int currentIndex = 0;
    private int questionCount = 10;
    private String username, category;
    private long startTime;
    private CountDownTimer countDownTimer;

    // Streak tracking
    private int currentStreak = 0;
    private int maxStreak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = getIntent().getStringExtra("username");
        category = getIntent().getStringExtra("category");
        questionsList = (ArrayList<Models.Question>) getIntent().getSerializableExtra("questions");
        questionCount = getIntent().getIntExtra("questionCount", 10);

        if (questionsList == null || questionsList.isEmpty()) {
            finish();
            return;
        }
        questionCount = questionsList.size();
        userAnswers = new int[questionCount];

        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("index");
            userAnswers = savedInstanceState.getIntArray("answers");
            currentStreak = savedInstanceState.getInt("streak", 0);
            maxStreak = savedInstanceState.getInt("maxStreak", 0);
        } else {
            for (int i = 0; i < questionCount; i++) userAnswers[i] = -1;
        }

        questionTv = findViewById(R.id.question);
        timerTv = findViewById(R.id.timer);
        qNoTv = findViewById(R.id.qno);
        streakTv = findViewById(R.id.streakText);
        progressBar = findViewById(R.id.progress);
        optionsGroup = findViewById(R.id.group);
        optButtons[0] = findViewById(R.id.opt1);
        optButtons[1] = findViewById(R.id.opt2);
        optButtons[2] = findViewById(R.id.opt3);
        optButtons[3] = findViewById(R.id.opt4);
        nextBtn = findViewById(R.id.next);
        prevBtn = findViewById(R.id.prev);
        submitBtn = findViewById(R.id.submit);

        progressBar.setMax(questionCount);
        startTime = System.currentTimeMillis();

        loadQuestion();

        nextBtn.setOnClickListener(v -> {
            saveAnswer();
            if (currentIndex < questionCount - 1) {
                currentIndex++;
                loadQuestion();
            }
        });

        prevBtn.setOnClickListener(v -> {
            saveAnswer();
            if (currentIndex > 0) {
                currentIndex--;
                loadQuestion();
            }
        });

        submitBtn.setOnClickListener(v -> {
            saveAnswer();
            submitQuiz();
        });
    }

    private void startPerQuestionTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(SECONDS_PER_QUESTION * 1000L, 1000) {
            public void onTick(long l) {
                int secs = (int)(l / 1000);
                timerTv.setText(String.valueOf(secs));
                // Color changes: >10s normal, 6-10s warning, ≤5s danger
                if (secs > 10) {
                    timerTv.setTextColor(Color.WHITE);
                } else if (secs > 5) {
                    timerTv.setTextColor(Color.parseColor("#FF6D00"));
                } else {
                    timerTv.setTextColor(Color.parseColor("#FF1744"));
                }
            }
            public void onFinish() {
                // Auto-advance to next question on timeout
                saveAnswer();
                if (currentIndex < questionCount - 1) {
                    currentIndex++;
                    loadQuestion();
                } else {
                    submitQuiz();
                }
            }
        }.start();
    }

    private void loadQuestion() {
        Models.Question q = questionsList.get(currentIndex);
        questionTv.setText(q.question);
        qNoTv.setText("Q " + (currentIndex + 1) + " / " + questionCount);
        progressBar.setProgress(currentIndex + 1);

        // Update streak display
        streakTv.setText(String.valueOf(currentStreak));

        optionsGroup.clearCheck();
        for (int i = 0; i < 4; i++) {
            optButtons[i].setText(q.options[i]);
            optButtons[i].setChecked(userAnswers[currentIndex] == i);
        }

        // Show/hide prev/next
        prevBtn.setEnabled(currentIndex > 0);
        nextBtn.setEnabled(currentIndex < questionCount - 1);

        startPerQuestionTimer();
    }

    private void saveAnswer() {
        int id = optionsGroup.getCheckedRadioButtonId();
        if (id != -1) {
            userAnswers[currentIndex] = optionsGroup.indexOfChild(findViewById(id));
        }
    }

    private void submitQuiz() {
        if (countDownTimer != null) countDownTimer.cancel();

        int correctCount = 0, wrongCount = 0, score = 0;
        int streak = 0;
        int localMaxStreak = 0;
        ArrayList<Models.AttemptDetail> details = new ArrayList<>();

        for (int i = 0; i < questionCount; i++) {
            Models.Question q = questionsList.get(i);
            int selectedIdx = userAnswers[i];
            String selectedText = (selectedIdx == -1) ? "Not Answered" : q.options[selectedIdx];
            String correctText = q.options[q.answerIndex];
            boolean isRight = (selectedIdx == q.answerIndex);

            if (isRight) {
                correctCount++;
                score += 4;
                streak++;
                if (streak > localMaxStreak) localMaxStreak = streak;
            } else if (selectedIdx != -1) {
                wrongCount++;
                score -= 1;
                streak = 0;
            } else {
                streak = 0;
            }

            details.add(new Models.AttemptDetail(q.question, selectedText, correctText, isRight));
        }

        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        String difficulty = questionsList.get(0).difficulty;

        Models.Attempt attempt = new Models.Attempt(
            0, username, score, date, category, correctCount, wrongCount, timeTaken);
        DBHelper db = new DBHelper(this);
        db.insertAttempt(attempt, details);

        // Award badges
        String badgeEarned = checkAndAwardBadges(db, score, correctCount, timeTaken, localMaxStreak);

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("correct", correctCount);
        intent.putExtra("wrong", wrongCount);
        intent.putExtra("timeTaken", timeTaken);
        intent.putExtra("questionCount", questionCount);
        intent.putExtra("username", username);
        intent.putExtra("maxStreak", localMaxStreak);
        if (badgeEarned != null) intent.putExtra("badgeEarned", badgeEarned);
        startActivity(intent);
        finish();
    }

    private String checkAndAwardBadges(DBHelper db, int score, int correct, long timeTaken, int streak) {
        String badgeEarned = null;

        // First Quiz badge
        if (!db.hasBadge(username, "First Quiz")) {
            db.insertBadge(username, "First Quiz", "🎯 First Quiz!", "Completed your first quiz");
            badgeEarned = "🎯 First Quiz!";
        }
        // Perfect Score badge
        if (correct == questionsList.size() && !db.hasBadge(username, "Perfect Score")) {
            db.insertBadge(username, "Perfect Score", "💎 Perfect Score!", "Answered ALL questions correctly");
            badgeEarned = "💎 Perfect Score!";
        }
        // Speed Demon: finished in under 60 seconds
        if (timeTaken < 60 && !db.hasBadge(username, "Speed Demon")) {
            db.insertBadge(username, "Speed Demon", "⚡ Speed Demon!", "Completed a quiz in under 60 seconds");
            badgeEarned = "⚡ Speed Demon!";
        }
        // Streak Master: 5+ consecutive correct
        if (streak >= 5 && !db.hasBadge(username, "Streak Master")) {
            db.insertBadge(username, "Streak Master", "🔥 Streak Master!", "Got 5+ answers correct in a row");
            badgeEarned = "🔥 Streak Master!";
        }
        // High Scorer: score > 30
        if (score >= 30 && !db.hasBadge(username, "High Scorer")) {
            db.insertBadge(username, "High Scorer", "🏆 High Scorer!", "Scored 30+ points in a single quiz");
            badgeEarned = "🏆 High Scorer!";
        }

        return badgeEarned;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("index", currentIndex);
        outState.putIntArray("answers", userAnswers);
        outState.putInt("streak", currentStreak);
        outState.putInt("maxStreak", maxStreak);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
