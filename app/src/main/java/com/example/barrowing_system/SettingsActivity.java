package com.example.barrowing_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvAdminName, tvAdminEmail, tvAdminInitial, tvUserRole;
    private LinearLayout btnLogout, btnAbout;
    private SwitchCompat switchDarkMode;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

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

        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        // Toolbar back button
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadAdminProfile();
        loadDarkModePreference();
        setListeners();
    }

    private void initViews() {
        tvAdminName     = findViewById(R.id.tvAdminName);
        tvAdminEmail    = findViewById(R.id.tvAdminEmail);
        tvAdminInitial  = findViewById(R.id.tvAdminInitial);
        tvUserRole      = findViewById(R.id.tvUserRole);
        btnLogout       = findViewById(R.id.btnLogout);
        btnAbout        = findViewById(R.id.btnAbout);
        switchDarkMode  = findViewById(R.id.switchDarkMode);
    }

    // ── Load admin name and email from Firestore ──────────
    private void loadAdminProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Show email immediately while Firestore loads
        String email = user.getEmail();
        if (email != null) tvAdminEmail.setText(email);

        // Fetch full name and role from /users collection
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("fullName");
                        String role = doc.getString("role");

                        if (fullName != null && !fullName.isEmpty()) {
                            tvAdminName.setText(fullName);
                            tvAdminInitial.setText(
                                    String.valueOf(fullName.charAt(0)).toUpperCase());
                        }

                        // Display the actual role from database
                        if (role != null && !role.isEmpty()) {
                            tvUserRole.setText(role.equals("admin") ? "Admin" : "Resident");
                        } else {
                            tvUserRole.setText("Resident"); // Default to Resident if role is not set
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    // ── Load Dark Mode preference ───────────────────────────
    private void loadDarkModePreference() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        
        // Also verify current mode using AppCompatDelegate.getDefaultNightMode() to keep switch in sync
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        boolean isCurrentlyDark = currentNightMode == AppCompatDelegate.MODE_NIGHT_YES;
        
        switchDarkMode.setChecked(isDarkMode || isCurrentlyDark);

        // Apply the saved dark mode setting
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // ── Button listeners ──────────────────────────────────
    private void setListeners() {

        // Logout — show confirmation dialog first
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Dark Mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // About
        btnAbout.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Barangay Resources")
                        .setMessage("Community Borrowing System\nVersion 1.0.0\n\nA school project for managing barangay resource borrowing.")
                        .setPositiveButton("OK", null)
                        .show());

        // Role fix - click on role badge to fix incorrect role
        tvUserRole.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;
            
            // Update role to resident in database
            db.collection("users").document(user.getUid())
                    .update("role", "resident")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Role fixed to Resident. Please log out and log back in.", Toast.LENGTH_LONG).show();
                        tvUserRole.setText("Resident");
                        loadAdminProfile(); // Reload profile to reflect changes
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fix role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    // ── Logout confirmation dialog ────────────────────────
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        SessionManager.clearSession(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}