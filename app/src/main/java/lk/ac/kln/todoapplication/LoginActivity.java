package lk.ac.kln.todoapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import lk.ac.kln.todoapplication.Auth.PasswordUtils;
import lk.ac.kln.todoapplication.Utils.DatabaseHandler;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEt, passwordEt;
    private Button loginBtn;
    private TextView goToRegister;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEt = findViewById(R.id.loginUsername);
        passwordEt = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginButton);
        goToRegister = findViewById(R.id.goToRegister);

        db = new DatabaseHandler(this);
        db.openDatabase();

        loginBtn.setOnClickListener(v -> {
            String username = usernameEt.getText().toString().trim();
            String password = passwordEt.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter username & password", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHandler.UserRecord user = db.getUserByUsername(username);
            if (user == null) {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = PasswordUtils.verifyPassword(password.toCharArray(), user.passwordHash, user.salt);
            if (!ok) {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putInt("current_user_id", user.id)
                    .putString("current_username", user.username)
                    .apply();

            // Decide where to go: onboarding (first time for this user) or main
            boolean seenOnboarding = prefs.getBoolean("seen_onboarding_for_user_" + user.id, false);
            if (!seenOnboarding) {
                Intent i = new Intent(LoginActivity.this, OnboardingActivity.class);
                i.putExtra("user_id", user.id);
                startActivity(i);
            } else {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("user_id", user.id);
                startActivity(i);
            }
            finish();
        });

        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }
}
