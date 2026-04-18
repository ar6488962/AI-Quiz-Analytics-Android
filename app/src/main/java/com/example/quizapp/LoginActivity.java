package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText userEdit = findViewById(R.id.username);
        TextInputEditText passEdit = findViewById(R.id.password);
        MaterialButton loginBtn = findViewById(R.id.loginBtn);
        TextView registerLink = findViewById(R.id.registerLink);

        DBHelper db = new DBHelper(this);

        loginBtn.setOnClickListener(v -> {
            String userStr = userEdit.getText().toString().trim();
            String passStr = passEdit.getText().toString().trim();

            if (userStr.isEmpty() || passStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Models.User user = db.loginUser(userStr, passStr);
            if (user != null) {
                // ✅ FIXED: Check for "teacher" role (not "admin")
                if (user.role.equals("teacher")) {
                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                    intent.putExtra("teacherName", user.username);
                    startActivity(intent);
                } else {
                    // Student → Category screen
                    Intent intent = new Intent(this, CategoryActivity.class);
                    intent.putExtra("username", user.username);
                    startActivity(intent);
                }
                finish();
            } else {
                Toast.makeText(this, "❌ Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
