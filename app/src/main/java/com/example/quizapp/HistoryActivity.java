package com.example.quizapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private boolean isTeacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        isTeacher = getIntent().getBooleanExtra("isTeacher", false);
        String username = getIntent().getStringExtra("username");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(isTeacher ? username + "'s History" : "My Quiz History");
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        TextView emptyText = findViewById(R.id.emptyText);

        // Stats banner views
        TextView statTotal = findViewById(R.id.statTotalQuizzes);
        TextView statBest = findViewById(R.id.statBestScore);
        TextView statAvg = findViewById(R.id.statAvgAccuracy);

        DBHelper db = new DBHelper(this);
        Cursor cursor = db.getStudentHistory(username);
        List<Models.Attempt> list = new ArrayList<>();

        int bestScore = 0;
        int totalCorrect = 0;
        int totalQuestions = 0;

        while (cursor.moveToNext()) {
            Models.Attempt attempt = new Models.Attempt(
                cursor.getInt(0), cursor.getString(1), cursor.getInt(2),
                cursor.getString(3), cursor.getString(4), cursor.getInt(5),
                cursor.getInt(6), cursor.getLong(7));
            list.add(attempt);

            if (attempt.score > bestScore) bestScore = attempt.score;
            totalCorrect += attempt.correct;
            totalQuestions += (attempt.correct + attempt.wrong);
        }
        cursor.close();

        // Update stats banner
        int quizCount = list.size();
        statTotal.setText(String.valueOf(quizCount));
        statBest.setText(String.valueOf(bestScore));
        int avgAccuracy = totalQuestions > 0 ? (totalCorrect * 100) / totalQuestions : 0;
        statAvg.setText(avgAccuracy + "%");

        if (list.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new HistoryAdapter(list));
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final List<Models.Attempt> list;
        HistoryAdapter(List<Models.Attempt> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attempt, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Models.Attempt item = list.get(position);
            holder.score.setText(String.valueOf(item.score));
            holder.date.setText(item.date);
            holder.cat.setText(item.category);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HistoryActivity.this, AttemptDetailsActivity.class);
                intent.putExtra("attemptId", (long) item.id);
                intent.putExtra("username", item.username);
                intent.putExtra("date", item.date);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView score, date, cat;
            ViewHolder(View v) {
                super(v);
                score = v.findViewById(R.id.itemScore);
                date = v.findViewById(R.id.itemDate);
                cat = v.findViewById(R.id.itemCat);
            }
        }
    }
}
