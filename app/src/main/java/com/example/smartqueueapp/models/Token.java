package com.example.smartqueueapp.models;

public class Token {

    private String tokenId;
    private String name;
    private int tokenNo;
    private String status; // "waiting", "serving", "completed"
    private long timestamp;

    // -------------------------------------------------------
    // Empty constructor — required for Firebase Realtime DB
    // -------------------------------------------------------
    public Token() {
    }

    // -------------------------------------------------------
    // Parameterized constructor
    // -------------------------------------------------------
    public Token(String tokenId, String name, int tokenNo, String status, long timestamp) {
        this.tokenId   = tokenId;
        this.name      = name;
        this.tokenNo   = tokenNo;
        this.status    = status;
        this.timestamp = timestamp;
    }

    // -------------------------------------------------------
    // Getters
    // -------------------------------------------------------
    public String getTokenId() {
        return tokenId;
    }

    public String getName() {
        return name;
    }

    public int getTokenNo() {
        return tokenNo;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // -------------------------------------------------------
    // Setters
    // -------------------------------------------------------
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTokenNo(int tokenNo) {
        this.tokenNo = tokenNo;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
