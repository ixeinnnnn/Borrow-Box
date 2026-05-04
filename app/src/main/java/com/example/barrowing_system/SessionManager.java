package com.example.barrowing_system;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Session Manager class for handling user sessions
 * Stores user session data locally using SharedPreferences
 */
public class SessionManager {
    private static final String PREF_NAME = "BarrowingSystemSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_SESSION_CREATED = "sessionCreated";
    private SharedPreferences pref;
    private Editor editor;
    private Context context;
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * @param userId Firebase Auth UID
     * @param userEmail User's email
     * @param userName User's name
     * @param userRole User's role (admin/user)
     */
    public void createLoginSession(String userId, String userEmail, String userName, String userRole) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_ROLE, userRole);
        editor.putLong(KEY_SESSION_CREATED, System.currentTimeMillis());
        editor.commit();
    }

    /**
     * Check if user is logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get user ID
     * @return User ID or null if not logged in
     */
    public String getUserId() {
        if (isLoggedIn()) {
            return pref.getString(KEY_USER_ID, null);
        }
        return null;
    }

    /**
     * Get user email
     * @return User email or null if not logged in
     */
    public String getUserEmail() {
        if (isLoggedIn()) {
            return pref.getString(KEY_USER_EMAIL, null);
        }
        return null;
    }

    /**
     * Get user name
     * @return User name or null if not logged in
     */
    public String getUserName() {
        if (isLoggedIn()) {
            return pref.getString(KEY_USER_NAME, null);
        }
        return null;
    }

    /**
     * Get user role
     * @return User role or null if not logged in
     */
    public String getUserRole() {
        if (isLoggedIn()) {
            return pref.getString(KEY_USER_ROLE, null);
        }
        return null;
    }

    /**
     * Check if current user is admin
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return "admin".equals(getUserRole());
    }

    /**
     * Check if current user is regular user
     * @return true if user is regular user, false otherwise
     */
    public boolean isRegularUser() {
        return "user".equals(getUserRole());
    }

    /**
     * Get session creation time
     * @return Session creation timestamp
     */
    public long getSessionCreated() {
        return pref.getLong(KEY_SESSION_CREATED, 0);
    }

    /**
     * Update user name in session
     * @param userName New user name
     */
    public void updateUserName(String userName) {
        if (isLoggedIn()) {
            editor.putString(KEY_USER_NAME, userName);
            editor.commit();
        }
    }

    /**
     * Update user role in session
     * @param userRole New user role
     */
    public void updateUserRole(String userRole) {
        if (isLoggedIn()) {
            editor.putString(KEY_USER_ROLE, userRole);
            editor.commit();
        }
    }

    /**
     * Clear session data (logout)
     */
    public void clearSession() {
        editor.clear();
        editor.commit();
    }

    /**
     * Clear session data (static method for convenience)
     * @param context Application context
     */
    public static void clearSession(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        sessionManager.clearSession();
    }

    /**
     * Get session details as string (for debugging)
     * @return Session details
     */
    public String getSessionDetails() {
        if (isLoggedIn()) {
            return "Session Details:\n" +
                   "User ID: " + getUserId() + "\n" +
                   "Email: " + getUserEmail() + "\n" +
                   "Name: " + getUserName() + "\n" +
                   "Role: " + getUserRole() + "\n" +
                   "Session Created: " + getSessionCreated();
        }
        return "No active session";
    }

    /**
     * Check if session is expired (optional - implement session timeout)
     * @param maxSessionDuration Maximum session duration in milliseconds
     * @return true if session is expired, false otherwise
     */
    public boolean isSessionExpired(long maxSessionDuration) {
        if (!isLoggedIn()) {
            return true;
        }
        
        long sessionCreated = getSessionCreated();
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - sessionCreated) > maxSessionDuration;
    }

    /**
     * Refresh session timestamp
     */
    public void refreshSession() {
        if (isLoggedIn()) {
            editor.putLong(KEY_SESSION_CREATED, System.currentTimeMillis());
            editor.commit();
        }
    }

    /**
     * Get all session keys and values (for debugging)
     * @return String representation of all session data
     */
    public String getAllSessionData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Session Data:\n");
        sb.append("Is Logged In: ").append(isLoggedIn()).append("\n");
        sb.append("User ID: ").append(getUserId()).append("\n");
        sb.append("User Email: ").append(getUserEmail()).append("\n");
        sb.append("User Name: ").append(getUserName()).append("\n");
        sb.append("User Role: ").append(getUserRole()).append("\n");
        sb.append("Session Created: ").append(getSessionCreated()).append("\n");
        return sb.toString();
    }
}
