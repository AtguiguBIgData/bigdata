package com.atguigu.business.scheduler;

/**
 * Created by wuyufei on 10/9/2016.
 */
public interface TaskListener {
    void notify(ProcessResult processResult);
}
