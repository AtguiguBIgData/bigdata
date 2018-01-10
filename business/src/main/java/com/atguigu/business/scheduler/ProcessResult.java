/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.scheduler;

public class ProcessResult {
    private String uuid;
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ProcessResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
