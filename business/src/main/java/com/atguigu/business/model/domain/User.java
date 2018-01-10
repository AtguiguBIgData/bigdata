package com.atguigu.business.model.domain;

public class User {

    private int uid;

    private String username;

    private String password;

    private boolean first;

    private long timestamp;

    private String prefgenres;

    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.uid = username.hashCode();
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp / 1000;
    }

    public boolean passwordMatch(String password) {
        return this.password.compareTo(password) == 0;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }


    public String getPrefgenres() {
        return prefgenres;
    }

    public void setPrefgenres(String prefgenres) {
        this.prefgenres = prefgenres;
    }
}