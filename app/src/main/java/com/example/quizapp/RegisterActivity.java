package com.example.quizapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextInputEditText userEdit = findViewById(R.id.username);
        TextInputEditText passEdit = findViewById(R.id.password);
        MaterialButton regBtn = findViewById(R.id.registerBtn);
        TextView loginLink = findViewById(R.id.loginLink);

        DBHelper db = new DBHelper(this);

        android.widget.RadioGroup roleGroup = findViewById(R.id.roleGroup);

        regBtn.setOnClickListener(v -> {
            String user = userEdit.getText().toString().trim();
            String pass = passEdit.getText().toString().trim();

            if (user.isEmpty() || pass.length() < 6) {
                Toast.makeText(this, "Valid username and 6+ char password required", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = (roleGroup.getCheckedRadioButtonId() == R.id.radioTeacher) ? "teacher" : "student";

            if (db.registerUser(user, pass, role)) {
                Toast.makeText(this, "Registration Successful as " + role, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            }
        });

        loginLink.setOnClickListener(v -> finish());
    }
}
