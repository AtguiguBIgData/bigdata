/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.tasks;

import java.util.HashMap;
import java.util.Map;

public class Task {

    private String uuid;

    private String title;

    private String jarpath;

    private Map<String,Object> params;

    private Status status;

    private Type type;

    private Long timestamp;

    public Task() {
        this.params = new HashMap<>();
    }

    public Task(String uuid, String title, String jarpath, Map<String, Object> params, Status status, Type type, Long timestamp) {
        this.uuid = uuid;
        this.title = title;
        this.jarpath = jarpath;
        this.params = params;
        this.status = status;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJarpath() {
        return jarpath;
    }

    public void setJarpath(String jarpath) {
        this.jarpath = jarpath;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }


    enum Type {
        SPARK(0),
        SPARK_STREAMING(1);

        private int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    enum Status {
        NEW(0),
        PENDING(1),
        RUNNING(2),
        SUCCESS(3),
        FAIL(4);

        private int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }
}