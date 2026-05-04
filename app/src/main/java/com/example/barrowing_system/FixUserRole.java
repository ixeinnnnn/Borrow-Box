package com.example.barrowing_system;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Temporary utility class to fix user role from "admin" to "resident"
 * Run this once to fix the incorrectly tagged user account
 */
public class FixUserRole {
    
    public static void fixCurrentUserRole() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (currentUser == null) {
            Log.e("FixUserRole", "No user logged in");
            return;
        }
        
        String uid = currentUser.getUid();
        Log.d("FixUserRole", "Fixing role for user: " + uid);
        
        // Update the user's role to "resident"
        db.collection("users").document(uid)
                .update("role", "resident")
                .addOnSuccessListener(aVoid -> {
                    Log.d("FixUserRole", "Successfully updated user role to resident");
                })
                .addOnFailureListener(e -> {
                    Log.e("FixUserRole", "Failed to update user role: " + e.getMessage());
                });
    }
}
