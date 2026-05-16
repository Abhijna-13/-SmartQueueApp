package com.example.smartqueueapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.smartqueueapp.R;
import com.example.smartqueueapp.models.Token;

public class DashboardActivity extends AppCompatActivity {

    // -------------------------------------------------------
    // UI References
    // -------------------------------------------------------
    private TextView tvWelcome, tvCurrentlyServing;
    private CardView cardGenerateToken, cardViewQueue;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private MaterialButton btnLogout;

    // -------------------------------------------------------
    // Firebase
    // -------------------------------------------------------
    private FirebaseAuth mAuth;
    private DatabaseReference queueRef;

    // -------------------------------------------------------
    // State
    // -------------------------------------------------------
    private String currentUserName = "User";
    private String currentUserEmail = "";
    private ValueEventListener servingListener;

    // -------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initFirebase();
        bindViews();
        setupToolbar();
        loadUserInfo();
        listenForServingToken();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (servingListener != null) {
            queueRef.removeEventListener(servingListener);
        }
    }

    // -------------------------------------------------------
    // Firebase
    // -------------------------------------------------------
    private void initFirebase() {

        mAuth = FirebaseAuth.getInstance();

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://smart-queue-app-f25f9-default-rtdb.asia-southeast1.firebasedatabase.app/"
        );

        queueRef = database.getReference("queue");
    }

    // -------------------------------------------------------
    // Bind Views
    // -------------------------------------------------------
    private void bindViews() {

        toolbar = findViewById(R.id.toolbar);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvCurrentlyServing = findViewById(R.id.tvCurrentlyServing);

        cardGenerateToken = findViewById(R.id.cardGenerateToken);
        cardViewQueue = findViewById(R.id.cardViewQueue);

        progressBar = findViewById(R.id.progressBar);

        btnLogout = findViewById(R.id.btnLogout);
    }

    // -------------------------------------------------------
    // Toolbar
    // -------------------------------------------------------
    private void setupToolbar() {

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SmartQueue");
        }
    }

    // -------------------------------------------------------
    // User Info
    // -------------------------------------------------------
    private void loadUserInfo() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            currentUserEmail =
                    user.getEmail() != null
                            ? user.getEmail()
                            : "";

            if (user.getDisplayName() != null
                    && !user.getDisplayName().isEmpty()) {

                currentUserName = user.getDisplayName();

            } else if (!currentUserEmail.isEmpty()) {

                currentUserName =
                        currentUserEmail.split("@")[0];
            }
        }

        tvWelcome.setText(
                "Hello, " + currentUserName + "!"
        );
    }

    // -------------------------------------------------------
    // Real-time Listener
    // -------------------------------------------------------
    private void listenForServingToken() {

        servingListener = new ValueEventListener() {

            @Override
            public void onDataChange(
                    @NonNull DataSnapshot snapshot) {

                boolean found = false;

                for (DataSnapshot tokenSnap :
                        snapshot.getChildren()) {

                    Token token =
                            tokenSnap.getValue(Token.class);

                    if (token != null
                            && "serving".equalsIgnoreCase(
                            token.getStatus())) {

                        tvCurrentlyServing.setText(
                                "Token #" + token.getTokenNo()
                        );

                        found = true;
                        break;
                    }
                }

                if (!found) {
                    tvCurrentlyServing.setText(
                            "No one serving"
                    );
                }
            }

            @Override
            public void onCancelled(
                    @NonNull DatabaseError error) {

                tvCurrentlyServing.setText(
                        "Unavailable"
                );

                Toast.makeText(
                        DashboardActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        };

        queueRef.addValueEventListener(servingListener);
    }

    // -------------------------------------------------------
    // Click Listeners
    // -------------------------------------------------------
    private void setListeners() {

        cardGenerateToken.setOnClickListener(
                v -> generateToken()
        );

        cardViewQueue.setOnClickListener(
                v -> navigateToQueueStatus()
        );

        btnLogout.setOnClickListener(
                v -> confirmLogout()
        );
    }

    // -------------------------------------------------------
    // Generate Token
    // -------------------------------------------------------
    private void generateToken() {

        showLoading(true);

        queueRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        int nextTokenNo =
                                (int) snapshot.getChildrenCount() + 1;

                        String newKey =
                                queueRef.push().getKey();

                        if (newKey == null) {

                            showLoading(false);

                            Toast.makeText(
                                    DashboardActivity.this,
                                    "Failed to generate token",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        Token newToken = new Token(
                                newKey,
                                currentUserName,
                                nextTokenNo,
                                "waiting",
                                System.currentTimeMillis()
                        );

                        queueRef.child(newKey)
                                .setValue(newToken)
                                .addOnCompleteListener(task -> {

                                    showLoading(false);

                                    if (task.isSuccessful()) {

                                        Toast.makeText(
                                                DashboardActivity.this,
                                                "Your token number is: "
                                                        + nextTokenNo,
                                                Toast.LENGTH_LONG
                                        ).show();

                                    } else {

                                        String err =
                                                task.getException() != null
                                                        ? task.getException()
                                                        .getMessage()
                                                        : "Error";

                                        Toast.makeText(
                                                DashboardActivity.this,
                                                err,
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        showLoading(false);

                        Toast.makeText(
                                DashboardActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // -------------------------------------------------------
    // Logout
    // -------------------------------------------------------
    private void confirmLogout() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(
                        "Logout",
                        (dialog, which) -> performLogout()
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void performLogout() {

        mAuth.signOut();

        Intent intent = new Intent(
                DashboardActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    // -------------------------------------------------------
    // Navigation
    // -------------------------------------------------------
    private void navigateToQueueStatus() {

        startActivity(
                new Intent(
                        DashboardActivity.this,
                        QueueStatusActivity.class
                )
        );
    }

    // -------------------------------------------------------
    // Loading UI
    // -------------------------------------------------------
    private void showLoading(boolean isLoading) {

        progressBar.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );

        cardGenerateToken.setEnabled(!isLoading);
        cardViewQueue.setEnabled(!isLoading);
    }
}