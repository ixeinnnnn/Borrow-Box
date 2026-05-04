package com.example.barrowing_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Safe Register Activity - Basic version without crashes
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply dark mode preference as the VERY FIRST thing
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        try {
            setContentView(R.layout.activity_register);
            initViews();
            setListeners();
            Toast.makeText(this, "Register screen loaded successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading register: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            etFullName = findViewById(R.id.etFullName);
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            btnRegister = findViewById(R.id.btnRegister);
            tvBackToLogin = findViewById(R.id.tvBackToLogin);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing register views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setListeners() {
        try {
            btnRegister.setOnClickListener(v -> {
                String fullName = etFullName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (!validateInputs(fullName, email, password, confirmPassword)) return;

                performRegistration(fullName, email, password);
            });

            tvBackToLogin.setOnClickListener(v -> {
                finish();
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error setting register listeners: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInputs(String fullName, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.name_required));
            etFullName.requestFocus();
            return false;
        }

        if (fullName.length() < 3) {
            etFullName.setError(getString(R.string.name_min_length));
            etFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.email_required));
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.valid_email));
            etEmail.requestFocus();
            return false;
        }

        if (!PasswordValidator.validatePasswordAndSetError(password, this, etPassword)) {
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.confirm_password_required));
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.passwords_not_match));
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performRegistration(String fullName, String email, String password) {
        btnRegister.setEnabled(false);
        btnRegister.setText(getString(R.string.creating_account));
        Toast.makeText(this, "Registration successful for: " + fullName, Toast.LENGTH_SHORT).show();
        btnRegister.setEnabled(true);
        btnRegister.setText("Register");
        finish();
    }
}
