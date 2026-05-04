package com.example.barrowing_system;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase Helper Class
 * Manages Firebase Firestore connections and common operations
 */
public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static FirebaseAuth mAuth;
    private static FirebaseFirestore db;
    private static Context mContext;

    /**
     * Initialize Firebase instances
     * @param context Application context
     */
    public static void initialize(Context context) {
        mContext = context.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Firebase initialized");
    }

    /**
     * Get Firebase Auth instance
     * @return FirebaseAuth instance
     */
    public static FirebaseAuth getAuth() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    /**
     * Get Firestore instance
     * @return FirebaseFirestore instance
     */
    public static FirebaseFirestore getDatabase() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }

    /**
     * Get current user
     * @return Current Firebase user or null
     */
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    /**
     * Check if user is logged in
     * @return true if user is logged in
     */
    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Save user to database
     * @param user User object to save
     * @param callback Operation callback
     */
    public static void saveUser(User user, DatabaseOperationCallback callback) {
        if (user == null || user.getUid() == null) {
            callback.onOperationComplete(false, "Invalid user data");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", user.getFullName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhoneNumber()); // FIXED: was user.getPhone()
        userData.put("address", user.getAddress());
        userData.put("role", user.getRole());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved successfully: " + user.getUid());
                    callback.onOperationComplete(true, "User saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user: " + e.getMessage());
                    callback.onOperationComplete(false, e.getMessage());
                });
    }

    /**
     * Get user by ID
     * @param userId User ID
     * @param callback User data callback
     */
    public static void getUser(String userId, UserDataCallback callback) {
        if (userId == null) {
            callback.onUserDataReceived(null, "Invalid user ID");
            return;
        }

        db.collection("users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                User user = new User(
                                        document.getId(),
                                        document.getString("fullName"),
                                        document.getString("email"),
                                        document.getString("role"),
                                        document.getString("phone"),
                                        document.getString("address")
                                );
                                callback.onUserDataReceived(user, null);
                            } else {
                                callback.onUserDataReceived(null, "User not found");
                            }
                        } else {
                            callback.onUserDataReceived(null, task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Show error message
     * @param message Error message to show
     */
    public static void showError(String message) {
        if (mContext != null) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, message);
    }

    /**
     * Show success message
     * @param message Success message to show
     */
    public static void showSuccess(String message) {
        if (mContext != null) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, message);
    }

    /**
     * Callback interface for database operations
     */
    public interface DatabaseOperationCallback {
        void onOperationComplete(boolean success, String message);
    }

    /**
     * Callback interface for user data retrieval
     */
    public interface UserDataCallback {
        void onUserDataReceived(User user, String error);
    }

    /**
     * Send password reset email
     * @param email User's email address
     * @param callback Operation callback
     */
    public static void sendPasswordReset(String email, DatabaseOperationCallback callback) {
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(
                        mContext.getPackageName(),
                        false,
                        null
                )
                .setHandleCodeInApp(true)
                .setUrl("https://juanezaian-58a6b03c.firebaseapp.com/__/auth/action")
                .build();

        getAuth().sendPasswordResetEmail(email, actionCodeSettings)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Password reset email sent to: " + email);
                    callback.onOperationComplete(true, "Password reset email sent successfully");
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mapPasswordResetError(e.getMessage());
                    Log.e(TAG, "Failed to send password reset email: " + e.getMessage());
                    callback.onOperationComplete(false, errorMessage);
                });
    }

    /**
     * Confirm password reset with oobCode
     * @param oobCode One-time code from email link
     * @param newPassword New password
     * @param callback Operation callback
     */
    public static void confirmPasswordReset(String oobCode, String newPassword, DatabaseOperationCallback callback) {
        getAuth().confirmPasswordReset(oobCode, newPassword)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Password reset confirmed successfully");
                    callback.onOperationComplete(true, "Password has been reset successfully");
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mapPasswordResetError(e.getMessage());
                    Log.e(TAG, "Failed to confirm password reset: " + e.getMessage());
                    callback.onOperationComplete(false, errorMessage);
                });
    }

    /**
     * Verify password reset code
     * @param oobCode One-time code from email link
     * @param callback Operation callback
     */
    public static void verifyPasswordResetCode(String oobCode, DatabaseOperationCallback callback) {
        getAuth().verifyPasswordResetCode(oobCode)
                .addOnSuccessListener(email -> {
                    Log.d(TAG, "Password reset code verified for: " + email);
                    callback.onOperationComplete(true, email);
                })
                .addOnFailureListener(e -> {
                    String errorMessage = mapPasswordResetError(e.getMessage());
                    Log.e(TAG, "Failed to verify password reset code: " + e.getMessage());
                    callback.onOperationComplete(false, errorMessage);
                });
    }

    /**
     * Map Firebase error codes to human-readable messages
     * @param errorMessage Firebase error message
     * @return Human-readable error message
     */
    private static String mapPasswordResetError(String errorMessage) {
        if (errorMessage == null) {
            return "An unknown error occurred";
        }

        if (errorMessage.contains("user-not-found")) {
            return "No account found with this email address";
        }
        if (errorMessage.contains("invalid-email")) {
            return "Invalid email address format";
        }
        if (errorMessage.contains("expired-action-code")) {
            return "This reset link has expired. Please request a new one";
        }
        if (errorMessage.contains("invalid-action-code")) {
            return "This reset link is invalid. Please request a new one";
        }
        if (errorMessage.contains("weak-password")) {
            return "Password is too weak. Please use a stronger password";
        }
        if (errorMessage.contains("too-many-requests")) {
            return "Too many attempts. Please try again later";
        }

        return errorMessage;
    }
}