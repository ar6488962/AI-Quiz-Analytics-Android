package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private UserAdapter adapter;
    private List<StudentStats> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        String teacherName = getIntent().getStringExtra("teacherName");

        // Stats
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        TextView totalUsersTv = findViewById(R.id.totalUsers);
        TextView totalAttemptsTv = findViewById(R.id.totalAttempts);
        TextView avgScoreTv = findViewById(R.id.avgScore);
        TextView countLabel = findViewById(R.id.studentCountLabel);
        RecyclerView recyclerView = findViewById(R.id.userRecyclerView);
        MaterialButton logoutBtn = findViewById(R.id.logoutBtn);

        // Fetch global stats
        Cursor uCursor = db.rawQuery("SELECT COUNT(*) FROM users WHERE role = 'student'", null);
        int totalStudents = 0;
        if (uCursor.moveToFirst()) {
            totalStudents = uCursor.getInt(0);
            totalUsersTv.setText(String.valueOf(totalStudents));
        }
        uCursor.close();

        Cursor aCursor = db.rawQuery("SELECT COUNT(*), AVG(score) FROM attempts", null);
        if (aCursor.moveToFirst()) {
            totalAttemptsTv.setText(String.valueOf(aCursor.getInt(0)));
            avgScoreTv.setText(String.format("%.1f", aCursor.getDouble(1)));
        }
        aCursor.close();

        // Build student list with per-student stats
        Cursor studentCursor = db.rawQuery(
            "SELECT u.username, COUNT(a.id), COALESCE(MAX(a.score),0), COALESCE(AVG(a.score),0) " +
            "FROM users u LEFT JOIN attempts a ON u.username = a.username " +
            "WHERE u.role = 'student' GROUP BY u.username ORDER BY MAX(a.score) DESC", null);

        while (studentCursor.moveToNext()) {
            String uname = studentCursor.getString(0);
            int quizCount = studentCursor.getInt(1);
            int bestScore = studentCursor.getInt(2);
            double avgScore = studentCursor.getDouble(3);
            fullList.add(new StudentStats(uname, quizCount, bestScore, avgScore));
        }
        studentCursor.close();

        countLabel.setText(totalStudents + " total");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(new ArrayList<>(fullList));
        recyclerView.setAdapter(adapter);

        // Search filter
        TextInputEditText searchInput = findViewById(R.id.searchStudent);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Logout
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void filterStudents(String query) {
        List<StudentStats> filtered = new ArrayList<>();
        for (StudentStats s : fullList) {
            if (s.username.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(s);
            }
        }
        adapter.updateList(filtered);
    }

    // --- Data Model ---
    static class StudentStats {
        String username;
        int quizCount;
        int bestScore;
        double avgScore;

        StudentStats(String username, int quizCount, int bestScore, double avgScore) {
            this.username = username;
            this.quizCount = quizCount;
            this.bestScore = bestScore;
            this.avgScore = avgScore;
        }
    }

    // --- Adapter ---
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<StudentStats> list;

        UserAdapter(List<StudentStats> list) { this.list = list; }

        void updateList(List<StudentStats> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StudentStats s = list.get(position);
            holder.name.setText(s.username);
            holder.quizCount.setText(s.quizCount + " quizzes");
            holder.bestScore.setText("Best: " + s.bestScore + " pts");
            holder.avgScore.setText(String.format("Avg: %.1f", s.avgScore));
            holder.rank.setText("#" + (position + 1));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, HistoryActivity.class);
                intent.putExtra("username", s.username);
                intent.putExtra("isTeacher", true);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, quizCount, bestScore, avgScore, rank;
            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.studentName);
                quizCount = v.findViewById(R.id.studentQuizCount);
                bestScore = v.findViewById(R.id.studentBestScore);
                avgScore = v.findViewById(R.id.studentAvgScore);
                rank = v.findViewById(R.id.studentRank);
            }
        }
    }
}
