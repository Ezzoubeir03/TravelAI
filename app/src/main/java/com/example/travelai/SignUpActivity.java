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

public class SignUpActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirm;
    private EditText etName, etEmail, etPassword, etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirm = findViewById(R.id.til_confirm);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.tv_login).setOnClickListener(v -> finish());
        findViewById(R.id.btn_signup).setOnClickListener(v -> attemptSignUp());

        etConfirm.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSignUp();
                return true;
            }
            return false;
        });
    }

    private void attemptSignUp() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);

        boolean ok = true;
        if (name.length() < 2) {
            tilName.setError("Enter your name");
            ok = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            ok = false;
        }
        if (pass.length() < 6) {
            tilPassword.setError("At least 6 characters");
            ok = false;
        }
        if (!pass.equals(confirm)) {
            tilConfirm.setError("Passwords do not match");
            ok = false;
        }
        if (!ok) return;

        long id = db.register(name, email, pass);
        if (id == DatabaseHelper.ERR_EMAIL_EXISTS) {
            tilEmail.setError("This email is already registered");
            return;
        }
        if (id < 0) {
            Toast.makeText(this, "Could not create account. Try again.", Toast.LENGTH_LONG).show();
            return;
        }

        session.login(id);
        Toast.makeText(this, "Account created. Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}