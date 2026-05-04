package com.example.barrowing_system;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Registration Activity with Firebase Integration
 * Includes all required fields: First Name, Last Name, Age, Address, Email, Password, Confirm Password
 * Password requirements: 12-16 characters with strength indicator
 * Password visibility toggle for both password fields
 */
public class RegisterActivityNew extends AppCompatActivity {

    private EditText etFirstName, etLastName, etAge, etAddress, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin, tvPasswordStrength;
    private ImageView ivTogglePassword, ivToggleConfirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_register_new);

            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            initViews();
            setListeners();
            setPasswordStrengthListener();

            Toast.makeText(this, "Register screen loaded successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading register: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            etFirstName = findViewById(R.id.etFirstName);
            etLastName = findViewById(R.id.etLastName);
            etAge = findViewById(R.id.etAge);
            etAddress = findViewById(R.id.etAddress);
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            btnRegister = findViewById(R.id.btnRegister);
            tvBackToLogin = findViewById(R.id.tvBackToLogin);
            tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
            ivTogglePassword = findViewById(R.id.ivTogglePassword);
            ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing register views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setListeners() {
        try {
            btnRegister.setOnClickListener(v -> {
                String firstName = etFirstName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String ageStr = etAge.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (!validateInputs(firstName, lastName, ageStr, address, email, password, confirmPassword)) {
                    return;
                }

                int age = Integer.parseInt(ageStr);
                performRegistration(firstName, lastName, age, address, email, password);
            });

            // Back to login click
            tvBackToLogin.setOnClickListener(v -> finish());
            
            // Password visibility toggle
            ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
            ivToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());
            
        } catch (Exception e) {
            Toast.makeText(this, "Error setting register listeners: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setPasswordStrengthListener() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            tvPasswordStrength.setText("");
            return;
        }
        
        String strengthText = PasswordValidator.getPasswordStrengthText(password, this);
        tvPasswordStrength.setText(getString(R.string.password_strength_indicator, strengthText));
        if (strengthText.equals(getString(R.string.password_strength_weak))) {
            tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (strengthText.equals(getString(R.string.password_strength_medium))) {
            tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
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

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye);
        } else {
            etConfirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }

    private boolean validateInputs(String firstName, String lastName, String ageStr, String address, 
                                  String email, String password, String confirmPassword) {
        // First Name validation
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError(getString(R.string.first_name_required));
            etFirstName.requestFocus();
            return false;
        }

        if (firstName.length() < 2) {
            etFirstName.setError("First name must be at least 2 characters");
            etFirstName.requestFocus();
            return false;
        }

        // Last Name validation
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError(getString(R.string.last_name_required));
            etLastName.requestFocus();
            return false;
        }

        if (lastName.length() < 2) {
            etLastName.setError("Last name must be at least 2 characters");
            etLastName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(ageStr)) {
            etAge.setError(getString(R.string.age_required));
            etAge.requestFocus();
            return false;
        }

        try {
            int age = Integer.parseInt(ageStr);
            if (age < 18 || age > 100) {
                etAge.setError(getString(R.string.invalid_age));
                etAge.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAge.setError(getString(R.string.invalid_age));
            etAge.requestFocus();
            return false;
        }

        // Address validation
        if (TextUtils.isEmpty(address)) {
            etAddress.setError(getString(R.string.address_required));
            etAddress.requestFocus();
            return false;
        }

        if (address.length() < 10) {
            etAddress.setError("Please enter a complete address (at least 10 characters)");
            etAddress.requestFocus();
            return false;
        }

        // Email validation
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

        // Password validation with strong requirements
        if (!PasswordValidator.validatePasswordAndSetError(password, this, etPassword)) {
            return false;
        }

        // Confirm password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.confirm_password_required));
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.passwords_not_match));
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performRegistration(String firstName, String lastName, int age, String address,
                                   String email, String password) {
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating account...");
        
        // Disable reCAPTCHA verification for testing (remove in production)
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                if (user != null) {
                    saveUserToDatabase(user.getUid(), firstName, lastName, age, address, email);
                }
            })
            .addOnFailureListener(e -> {
                btnRegister.setEnabled(true);
                btnRegister.setText("Create Account");
                String errorMsg = "Registration failed: " + e.getMessage();
                if (e.getMessage() != null && e.getMessage().contains("network")) {
                    errorMsg = "Network error. Please check your internet connection and try again.";
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            });
    }

    private void saveUserToDatabase(String userId, String firstName, String lastName, int age,
                                   String address, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", firstName + " " + lastName);
        userData.put("email", email);
        userData.put("phone", "");
        userData.put("address", address);
        userData.put("status", "Active");
        userData.put("joinedDate", java.text.DateFormat.getDateInstance().format(new java.util.Date()));
        userData.put("totalBorrows", 0);
        userData.put("role", "resident");

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create Account");
                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().getCurrentUser().delete();
                });
    }
}
