package lk.ac.kln.todoapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.passay.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import lk.ac.kln.todoapplication.Auth.PasswordUtils;
import lk.ac.kln.todoapplication.Utils.DatabaseHandler;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEt, passwordEt, confirmEt;
    private Button registerBtn;
    private TextView goToLogin;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEt = findViewById(R.id.registerUsername);
        passwordEt = findViewById(R.id.registerPassword);
        confirmEt = findViewById(R.id.registerConfirmPassword);
        registerBtn = findViewById(R.id.registerButton);
        goToLogin = findViewById(R.id.goToLogin);

        db = new DatabaseHandler(this);
        db.openDatabase();

        registerBtn.setEnabled(false);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFormState();
            }
        };

        usernameEt.addTextChangedListener(watcher);
        passwordEt.addTextChangedListener(watcher);
        confirmEt.addTextChangedListener(watcher);

        registerBtn.setOnClickListener(v -> attemptRegister());

        goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void updateFormState() {
        String username = usernameEt.getText().toString().trim();
        String password = passwordEt.getText().toString();
        String confirm = confirmEt.getText().toString();

        boolean basicFilled = !username.isEmpty() && !password.isEmpty() && !confirm.isEmpty();
        registerBtn.setEnabled(basicFilled);
    }

    private void attemptRegister() {
        String username = usernameEt.getText().toString().trim();
        String password = passwordEt.getText().toString();
        String confirm = confirmEt.getText().toString();

        if (username.isEmpty()) {
            usernameEt.setError("Enter username");
            usernameEt.requestFocus();
            return;
        }

        if (username.length() < 3) {
            usernameEt.setError("Username must be at least 3 characters");
            usernameEt.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEt.setError("Enter password");
            passwordEt.requestFocus();
            return;
        }

        if (!password.equals(confirm)) {
            confirmEt.setError("Passwords do not match");
            confirmEt.requestFocus();
            return;
        }

        String requirementError = passwordRequirementError(password);
        if (requirementError != null) {
            passwordEt.setError(requirementError);
            passwordEt.requestFocus();
            return;
        }

        if (db.isUsernameTaken(username)) {
            usernameEt.setError("Username already taken");
            usernameEt.requestFocus();
            return;
        }

        byte[] salt = PasswordUtils.generateSalt();
        String saltStr = PasswordUtils.saltToString(salt);
        String hash = PasswordUtils.hashPassword(password.toCharArray(), salt);

        long userId = db.insertUser(username, hash, saltStr);
        if (userId == -1) {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Persist session
        SharedPreferences prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE);
        prefs.edit()
                .putInt("current_user_id", (int) userId)
                .putString("current_username", username)
                .apply();

        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();

        // After registration, check onboarding seen flag (per user)
        boolean seenOnboarding = prefs.getBoolean("seen_onboarding_for_user_" + userId, false);
        if (!seenOnboarding) {
            Intent i = new Intent(RegisterActivity.this, OnboardingActivity.class);
            i.putExtra("user_id", (int) userId);
            startActivity(i);
        } else {
            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
            i.putExtra("user_id", (int) userId);
            startActivity(i);
        }
        finish();
    }

    private String passwordRequirementError(String pwd) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 128),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()
        ));

        RuleResult result = validator.validate(new PasswordData(pwd));
        if (result.isValid()) {
            return null;
        }

        return String.join(", ", validator.getMessages(result));
    }
}
