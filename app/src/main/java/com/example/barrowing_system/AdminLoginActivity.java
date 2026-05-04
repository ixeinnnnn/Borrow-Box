package com.example.barrowing_system;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Enhanced Admin Login Activity with Firebase Integration
 * Features:
 * - Firebase Authentication for admin login
 * - Role-based verification from Firestore
 * - Password visibility toggle
 * - Loading indicators
 * - Error handling with user feedback
 */
public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminEmail, etAdminPassword;
    private ImageView ivTogglePassword;
    private Button btnAdminSignIn;
    private TextView tvBackToLogin;
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_admin_login);
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            initViews();
            setListeners();
            checkCurrentUser();
            Toast.makeText(this, "Admin login screen loaded successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading admin login: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            etAdminEmail = findViewById(R.id.etAdminEmail);
            etAdminPassword = findViewById(R.id.etAdminPassword);
            ivTogglePassword = findViewById(R.id.ivTogglePassword);
            btnAdminSignIn = findViewById(R.id.btnAdminSignIn);
            tvBackToLogin = findViewById(R.id.tvBackToLogin);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing admin views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setListeners() {
        try {
            ivTogglePassword.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    etAdminPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivTogglePassword.setImageResource(R.drawable.ic_eye);
                    isPasswordVisible = false;
                } else {
                    etAdminPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
                    isPasswordVisible = true;
                }
                etAdminPassword.setSelection(etAdminPassword.getText().length());
            });

            btnAdminSignIn.setOnClickListener(v -> {
                String email = etAdminEmail.getText().toString().trim();
                String password = etAdminPassword.getText().toString().trim();

                if (!validateInputs(email, password)) return;

                performAdminLogin(email, password);
            });

            tvBackToLogin.setOnClickListener(v -> {
                finish();
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error setting admin listeners: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            verifyAdminRole(currentUser);
        }
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etAdminEmail.setError(getString(R.string.email_required));
            etAdminEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etAdminEmail.setError(getString(R.string.valid_email));
            etAdminEmail.requestFocus();
            return false;
        }

        if (!PasswordValidator.validatePasswordAndSetError(password, this, etAdminPassword)) {
            return false;
        }

        return true;
    }

    private void performAdminLogin(String email, String password) {
        btnAdminSignIn.setEnabled(false);
        btnAdminSignIn.setText(getString(R.string.admin_signing_in));
        
        // Disable reCAPTCHA verification for testing (remove in production)
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                if (user != null) {
                    verifyAdminRole(user);
                }
            })
            .addOnFailureListener(e -> {
                btnAdminSignIn.setEnabled(true);
                btnAdminSignIn.setText(getString(R.string.admin_sign_in));
                String errorMsg = getString(R.string.login_failed, e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("network")) {
                    errorMsg = "Network error. Please check your internet connection and try again.";
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            });
    }

    private void verifyAdminRole(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String role = document.getString("role");
                            if ("admin".equals(role)) {
                                navigateToAdminDashboard();
                            } else {
                                Toast.makeText(AdminLoginActivity.this, "Access denied: Not an admin account", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                btnAdminSignIn.setEnabled(true);
                                btnAdminSignIn.setText(getString(R.string.admin_sign_in));
                            }
                        } else {
                            if (user.getEmail().equals("admin@barangay.com")) {
                                navigateToAdminDashboard();
                            } else {
                                Toast.makeText(AdminLoginActivity.this, "Access denied: User not found", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                btnAdminSignIn.setEnabled(true);
                                btnAdminSignIn.setText(getString(R.string.admin_sign_in));
                            }
                        }
                    } else {
                        Toast.makeText(AdminLoginActivity.this, "Error verifying admin role: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        btnAdminSignIn.setEnabled(true);
                        btnAdminSignIn.setText(getString(R.string.admin_sign_in));
                    }
                }
            });
    }

    private void navigateToAdminDashboard() {
        try {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, getString(R.string.admin_login_successful), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error opening admin dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            btnAdminSignIn.setEnabled(true);
            btnAdminSignIn.setText(getString(R.string.admin_sign_in));
        }
    }
}
