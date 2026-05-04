package com.example.barrowing_system;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Enhanced Login Activity with Firebase Integration
 * Features:
 * - Firebase Authentication for user login
 * - Role-based navigation (Admin/User)
 * - Password visibility toggle
 * - Loading indicators
 * - Session management
 * - Error handling with user feedback
 */
public class LoginActivityNew extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvRegister, tvForgotPassword;
    private ImageView ivTogglePassword;
    private CardView ivAdminLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_login);
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            initViews();
            setListeners();
            checkCurrentUser();
            Toast.makeText(this, "Login screen loaded successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading login: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            btnSignIn = findViewById(R.id.btnSignIn);
            tvRegister = findViewById(R.id.tvRegister);
            tvForgotPassword = findViewById(R.id.tvForgotPassword);
            ivTogglePassword = findViewById(R.id.ivTogglePassword);
            ivAdminLogin = findViewById(R.id.btnAdmin);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing login views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setListeners() {
        try {
            btnSignIn.setOnClickListener(v -> {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (!validateInputs(email, password)) {
                    return;
                }

                performLogin(email, password);
            });

            tvRegister.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, RegisterActivityNew.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error opening register: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            tvForgotPassword.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, ForgotPasswordActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error opening forgot password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            ivAdminLogin.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, AdminLoginActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error opening admin login: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        } catch (Exception e) {
            Toast.makeText(this, "Error setting login listeners: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getUserRoleAndNavigate(currentUser);
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye);
        } else {
            etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private boolean validateInputs(String email, String password) {
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

        return true;
    }

    private void performLogin(String email, String password) {
        btnSignIn.setEnabled(false);
        btnSignIn.setText(getString(R.string.signing_in));
        
        // Disable reCAPTCHA verification for testing (remove in production)
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                if (user != null) {
                    getUserRoleAndNavigate(user);
                }
            })
            .addOnFailureListener(e -> {
                btnSignIn.setEnabled(true);
                btnSignIn.setText(getString(R.string.sign_in));
                String errorMsg = getString(R.string.login_failed, e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("network")) {
                    errorMsg = "Network error. Please check your internet connection and try again.";
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            });
    }

    private void getUserRoleAndNavigate(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String role = document.getString("role");
                            navigateBasedOnRole(role, user);
                        } else {
                            if (user.getEmail().equals("admin@barangay.com")) {
                                navigateToAdminDashboard();
                            } else {
                                navigateToUserDashboard();
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivityNew.this, "Error checking user role: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        navigateToUserDashboard();
                    }
                }
            });
    }

    private void navigateBasedOnRole(String role, FirebaseUser user) {
        if ("admin".equals(role)) {
            navigateToAdminDashboard();
        } else {
            navigateToUserDashboard();
        }
    }

    private void navigateToUserDashboard() {
        try {
            Toast.makeText(this, "Navigating to User Dashboard...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, UserDashboardActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error opening user dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            btnSignIn.setEnabled(true);
            btnSignIn.setText(getString(R.string.sign_in));
        }
    }

    private void navigateToAdminDashboard() {
        try {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Welcome to Admin Dashboard", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error opening admin dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            btnSignIn.setEnabled(true);
            btnSignIn.setText(getString(R.string.sign_in));
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
