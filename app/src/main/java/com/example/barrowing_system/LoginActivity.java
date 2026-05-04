package com.example.barrowing_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView ivTogglePassword;
    private Button btnSignIn;
    private CardView btnGoogle, btnAdmin;
    private TextView tvForgotPassword, tvRegister;
    private ProgressBar progressBar;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply dark mode preference
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnAdmin = findViewById(R.id.btnAdmin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setListeners() {
        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivTogglePassword.setImageResource(R.drawable.ic_eye);
                isPasswordVisible = false;
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                etEmail.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                etPassword.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (!validateInputs(email, password)) return;
            signInUser(email, password);
        });

        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show();
        });

        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email");
                etEmail.requestFocus();
                return;
            }
            sendPasswordResetEmail(email);
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void signInUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        btnSignIn.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                fetchUserRole(user.getUid());
                            }
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserRole(String uid) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(uid).get()
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String role = document.getString("role");
                            if (role == null || role.isEmpty()) {
                                Toast.makeText(LoginActivity.this,
                                        "Account role not set. Contact admin.",
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                return;
                            }

                            Intent intent;
                            if ("admin".equals(role)) {
                                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                            } else if ("resident".equals(role)) {
                                intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Unknown role: " + role,
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                return;
                            }

                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "User document not found. Contact admin.",
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Error fetching user data: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (!PasswordValidator.validatePasswordAndSetError(password, this, etPassword)) {
            return false;
        }

        return true;
    }
}
