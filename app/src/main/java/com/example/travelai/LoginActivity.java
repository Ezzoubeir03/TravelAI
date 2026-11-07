package com.example.travelai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private TextInputLayout tilEmail, tilPassword;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);
        db = new DatabaseHelper(this);

        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        findViewById(R.id.btn_login).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tv_signup).setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();

        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean ok = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            ok = false;
        }
        if (pass.isEmpty()) {
            tilPassword.setError("Enter your password");
            ok = false;
        }
        if (!ok) return;

        User user = db.login(email, pass);
        if (user == null) {
            tilPassword.setError("Wrong email or password");
            return;
        }

        session.login(user.id);
        Toast.makeText(this, "Welcome back, " + user.name + "!", Toast.LENGTH_SHORT).show();
        goToMain();
    }

    private void goToMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}