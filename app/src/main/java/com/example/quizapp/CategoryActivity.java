package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity {

    private String username;
    private ChipGroup categoryGroup, difficultyGroup, questionCountGroup;
    private ProgressBar loadingBar;
    private MaterialButton startBtn;
    private OkHttpClient client = new OkHttpClient();

    // 10 expanded categories with Open Trivia DB IDs
    private final String[] CATEGORY_NAMES = {
        "General Knowledge", "Science & Nature", "History",
        "Geography", "Sports", "Movies",
        "Music", "Technology", "Mythology", "Art & Literature"
    };
    private final int[] CATEGORY_IDS = {
        9, 17, 23, 22, 21, 11, 12, 18, 20, 25
    };

    private final String[] DIFFICULTIES = {"Easy", "Medium", "Hard"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        username = getIntent().getStringExtra("username");

        TextView welcomeText = findViewById(R.id.welcomeText);
        if (username != null && !username.isEmpty()) {
            welcomeText.setText("Hi, " + username + "! 👋");
        }

        categoryGroup = findViewById(R.id.categoryChipGroup);
        difficultyGroup = findViewById(R.id.difficultyChipGroup);
        loadingBar = findViewById(R.id.loadingProgress);
        startBtn = findViewById(R.id.startQuizBtn);
        questionCountGroup = findViewById(R.id.questionCountGroup);

        // Dynamically add category chips
        for (String catName : CATEGORY_NAMES) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_category_chip, categoryGroup, false);
            chip.setText(catName);
            chip.setId(View.generateViewId());
            categoryGroup.addView(chip);
        }
        // Select first category automatically
        if (categoryGroup.getChildCount() > 0) {
            ((Chip) categoryGroup.getChildAt(0)).setChecked(true);
        }

        // Start Quiz button
        startBtn.setOnClickListener(v -> {
            int selectedCatId = categoryGroup.getCheckedChipId();
            int selectedDiffId = difficultyGroup.getCheckedChipId();

            if (selectedCatId == View.NO_ID) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedDiffId == View.NO_ID) {
                Toast.makeText(this, "Please select a difficulty", Toast.LENGTH_SHORT).show();
                return;
            }

            Chip catChip = findViewById(selectedCatId);
            Chip diffChip = findViewById(selectedDiffId);
            
            String cat = catChip.getText().toString();
            String diff = diffChip.getText().toString();

            int questionCount = getSelectedQuestionCount();
            fetchQuestions(cat, diff.toLowerCase(), questionCount);
        });

        // Navigation buttons
        findViewById(R.id.viewHistoryBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        findViewById(R.id.viewLeaderboardBtn).setOnClickListener(v ->
            startActivity(new Intent(this, LeaderboardActivity.class)));

        findViewById(R.id.viewBadgesBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, BadgesActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // badgesQuickBtn (header area) - same as viewBadgesBtn
        findViewById(R.id.badgesQuickBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, BadgesActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Logout functionality
        findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            // Completely wipe the stack so they can't go 'back' into the app after logout
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }



    private int getSelectedQuestionCount() {
        int checkedId = questionCountGroup.getCheckedChipId();
        if (checkedId == R.id.chip5) return 5;
        if (checkedId == R.id.chip15) return 15;
        return 10; // default
    }

    private int getCategoryId(String catName) {
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            if (CATEGORY_NAMES[i].equals(catName)) {
                return CATEGORY_IDS[i];
            }
        }
        return 9; // fallback: General Knowledge
    }

    private void fetchQuestions(String cat, String diff, int count) {
        startBtn.setEnabled(false);
        loadingBar.setVisibility(View.VISIBLE);

        int catId = getCategoryId(cat);
        String url = "https://opentdb.com/api.php?amount=" + count
            + "&category=" + catId
            + "&difficulty=" + diff
            + "&type=multiple";

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    startBtn.setEnabled(true);
                    loadingBar.setVisibility(View.GONE);
                    Toast.makeText(CategoryActivity.this,
                        "Network Error: Check Internet Connection", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        startBtn.setEnabled(true);
                        loadingBar.setVisibility(View.GONE);
                        Toast.makeText(CategoryActivity.this,
                            "Server error. Please try again.", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JSONObject jobject = new JSONObject(jsonData);

                    if (jobject.getInt("response_code") != 0) {
                        runOnUiThread(() -> {
                            startBtn.setEnabled(true);
                            loadingBar.setVisibility(View.GONE);
                            Toast.makeText(CategoryActivity.this,
                                "Not enough questions for this selection. Try a different combination.",
                                Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    JSONArray jarray = jobject.getJSONArray("results");
                    ArrayList<Models.Question> apiQuestions = new ArrayList<>();

                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject obj = jarray.getJSONObject(i);
                        String qText = decodeHtml(obj.getString("question"));
                        String correctAns = decodeHtml(obj.getString("correct_answer"));
                        JSONArray incorrects = obj.getJSONArray("incorrect_answers");

                        String[] options = new String[4];
                        options[0] = decodeHtml(incorrects.getString(0));
                        options[1] = decodeHtml(incorrects.getString(1));
                        options[2] = decodeHtml(incorrects.getString(2));
                        options[3] = correctAns;

                        // Shuffle options
                        ArrayList<String> oList = new ArrayList<>();
                        for (String s : options) oList.add(s);
                        java.util.Collections.shuffle(oList);

                        int finalAnsIndex = 0;
                        for (int j = 0; j < 4; j++) {
                            options[j] = oList.get(j);
                            if (options[j].equals(correctAns)) finalAnsIndex = j;
                        }

                        apiQuestions.add(new Models.Question(0, qText, options,
                            finalAnsIndex, cat, diff.toUpperCase()));
                    }

                    runOnUiThread(() -> {
                        startBtn.setEnabled(true);
                        loadingBar.setVisibility(View.GONE);
                        Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("category", cat);
                        intent.putExtra("questionCount", count);
                        intent.putExtra("questions", apiQuestions);
                        startActivity(intent);
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        startBtn.setEnabled(true);
                        loadingBar.setVisibility(View.GONE);
                        Toast.makeText(CategoryActivity.this,
                            "Data Error. Please try again.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private String decodeHtml(String text) {
        return android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY).toString();
    }
}
