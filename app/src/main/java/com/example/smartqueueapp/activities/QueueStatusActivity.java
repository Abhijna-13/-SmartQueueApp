package com.example.smartqueueapp.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartqueueapp.R;
import com.example.smartqueueapp.adapters.TokenAdapter;
import com.example.smartqueueapp.models.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueueStatusActivity extends AppCompatActivity {

    private static final int AVG_SERVICE_MINUTES = 3;

    // UI
    private Toolbar toolbar;
    private TextView tvNowServing, tvYourToken, tvWaitTime, tvEmpty;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference queueRef;
    private ValueEventListener queueListener;

    // Adapter
    private TokenAdapter adapter;

    // Current User
    private String currentUserName = "";
    private String currentUserEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_status);

        initFirebase();
        bindViews();
        setupToolbar();
        setupRecyclerView();
        loadCurrentUser();
        listenForQueueUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (queueListener != null && queueRef != null) {
            queueRef.removeEventListener(queueListener);
        }
    }

    // ---------------------------------------------------
    // Firebase
    // ---------------------------------------------------
    private void initFirebase() {

        mAuth = FirebaseAuth.getInstance();

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
        tvYourToken = findViewById(R.id.tvYourToken);
        tvWaitTime = findViewById(R.id.tvWaitTime);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
    }

    // ---------------------------------------------------
    // Toolbar
    // ---------------------------------------------------
    private void setupToolbar() {

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Queue Status");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    // Current User
    // ---------------------------------------------------
    private void loadCurrentUser() {

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
    }

    // ---------------------------------------------------
    // Listen Firebase Queue
    // ---------------------------------------------------
    private void listenForQueueUpdates() {

        showLoading(true);

        queueListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<Token> waitingTokens = new ArrayList<>();

                String nowServing = "–";
                int yourToken = -1;
                int waitTime = 0;

                try {

                    List<Token> allTokens = new ArrayList<>();

                    for (DataSnapshot child : snapshot.getChildren()) {

                        Token token = child.getValue(Token.class);

                        if (token != null) {
                            allTokens.add(token);
                        }
                    }

                    // Sort tokens
                    Collections.sort(allTokens,
                            (a, b) ->
                                    Integer.compare(
                                            a.getTokenNo(),
                                            b.getTokenNo()
                                    ));

                    int waitingPosition = 0;

                    for (Token token : allTokens) {

                        if (token.getStatus() == null)
                            continue;

                        // NOW SERVING
                        if (token.getStatus()
                                .equalsIgnoreCase("serving")) {

                            nowServing =
                                    String.valueOf(
                                            token.getTokenNo()
                                    );
                        }

                        // WAITING TOKENS
                        if (token.getStatus()
                                .equalsIgnoreCase("waiting")) {

                            waitingTokens.add(token);

                            waitingPosition++;

                            if (isCurrentUser(token)) {

                                yourToken =
                                        token.getTokenNo();

                                waitTime =
                                        (waitingPosition - 1)
                                                * AVG_SERVICE_MINUTES;
                            }
                        }
                    }

                    // ----------------------------
                    // Update UI
                    // ----------------------------

                    tvNowServing.setText(nowServing);

                    if (yourToken != -1) {
                        tvYourToken.setText(
                                String.valueOf(yourToken)
                        );
                    } else {
                        tvYourToken.setText("–");
                    }

                    tvWaitTime.setText(waitTime + " min");

                    adapter.updateList(waitingTokens);

                    showEmptyState(waitingTokens.isEmpty());

                } catch (Exception e) {

                    Toast.makeText(
                            QueueStatusActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }

                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                showLoading(false);

                Toast.makeText(
                        QueueStatusActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        queueRef.addValueEventListener(queueListener);
    }

    // ---------------------------------------------------
    // Helpers
    // ---------------------------------------------------
    private boolean isCurrentUser(Token token) {

        if (token.getName() == null)
            return false;

        if (!currentUserName.isEmpty()
                && token.getName()
                .equalsIgnoreCase(currentUserName)) {

            return true;
        }

        if (!currentUserEmail.isEmpty()) {

            String prefix =
                    currentUserEmail.split("@")[0];

            return token.getName()
                    .equalsIgnoreCase(prefix);
        }

        return false;
    }

    private void showLoading(boolean loading) {

        progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
    }

    private void showEmptyState(boolean empty) {

        tvEmpty.setVisibility(
                empty ? View.VISIBLE : View.GONE
        );

        recyclerView.setVisibility(
                empty ? View.GONE : View.VISIBLE
        );
    }
}