package com.atguigu.business.model.domain;

public class Comment {

    private int uid;

    private int mid;

    private double score;

    private String tag;

    private long timestamp;

    public Comment() {
    }

    public Comment(int uid, int mid, double score, String tag, long timestamp) {
        this.uid = uid;
        this.mid = mid;
        this.score = score;
        this.tag = tag;
        this.timestamp = timestamp;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}