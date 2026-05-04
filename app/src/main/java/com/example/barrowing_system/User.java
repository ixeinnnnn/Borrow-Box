package com.example.barrowing_system;

/**
 * User model class for Firebase Firestore
 * Represents a user in the barangay borrowing system
 */
public class User {

    private String uid;
    private String fullName;
    private String email;
    private String role;
    private String phoneNumber;
    private String address;
    private long createdAt;
    private long lastLogin;
    private boolean isActive;
    private int totalBorrowings;
    private int activeBorrowings;

    /**
     * Default constructor required for Firebase
     */
    public User() {
        this.role = "user";
        this.isActive = true;
        this.totalBorrowings = 0;
        this.activeBorrowings = 0;
        this.createdAt = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
    }

    /**
     * Constructor with basic user information
     * @param uid Firebase Auth UID
     * @param fullName User's full name
     * @param email User's email address
     */
    public User(String uid, String fullName, String email) {
        this();
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
    }

    /**
     * Constructor with complete user information
     * @param uid Firebase Auth UID
     * @param fullName User's full name
     * @param email User's email address
     * @param role User role (admin/user)
     * @param phoneNumber Phone number
     * @param address Address
     */
    public User(String uid, String fullName, String email, String role, String phoneNumber, String address) {
        this(uid, fullName, email);
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Getters and Setters

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getTotalBorrowings() {
        return totalBorrowings;
    }

    public void setTotalBorrowings(int totalBorrowings) {
        this.totalBorrowings = totalBorrowings;
    }

    public int getActiveBorrowings() {
        return activeBorrowings;
    }

    public void setActiveBorrowings(int activeBorrowings) {
        this.activeBorrowings = activeBorrowings;
    }

    /**
     * Check if user is an admin
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    /**
     * Check if user is a regular user
     * @return true if user is regular user, false otherwise
     */
    public boolean isUser() {
        return "user".equals(role);
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }

    /**
     * Increment total borrowings count
     */
    public void incrementTotalBorrowings() {
        this.totalBorrowings++;
    }

    /**
     * Increment active borrowings count
     */
    public void incrementActiveBorrowings() {
        this.activeBorrowings++;
    }

    /**
     * Decrement active borrowings count
     */
    public void decrementActiveBorrowings() {
        if (this.activeBorrowings > 0) {
            this.activeBorrowings--;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", totalBorrowings=" + totalBorrowings +
                ", activeBorrowings=" + activeBorrowings +
                '}';
    }
}
