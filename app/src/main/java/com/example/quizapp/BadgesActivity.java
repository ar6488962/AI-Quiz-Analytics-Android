package com.example.quizapp;

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

public class BadgesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        String username = getIntent().getStringExtra("username");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView countText = findViewById(R.id.badgeCountText);
        RecyclerView recycler = findViewById(R.id.badgesRecycler);
        TextView emptyText = findViewById(R.id.emptyBadgesText);

        DBHelper db = new DBHelper(this);
        Cursor cursor = db.getUserBadges(username);

        List<Models.Badge> badges = new ArrayList<>();
        while (cursor.moveToNext()) {
            badges.add(new Models.Badge(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5)
            ));
        }
        cursor.close();

        int count = badges.size();
        countText.setText(count + (count == 1 ? " Badge Earned" : " Badges Earned"));

        if (badges.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recycler.setVisibility(View.VISIBLE);
            recycler.setLayoutManager(new LinearLayoutManager(this));
            recycler.setAdapter(new BadgeAdapter(badges));
        }
    }

    private static class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.ViewHolder> {
        private final List<Models.Badge> list;
        BadgeAdapter(List<Models.Badge> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Models.Badge badge = list.get(position);
            holder.emoji.setText(extractEmoji(badge.badgeName));
            holder.name.setText(cleanName(badge.badgeName));
            holder.description.setText(badge.badgeDescription);
            holder.date.setText("Earned: " + badge.earnedDate);
        }

        // Extract leading emoji from badge name string like "🏅 Badge Name"
        private String extractEmoji(String name) {
            if (name == null || name.isEmpty()) return "🏅";
            String[] parts = name.split(" ", 2);
            return parts[0];
        }

        private String cleanName(String name) {
            if (name == null || name.isEmpty()) return "";
            // Remove leading emoji if present
            return name.replaceFirst("^[^a-zA-Z]+", "").trim();
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView emoji, name, description, date;
            ViewHolder(View v) {
                super(v);
                emoji = v.findViewById(R.id.badgeEmoji);
                name = v.findViewById(R.id.badgeName);
                description = v.findViewById(R.id.badgeDescription);
                date = v.findViewById(R.id.badgeDate);
            }
        }
    }
}
