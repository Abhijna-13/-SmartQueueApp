package com.example.smartqueueapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartqueueapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    // -------------------------------------------------------
    // UI References
    // -------------------------------------------------------
    private TextInputLayout   tilFullName, tilEmail, tilPassword;
    private TextInputEditText etFullName, etEmail, etPassword;
    private MaterialButton    btnRegister;
    private ProgressBar       progressBar;
    private TextView          tvLoginRedirect;

    // -------------------------------------------------------
    // Firebase
    // -------------------------------------------------------
    private FirebaseAuth mAuth;

    // -------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initFirebase();
        bindViews();
        setListeners();
    }

    // -------------------------------------------------------
    // Initialisation helpers
    // -------------------------------------------------------
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void bindViews() {
        tilFullName     = findViewById(R.id.tilFullName);
        tilEmail        = findViewById(R.id.tilEmail);
        tilPassword     = findViewById(R.id.tilPassword);

        etFullName      = findViewById(R.id.etFullName);
        etEmail         = findViewById(R.id.etEmail);
        etPassword      = findViewById(R.id.etPassword);

        btnRegister     = findViewById(R.id.btnRegister);
        progressBar     = findViewById(R.id.progressBar);
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect);
    }

    private void setListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLoginRedirect.setOnClickListener(v -> navigateToLogin());
    }

    // -------------------------------------------------------
    // Validation
    // -------------------------------------------------------
    private boolean validateInputs(String fullName, String email, String password) {
        boolean valid = true;

        // Full Name
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            valid = false;
        } else {
            tilFullName.setError(null);
        }

        // Email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            tilEmail.setError(null);
        }

        // Password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            tilPassword.setError(null);
        }

        return valid;
    }

    // -------------------------------------------------------
    // Registration logic
    // -------------------------------------------------------
    private void attemptRegister() {
        String fullName = etFullName.getText() != null
                ? etFullName.getText().toString().trim() : "";
        String email    = etEmail.getText() != null
                ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString().trim() : "";

        if (!validateInputs(fullName, email, password)) {
            return;
        }

        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Registration succeeded — navigate to Dashboard
                        Toast.makeText(
                                RegisterActivity.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();
                        navigateToDashboard();
                    } else {
                        // Registration failed — show error
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed. Please try again.";
                        Toast.makeText(
                                RegisterActivity.this,
                                errorMsg,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // -------------------------------------------------------
    // Navigation helpers
    // -------------------------------------------------------
    private void navigateToDashboard() {
        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // -------------------------------------------------------
    // UI state helpers
    // -------------------------------------------------------
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
        btnRegister.setAlpha(isLoading ? 0.6f : 1.0f);
    }
}
