package com.example.quizapp;

import android.database.Cursor;
import android.graphics.Color;
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

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DBHelper db = new DBHelper(this);
        Cursor cursor = db.getAllAttempts();
        List<Models.Attempt> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(new Models.Attempt(
                cursor.getInt(0), cursor.getString(1), cursor.getInt(2),
                cursor.getString(3), cursor.getString(4), cursor.getInt(5),
                cursor.getInt(6), cursor.getLong(7)));
        }
        cursor.close();

        // Fill podium (top 3)
        fillPodium(list);

        recyclerView.setAdapter(new LeaderAdapter(list));
    }

    private void fillPodium(List<Models.Attempt> list) {
        TextView rank1name = findViewById(R.id.rank1name);
        TextView rank1score = findViewById(R.id.rank1score);
        TextView rank2name = findViewById(R.id.rank2name);
        TextView rank2score = findViewById(R.id.rank2score);
        TextView rank3name = findViewById(R.id.rank3name);
        TextView rank3score = findViewById(R.id.rank3score);

        if (list.size() >= 1) {
            rank1name.setText(list.get(0).username);
            rank1score.setText(list.get(0).score + " pts");
        }
        if (list.size() >= 2) {
            rank2name.setText(list.get(1).username);
            rank2score.setText(list.get(1).score + " pts");
        }
        if (list.size() >= 3) {
            rank3name.setText(list.get(2).username);
            rank3score.setText(list.get(2).score + " pts");
        }
    }

    private static class LeaderAdapter extends RecyclerView.Adapter<LeaderAdapter.ViewHolder> {
        private final List<Models.Attempt> list;
        LeaderAdapter(List<Models.Attempt> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Models.Attempt item = list.get(position);
            holder.name.setText(item.username);
            holder.score.setText(String.valueOf(item.score));
            holder.date.setText(item.category + " • " + item.date);

            // Medal display
            if (position == 0) {
                holder.rank.setText("🥇");
                holder.rank.setTextSize(20f);
            } else if (position == 1) {
                holder.rank.setText("🥈");
                holder.rank.setTextSize(20f);
            } else if (position == 2) {
                holder.rank.setText("🥉");
                holder.rank.setTextSize(20f);
            } else {
                holder.rank.setText("#" + (position + 1));
                holder.rank.setTextSize(14f);
                holder.rank.setTextColor(Color.parseColor("#7B6FA0"));
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView rank, name, score, date;
            ViewHolder(View v) {
                super(v);
                rank = v.findViewById(R.id.rank);
                name = v.findViewById(R.id.name);
                score = v.findViewById(R.id.score);
                date = v.findViewById(R.id.date);
            }
        }
    }
}
