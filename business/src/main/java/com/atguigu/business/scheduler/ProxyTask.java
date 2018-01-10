/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.scheduler;

import com.atguigu.business.utils.SpringContextHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ProxyTask implements Job {
    private Logger logger = LoggerFactory.getLogger(ProxyTask.class);

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap datamap = jobExecutionContext.getJobDetail().getJobDataMap();
        String uuid = datamap.getString("uuid");
        RunnableTask task = (RunnableTask) datamap.get("task");
        ProcessResult processResult = null;
        try{
            processResult = task.run();
        }catch (Exception e){
            logger.error("fail to process {} with exception {}",task.getClass(),e.getMessage());
            processResult = new ProcessResult(false,e.getMessage());
        }
        processResult.setUuid(uuid);
        SpringContextHelper.getBean(SchedulerService.class).notify(uuid,processResult);
        logger.debug("complete exec process {} at {}",this.getClass(),new Date());
    }
}
