package com.example.barrowing_system;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

/**
 * Reset Password Activity with Firebase Integration
 * Handles password reset from email link with oobCode
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvBackToForgot, tvSuccessMessage, tvErrorMessage;
    private ProgressBar progressBar;
    private View formContainer, successContainer, errorContainer;

    private String oobCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        FirebaseHelper.initialize(this);

        extractOobCode();
        initViews();
        setListeners();
    }

    private void extractOobCode() {
        Intent intent = getIntent();
        oobCode = intent.getStringExtra("oobCode");

        if (oobCode == null || oobCode.isEmpty()) {
            oobCode = intent.getData().getQueryParameter("oobCode");
        }

        if (oobCode == null || oobCode.isEmpty()) {
            showError("This reset link is invalid or has expired. Please request a new one.");
            formContainer.setVisibility(View.GONE);
            errorContainer.setVisibility(View.VISIBLE);
        } else {
            verifyOobCode();
        }
    }

    private void verifyOobCode() {
        FirebaseHelper.verifyPasswordResetCode(oobCode, (success, message) -> {
            if (!success) {
                showError(message);
                formContainer.setVisibility(View.GONE);
                errorContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToForgot = findViewById(R.id.tvBackToForgot);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        progressBar = findViewById(R.id.progressBar);
        formContainer = findViewById(R.id.formContainer);
        successContainer = findViewById(R.id.successContainer);
        errorContainer = findViewById(R.id.errorContainer);
    }

    private void setListeners() {
        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (!validatePassword(newPassword, confirmPassword)) return;

            resetPassword(newPassword);
        });

        tvBackToForgot.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validatePassword(String password, String confirmPassword) {
        if (TextUtils.isEmpty(password)) {
            etNewPassword.setError(getString(R.string.password_required));
            etNewPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            etNewPassword.setError("Password must be at least 8 characters");
            etNewPassword.requestFocus();
            return false;
        }

        if (!PasswordValidator.validatePasswordAndSetError(password, this, etNewPassword)) {
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

    private void resetPassword(String newPassword) {
        showLoading(true);
        hideError();

        FirebaseHelper.confirmPasswordReset(oobCode, newPassword, (success, message) -> {
            showLoading(false);

            if (success) {
                showSuccess();
            } else {
                showError(message);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnResetPassword.setEnabled(false);
            btnResetPassword.setText(getString(R.string.resetting_password));
        } else {
            progressBar.setVisibility(View.GONE);
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText(getString(R.string.reset_password));
        }
    }

    private void showSuccess() {
        formContainer.setVisibility(View.GONE);
        successContainer.setVisibility(View.VISIBLE);
        tvSuccessMessage.setText(getString(R.string.password_reset_success));

        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(this, LoginActivityNew.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 3000);
    }

    private void showError(String message) {
        errorContainer.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
    }

    private void hideError() {
        errorContainer.setVisibility(View.GONE);
    }
}
