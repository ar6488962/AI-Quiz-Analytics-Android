package com.example.quizapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class AttemptDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attempt_details);

        long attemptId = getIntent().getLongExtra("attemptId", -1);
        String username = getIntent().getStringExtra("username");
        String date = getIntent().getStringExtra("date");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView userTv = findViewById(R.id.detailUser);
        TextView dateTv = findViewById(R.id.detailDate);
        userTv.setText("Student: " + username);
        dateTv.setText("Date: " + date);

        RecyclerView recyclerView = findViewById(R.id.detailsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DBHelper db = new DBHelper(this);
        Cursor cursor = db.getAttemptDetails(attemptId);
        List<Models.AttemptDetail> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(new Models.AttemptDetail(
                cursor.getString(2), // question
                cursor.getString(3), // selected
                cursor.getString(4), // correct
                cursor.getInt(5) == 1 // is_right
            ));
        }
        cursor.close();

        recyclerView.setAdapter(new DetailsAdapter(list));
    }

    private static class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {
        private final List<Models.AttemptDetail> list;
        DetailsAdapter(List<Models.AttemptDetail> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attempt_detail, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Models.AttemptDetail item = list.get(position);
            holder.q.setText(item.question);
            holder.s.setText("Selected: " + item.selectedOption);
            holder.c.setText("Correct: " + item.correctOption);
            
            if (item.isCorrect) {
                holder.s.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.correct_green));
                holder.status.setImageResource(android.R.drawable.presence_online); // green dot or similar
                holder.status.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.correct_green));
            } else {
                holder.s.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.wrong_red));
                holder.status.setImageResource(android.R.drawable.presence_busy); // red dot or similar
                holder.status.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.wrong_red));
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView q, s, c;
            ImageView status;
            ViewHolder(View v) {
                super(v);
                q = v.findViewById(R.id.detailQuestion);
                s = v.findViewById(R.id.detailSelected);
                c = v.findViewById(R.id.detailCorrect);
                status = v.findViewById(R.id.detailStatusIcon);
            }
        }
    }
}
