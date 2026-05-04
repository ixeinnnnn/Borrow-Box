package com.example.barrowing_system;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Forgot Password Activity with Firebase Integration
 * Sends password reset email to users
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnResetPassword;
    private TextView tvBackToLogin, tvSuccessMessage, tvErrorMessage;
    private ProgressBar progressBar;
    private View formContainer, successContainer, errorContainer;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        progressBar = findViewById(R.id.progressBar);
        formContainer = findViewById(R.id.formContainer);
        successContainer = findViewById(R.id.successContainer);
        errorContainer = findViewById(R.id.errorContainer);
    }

    private void setListeners() {
        btnResetPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (!validateEmail(email)) return;

            sendPasswordReset(email);
        });

        tvBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private boolean validateEmail(String email) {
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

        return true;
    }

    private void sendPasswordReset(String email) {
        showLoading(true);
        hideError();

        mAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener(aVoid -> {
                showLoading(false);
                showSuccess(email);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                String errorMessage = getErrorMessage(e.getMessage());
                showError(errorMessage);
            });
    }

    private String getErrorMessage(String firebaseError) {
        if (firebaseError == null) {
            return "An unknown error occurred";
        }
        if (firebaseError.contains("user-not-found")) {
            return "No account found with this email address";
        }
        if (firebaseError.contains("invalid-email")) {
            return "Invalid email address format";
        }
        if (firebaseError.contains("too-many-requests")) {
            return "Too many attempts. Please try again later";
        }
        return firebaseError;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnResetPassword.setEnabled(false);
            btnResetPassword.setText(getString(R.string.sending_reset_link));
        } else {
            progressBar.setVisibility(View.GONE);
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText(getString(R.string.send_reset_link));
        }
    }

    private void showSuccess(String email) {
        formContainer.setVisibility(View.GONE);
        successContainer.setVisibility(View.VISIBLE);
        tvSuccessMessage.setText(getString(R.string.reset_link_sent, email));

        tvBackToLogin.postDelayed(() -> finish(), 3000);
    }

    private void showError(String message) {
        errorContainer.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
    }

    private void hideError() {
        errorContainer.setVisibility(View.GONE);
    }
}
