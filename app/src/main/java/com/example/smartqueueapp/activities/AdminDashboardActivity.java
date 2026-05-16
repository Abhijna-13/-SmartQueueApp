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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartqueueapp.R;
import com.example.smartqueueapp.adapters.TokenAdapter;
import com.example.smartqueueapp.models.Token;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    // UI
    private Toolbar toolbar;
    private TextView tvNowServing, tvServingName, tvTotalWaiting, tvEmpty;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private MaterialButton btnNextToken;
    private MaterialButton btnCompleteToken;
    private MaterialButton btnLogout;

    // Firebase
    private DatabaseReference queueRef;

    // Adapter
    private TokenAdapter adapter;

    private List<Token> tokenList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initFirebase();
        bindViews();
        setupToolbar();
        setupRecyclerView();
        setListeners();
        loadQueue();
    }

    // ---------------------------------------------------
    // Firebase
    // ---------------------------------------------------
    private void initFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://smart-queue-app-f25f9-default-rtdb.asia-southeast1.firebasedatabase.app/"
        );

        queueRef = database.getReference("queue");
    }

    // ---------------------------------------------------
    // Bind Views
    // ---------------------------------------------------
    private void bindViews() {

        toolbar = findViewById(R.id.toolbar);

        tvNowServing = findViewById(R.id.tvNowServing);
        tvServingName = findViewById(R.id.tvServingName);
        tvTotalWaiting = findViewById(R.id.tvTotalWaiting);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        btnNextToken = findViewById(R.id.btnNextToken);
        btnCompleteToken = findViewById(R.id.btnCompleteToken);
        btnLogout = findViewById(R.id.btnLogout);
    }

    // ---------------------------------------------------
    // Toolbar
    // ---------------------------------------------------
    private void setupToolbar() {

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }
    }

    // ---------------------------------------------------
    // RecyclerView
    // ---------------------------------------------------
    private void setupRecyclerView() {

        adapter = new TokenAdapter(this);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);
    }

    // ---------------------------------------------------
    // Listeners
    // ---------------------------------------------------
    private void setListeners() {

        btnNextToken.setOnClickListener(
                v -> moveNextTokenToServing()
        );

        btnCompleteToken.setOnClickListener(
                v -> completeCurrentToken()
        );

        btnLogout.setOnClickListener(
                v -> confirmLogout()
        );
    }

    // ---------------------------------------------------
    // Load Queue
    // ---------------------------------------------------
    private void loadQueue() {

        showLoading(true);

        queueRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        tokenList.clear();

                        int waitingCount = 0;

                        String servingToken = "–";
                        String servingName = "";

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            Token token =
                                    ds.getValue(Token.class);

                            if (token != null) {

                                tokenList.add(token);

                                if ("waiting".equalsIgnoreCase(
                                        token.getStatus())) {

                                    waitingCount++;
                                }

                                if ("serving".equalsIgnoreCase(
                                        token.getStatus())) {

                                    servingToken =
                                            "#" + token.getTokenNo();

                                    servingName =
                                            token.getName();
                                }
                            }
                        }

                        tvNowServing.setText(servingToken);
                        tvServingName.setText(servingName);

                        tvTotalWaiting.setText(
                                String.valueOf(waitingCount)
                        );

                        adapter.updateList(tokenList);

                        if (tokenList.isEmpty()) {

                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);

                        } else {

                            tvEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        showLoading(false);
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        showLoading(false);

                        Toast.makeText(
                                AdminDashboardActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // ---------------------------------------------------
    // Next Token
    // ---------------------------------------------------
    private void moveNextTokenToServing() {

        queueRef.orderByChild("status")
                .equalTo("waiting")
                .limitToFirst(1)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                for (DataSnapshot ds :
                                        snapshot.getChildren()) {

                                    ds.getRef()
                                            .child("status")
                                            .setValue("serving");

                                    Toast.makeText(
                                            AdminDashboardActivity.this,
                                            "Next token is now serving",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    break;
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                Toast.makeText(
                                        AdminDashboardActivity.this,
                                        error.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
    }

    // ---------------------------------------------------
    // Complete Token
    // ---------------------------------------------------
    private void completeCurrentToken() {

        queueRef.orderByChild("status")
                .equalTo("serving")
                .limitToFirst(1)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                for (DataSnapshot ds :
                                        snapshot.getChildren()) {

                                    ds.getRef()
                                            .child("status")
                                            .setValue("completed");

                                    Toast.makeText(
                                            AdminDashboardActivity.this,
                                            "Token completed",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    break;
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                Toast.makeText(
                                        AdminDashboardActivity.this,
                                        error.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
    }

    // ---------------------------------------------------
    // Logout
    // ---------------------------------------------------
    private void confirmLogout() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Logout from admin panel?")
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

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(
                AdminDashboardActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    // ---------------------------------------------------
    // Helpers
    // ---------------------------------------------------
    private void showLoading(boolean loading) {

        progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
    }
}