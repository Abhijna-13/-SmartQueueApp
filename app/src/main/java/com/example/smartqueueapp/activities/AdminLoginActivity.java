package com.example.smartqueueapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import com.example.smartqueueapp.R;

public class AdminLoginActivity extends AppCompatActivity {

    // -------------------------------------------------------
    // Admin gate — only this email may access the Admin Panel
    // -------------------------------------------------------
    private static final String ADMIN_EMAIL = "admin@smartqueue.com";

    // -------------------------------------------------------
    // UI References
    // -------------------------------------------------------
    private TextInputLayout   tilAdminEmail, tilAdminPassword;
    private TextInputEditText etAdminEmail, etAdminPassword;
    private MaterialButton    btnAdminLogin;
    private ProgressBar       progressBar;
    private TextView          tvBackToLogin;

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
        setContentView(R.layout.activity_admin_login);

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
        tilAdminEmail     = findViewById(R.id.tilAdminEmail);
        tilAdminPassword  = findViewById(R.id.tilAdminPassword);
        etAdminEmail      = findViewById(R.id.etAdminEmail);
        etAdminPassword   = findViewById(R.id.etAdminPassword);
        btnAdminLogin     = findViewById(R.id.btnAdminLogin);
        progressBar       = findViewById(R.id.progressBar);
        tvBackToLogin     = findViewById(R.id.tvBackToLogin);
    }

    private void setListeners() {
        btnAdminLogin.setOnClickListener(v -> attemptAdminLogin());
        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(AdminLoginActivity.this, LoginActivity.class));
            finish();
        });
    }

    // -------------------------------------------------------
    // Validation
    // -------------------------------------------------------
    private boolean validateInputs(String email, String password) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            tilAdminEmail.setError("Email is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilAdminEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            tilAdminEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilAdminPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            tilAdminPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            tilAdminPassword.setError(null);
        }

        return valid;
    }

    // -------------------------------------------------------
    // Admin login logic
    // -------------------------------------------------------
    private void attemptAdminLogin() {
        String email    = etAdminEmail.getText() != null
                ? etAdminEmail.getText().toString().trim() : "";
        String password = etAdminPassword.getText() != null
                ? etAdminPassword.getText().toString().trim() : "";

        if (!validateInputs(email, password)) return;

        // ── Admin email gate (client-side pre-check) ─────────────────
        // This stops non-admin users from even hitting Firebase.
        if (!email.equalsIgnoreCase(ADMIN_EMAIL)) {
            showAccessDenied();
            return;
        }

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Double-check email after Firebase confirms credentials
                        String signedInEmail = mAuth.getCurrentUser() != null
                                ? mAuth.getCurrentUser().getEmail()
                                : "";

                        if (ADMIN_EMAIL.equalsIgnoreCase(signedInEmail)) {
                            // ✅ Verified admin — navigate to Admin Dashboard
                            navigateToAdminDashboard();
                        } else {
                            // Signed in successfully but not the admin account
                            mAuth.signOut();
                            showAccessDenied();
                        }
                    } else {
                        // Firebase auth failure (wrong password, no such user, etc.)
                        showAccessDenied();
                    }
                });
    }

    // -------------------------------------------------------
    // Navigation
    // -------------------------------------------------------
    private void navigateToAdminDashboard() {
        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // -------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------
    private void showAccessDenied() {
        Toast.makeText(this, "Access Denied", Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnAdminLogin.setEnabled(!isLoading);
        btnAdminLogin.setAlpha(isLoading ? 0.6f : 1.0f);
    }
}
