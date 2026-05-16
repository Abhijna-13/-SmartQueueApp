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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // -------------------------------------------------------
    // UI References
    // -------------------------------------------------------
    private TextInputLayout   tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton    btnLogin;
    private ProgressBar       progressBar;
    private TextView          tvForgotPassword, tvRegisterRedirect, tvAdminLogin;

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
        setContentView(R.layout.activity_login);

        initFirebase();
        checkAlreadyLoggedIn();   // Auto-login check
        bindViews();
        setListeners();
    }

    // -------------------------------------------------------
    // Initialisation helpers
    // -------------------------------------------------------
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * If the user is already authenticated, skip straight to Dashboard.
     * Must be called before setContentView would block the UI.
     */
    private void checkAlreadyLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToDashboard();
        }
    }

    private void bindViews() {
        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);

        etEmail            = findViewById(R.id.etEmail);
        etPassword         = findViewById(R.id.etPassword);

        btnLogin           = findViewById(R.id.btnLogin);
        progressBar        = findViewById(R.id.progressBar);
        tvForgotPassword   = findViewById(R.id.tvForgotPassword);
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect);
        tvAdminLogin       = findViewById(R.id.tvAdminLogin);
    }

    private void setListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        tvRegisterRedirect.setOnClickListener(v -> navigateToRegister());
        tvAdminLogin.setOnClickListener(v -> navigateToAdminLogin());
    }

    // -------------------------------------------------------
    // Validation
    // -------------------------------------------------------
    private boolean validateInputs(String email, String password) {
        boolean valid = true;

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
    // Login logic
    // -------------------------------------------------------
    private void attemptLogin() {
        String email    = etEmail.getText() != null
                ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString().trim() : "";

        if (!validateInputs(email, password)) {
            return;
        }

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                LoginActivity.this,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                        ).show();
                        navigateToDashboard();
                    } else {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Login failed. Please check your credentials.";
                        Toast.makeText(
                                LoginActivity.this,
                                errorMsg,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // -------------------------------------------------------
    // Forgot password (password reset email)
    // -------------------------------------------------------
    private void handleForgotPassword() {
        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Enter your email to reset password");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to send reset email.";
                        Toast.makeText(
                                LoginActivity.this,
                                errorMsg,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // -------------------------------------------------------
    // Navigation helpers
    // -------------------------------------------------------
    /** Navigate to Dashboard and clear the back-stack so the user cannot press Back to return. */
    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }

    private void navigateToAdminLogin() {
        startActivity(new Intent(LoginActivity.this, AdminLoginActivity.class));
    }

    // -------------------------------------------------------
    // UI state helpers
    // -------------------------------------------------------
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnLogin.setAlpha(isLoading ? 0.6f : 1.0f);
    }
}
