package com.example.smartqueueapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartqueueapp.R;
import com.example.smartqueueapp.models.Token;

import java.util.ArrayList;
import java.util.List;

public class TokenAdapter extends RecyclerView.Adapter<TokenAdapter.TokenViewHolder> {

    // -------------------------------------------------------
    // Status text colours
    // -------------------------------------------------------
    private static final int COLOR_WAITING   = Color.parseColor("#FF9800"); // Orange
    private static final int COLOR_SERVING   = Color.parseColor("#4CAF50"); // Green
    private static final int COLOR_COMPLETED = Color.parseColor("#9E9E9E"); // Gray

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------
    private final Context     context;
    private final List<Token> tokenList;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------
    public TokenAdapter(Context context) {
        this(context, new ArrayList<>());
    }

    public TokenAdapter(Context context, List<Token> tokenList) {
        this.context   = context;
        this.tokenList = new ArrayList<>(tokenList); // defensive copy
    }

    // -------------------------------------------------------
    // Refresh data from outside (e.g. Firebase listener)
    // -------------------------------------------------------
    public void updateList(List<Token> newList) {
        tokenList.clear();
        tokenList.addAll(newList);
        notifyDataSetChanged();
    }

    // -------------------------------------------------------
    // RecyclerView.Adapter overrides
    // -------------------------------------------------------
    @NonNull
    @Override
    public TokenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_token, parent, false);
        return new TokenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TokenViewHolder holder, int position) {
        holder.bind(tokenList.get(position));
    }

    @Override
    public int getItemCount() {
        return tokenList.size();
    }

    // -------------------------------------------------------
    // ViewHolder
    // -------------------------------------------------------
    static class TokenViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTokenNo;
        private final TextView tvName;
        private final TextView tvStatus;

        TokenViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTokenNo = itemView.findViewById(R.id.tvTokenNo);
            tvName    = itemView.findViewById(R.id.tvCustomerName);
            tvStatus  = itemView.findViewById(R.id.tvStatusBadge);
        }

        void bind(Token token) {
            // ── Token number ──────────────────────────────────────
            tvTokenNo.setText("#" + token.getTokenNo());

            // ── Customer name ─────────────────────────────────────
            String name = (token.getName() != null && !token.getName().isEmpty())
                    ? token.getName()
                    : "Unknown";
            tvName.setText(name);

            // ── Status text + colour ──────────────────────────────
            String status = (token.getStatus() != null)
                    ? token.getStatus()
                    : "waiting";

            tvStatus.setText(capitalise(status));

            switch (status) {
                case "serving":
                    tvStatus.setTextColor(COLOR_SERVING);
                    break;
                case "completed":
                    tvStatus.setTextColor(COLOR_COMPLETED);
                    break;
                case "waiting":
                default:
                    tvStatus.setTextColor(COLOR_WAITING);
                    break;
            }
        }

        // "waiting" → "Waiting"
        private String capitalise(String s) {
            if (s == null || s.isEmpty()) return s;
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }
}
