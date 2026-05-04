package com.example.barrowing_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.barrowing_system.adapters.RecentActivityAdapter;
import com.example.barrowing_system.models.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * User Dashboard Activity
 * Provides user interface for browsing and borrowing items
 */
public class UserDashboardActivity extends AppCompatActivity {

    // Views
    private TextView tvWelcomeUser;
    private TextView tvActiveBorrowings;
    private TextView tvPendingRequests;
    private TextView tvViewAll;
    private TextView tvEmptyRecent;
    private ProgressBar progressBar;
        private ImageView btnNotification;
    private RecyclerView rvRecentActivity;
    private BottomNavigationView bottomNavigationView;

    // CardViews for quick actions
    private CardView btnBrowseItems, btnMyBorrowings, btnProfile;
    private Button btnLogout;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Firestore listeners (must be detached in onDestroy)
    private ListenerRegistration requestsListener;
    private ListenerRegistration recentListener;
    private ListenerRegistration userListener;

    // Adapter
    private RecentActivityAdapter recentActivityAdapter;

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

        setContentView(R.layout.activity_user_dashboard);

        // Firebase init with error handling
        try {
            mAuth       = FirebaseAuth.getInstance();
            db          = FirebaseFirestore.getInstance();
            currentUser = mAuth.getCurrentUser();
        } catch (Exception e) {
            android.util.Log.e("UserDashboard", "Firebase init error: " + e.getMessage());
            // Handle Firebase initialization failure
            Toast.makeText(this, "Firebase services unavailable. Please check your connection.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }

        // Guard: if not logged in, go back to login
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        initViews();
        setListeners();
        loadUserName();
        loadStatCards();
        loadRecentRequests();
    }

    // ─────────────────────────────────────────────
    // View binding
    // ─────────────────────────────────────────────
    private void initViews() {
        tvWelcomeUser      = findViewById(R.id.tvWelcomeUser);
        tvActiveBorrowings = findViewById(R.id.tvActiveBorrowings);
        tvPendingRequests  = findViewById(R.id.tvPendingRequests);
        tvViewAll          = findViewById(R.id.tvViewAll);
        tvEmptyRecent      = findViewById(R.id.tvEmptyRecent);
        progressBar        = findViewById(R.id.progressBar);
                btnNotification    = findViewById(R.id.btnNotification);
        rvRecentActivity   = findViewById(R.id.rvRecentActivity);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        btnBrowseItems     = findViewById(R.id.btnBrowseItems);
        btnMyBorrowings    = findViewById(R.id.btnMyBorrowings);
        btnProfile         = findViewById(R.id.btnProfile);
        btnLogout          = findViewById(R.id.btnLogout);

        // Setup RecyclerView
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        recentActivityAdapter = new RecentActivityAdapter(this, new ArrayList<>());
        rvRecentActivity.setAdapter(recentActivityAdapter);

        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    // ─────────────────────────────────────────────
    // Button listeners
    // ─────────────────────────────────────────────
    private void setListeners() {
        
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
        });

        btnBrowseItems.setOnClickListener(v -> {
            Intent intent = new Intent(this, BorrowActivity.class);
            startActivity(intent);
        });

        btnMyBorrowings.setOnClickListener(v -> {
            Intent intent = new Intent(this, RequestsActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, RequestsActivity.class);
            intent.putExtra("filter", "history");
            startActivity(intent);
        });

        tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(this, RequestsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());
        
        // Temporary fix button - remove after fixing role
        tvWelcomeUser.setOnClickListener(v -> {
            FixUserRole.fixCurrentUserRole();
            Toast.makeText(this, "Role fix attempted. Please log out and log back in.", Toast.LENGTH_LONG).show();
        });
    }

    // ─────────────────────────────────────────────
    // Bottom Navigation Setup
    // ─────────────────────────────────────────────
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                // Already on home, do nothing
                return true;
            } else if (itemId == R.id.nav_borrow) {
                Intent intent = new Intent(this, BorrowActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemId == R.id.nav_requests) {
                Intent intent = new Intent(this, RequestsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return false;
        });
    }

    // ─────────────────────────────────────────────
    // Load user's full name from Firestore /users
    // ─────────────────────────────────────────────
    private void loadUserName() {
        try {
            String uid = currentUser.getUid();

            userListener = db.collection("users")
                    .document(uid)
                    .addSnapshotListener((snapshot, error) -> {
                        try {
                            if (error != null) {
                                // Suppress Google Play Services related errors
                                if (error.getMessage() != null && 
                                    error.getMessage().contains("Failed to get service from broker")) {
                                    Log.w("UserDashboard", "Suppressed Google Play Services error in loadUserName");
                                    setWelcomeFromEmail();
                                    return;
                                }
                                Log.e("UserDashboard", "Firestore error in loadUserName: " + error.getMessage());
                                setWelcomeFromEmail();
                                return;
                            }
                            if (snapshot != null && snapshot.exists()) {
                                String fullName = snapshot.getString("fullName");
                                if (fullName != null && !fullName.isEmpty()) {
                                    tvWelcomeUser.setText("Welcome, " + fullName + "!");
                                } else {
                                    setWelcomeFromEmail();
                                }
                            } else {
                                setWelcomeFromEmail();
                            }
                        } catch (Exception e) {
                            Log.w("UserDashboard", "Exception in loadUserName listener: " + e.getMessage());
                            setWelcomeFromEmail();
                        }
                    });
        } catch (Exception e) {
            Log.w("UserDashboard", "Failed to setup loadUserName listener: " + e.getMessage());
            setWelcomeFromEmail();
        }
    }

    private void setWelcomeFromEmail() {
        String email = currentUser.getEmail();
        if (email != null && email.contains("@")) {
            String name = email.substring(0, email.indexOf("@"));
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            tvWelcomeUser.setText("Welcome, " + name + "!");
        } else {
            tvWelcomeUser.setText("Welcome Back!");
        }
    }

    // ─────────────────────────────────────────────
    // Load stat cards: Active Requests + Completed
    // ─────────────────────────────────────────────
    private void loadStatCards() {
        String uid = currentUser.getUid();

        requestsListener = db.collection("requests")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this,
                                "Failed to load stats: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots == null) return;

                    int active    = 0;
                    int completed = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String status = doc.getString("status");
                        if (status == null) continue;
                        if (status.equals("Pending") || status.equals("Approved")) active++;
                        if (status.equals("Returned") || status.equals("Completed")) completed++;
                    }

                    if (tvActiveBorrowings != null)
                        tvActiveBorrowings.setText(String.valueOf(active));
                    if (tvPendingRequests != null)
                        tvPendingRequests.setText(String.valueOf(completed));
                });
    }

    // ─────────────────────────────────────────────
    // Load 5 most recent requests for this user
    // ─────────────────────────────────────────────
    private void loadRecentRequests() {
        progressBar.setVisibility(View.VISIBLE);
        rvRecentActivity.setVisibility(View.GONE);
        tvEmptyRecent.setVisibility(View.GONE);

        String uid = currentUser.getUid();

        recentListener = db.collection("requests")
                .whereEqualTo("userId", uid)
                .orderBy("borrowDate", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((snapshots, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this,
                                "Failed to load requests: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null) return;

                    List<Request> requests = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Request request = new Request(
                                doc.getId(),
                                doc.getString("requesterName"),
                                doc.getString("requesterEmail"),
                                doc.getString("itemName"),
                                doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 0,
                                doc.getString("borrowDate"),
                                doc.getString("returnDate"),
                                doc.getString("purpose"),
                                doc.getString("status")
                        );
                        requests.add(request);
                    }

                    recentActivityAdapter.updateData(requests);

                    if (requests.isEmpty()) {
                        rvRecentActivity.setVisibility(View.GONE);
                        tvEmptyRecent.setVisibility(View.VISIBLE);
                    } else {
                        rvRecentActivity.setVisibility(View.VISIBLE);
                        tvEmptyRecent.setVisibility(View.GONE);
                    }
                });
    }

    // ─────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────
    private void logout() {
        mAuth.signOut();
        SessionManager.clearSession(this);
        navigateToLogin();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // ─────────────────────────────────────────────
    // Navigate to login and clear back stack
    // ─────────────────────────────────────────────
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ─────────────────────────────────────────────
    // Handle activity lifecycle transitions
    // ─────────────────────────────────────────────
    @Override
    protected void onPause() {
        try {
            super.onPause();
        } catch (Exception e) {
            android.util.Log.e("UserDashboard", "onPause error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Detach all Firestore listeners to prevent memory leaks
    // ─────────────────────────────────────────────
    @Override
    protected void onDestroy() {
        try {
            // Clean up Firebase listeners to prevent memory leaks
            if (requestsListener != null) {
                requestsListener.remove();
                requestsListener = null;
            }
            if (recentListener != null) {
                recentListener.remove();
                recentListener = null;
            }
            if (userListener != null) {
                userListener.remove();
                userListener = null;
            }
            super.onDestroy();
        } catch (Exception e) {
            android.util.Log.e("UserDashboard", "onDestroy error: " + e.getMessage());
            super.onDestroy();
        }
    }

    // ─────────────────────────────────────────────
    // Guard: redirect if session expired
    // ─────────────────────────────────────────────
    @Override
    protected void onResume() {
        try {
            super.onResume();
            // Check if user is still authenticated
            if (mAuth != null && mAuth.getCurrentUser() == null) {
                navigateToLogin();
            }
        } catch (Exception e) {
            // Handle any lifecycle exceptions gracefully
            android.util.Log.e("UserDashboard", "onResume error: " + e.getMessage());
            // If there's a critical error, navigate to login
            if (mAuth == null || mAuth.getCurrentUser() == null) {
                navigateToLogin();
            }
        }
    }
}