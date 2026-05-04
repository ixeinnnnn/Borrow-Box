package com.example.barrowing_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main Activity - Entry point for authenticated users
 * Redirects to appropriate dashboard based on user role
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        currentUser = mAuth.getCurrentUser();
        checkAuthenticationAndRedirect();
    }

    private void checkAuthenticationAndRedirect() {
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            mAuth.signOut();
            navigateToLogin();
            return;
        }

        String userRole = sessionManager.getUserRole();
        if ("admin".equals(userRole)) {
            navigateToAdminDashboard();
        } else {
            navigateToUserDashboard();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToUserDashboard() {
        Intent intent = new Intent(this, UserDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (mAuth.getCurrentUser() == null || !sessionManager.isLoggedIn()) {
            navigateToLogin();
        }
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            if (extras.containsKey("password_reset")) {
                Intent resetIntent = new Intent(this, ForgotPasswordActivity.class);
                startActivity(resetIntent);
                finish();
            }
            if (extras.containsKey("borrowing_request")) {
                Toast.makeText(this, "Borrowing request received", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showErrorAndLogout(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        
        mAuth.signOut();
        sessionManager.clearSession();
        navigateToLogin();
    }

    private boolean isSessionValid() {
        if (currentUser == null) {
            return false;
        }
        
        if (!sessionManager.isLoggedIn()) {
            return false;
        }
        
        String sessionUserId = sessionManager.getUserId();
        String firebaseUserId = currentUser.getUid();
        return sessionUserId != null && sessionUserId.equals(firebaseUserId);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
