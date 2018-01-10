/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.scheduler;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SchedulerService implements RunnableTask {
    private Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private static Scheduler scheduler = null;

    static {
        try{
            if(null == scheduler){
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void shutdown(){
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private ConcurrentHashMap<String, JobKey> tasks;
    private ConcurrentHashMap<String, List<TaskListener>> listConcurrentHashMap;

    private SchedulerService() {
        tasks = new ConcurrentHashMap<String, JobKey>();
        listConcurrentHashMap = new ConcurrentHashMap<String, List<TaskListener>>();

        this.submitTask(UUID.randomUUID().toString(),"0 0 */1 * * ?",this);
    }

    public String submitTask(String uuid,String crontime, RunnableTask task) {
        try {
            JobKey jobKey = new JobKey(uuid, "group");
            JobDetail jobDetail = JobBuilder.newJob(ProxyTask.class)
                    .withIdentity(jobKey)
                    .build();
            jobDetail.getJobDataMap().put("uuid", uuid);
            jobDetail.getJobDataMap().put("task", task);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(uuid, "group")
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(crontime))
                    .build();
            tasks.put(uuid, jobKey);
            scheduler.scheduleJob(jobDetail, trigger);
            logger.debug("Successfully schdule task " + task.getClass() + " with uuid:" + uuid);
            return uuid;
        } catch (SchedulerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String runTaskOnce(String uuid,RunnableTask task) {
        try {
            JobKey jobKey = new JobKey(uuid, "group");
            JobDetail jobDetail = JobBuilder.newJob(ProxyTask.class)
                    .withIdentity(jobKey)
                    .build();
            jobDetail.getJobDataMap().put("uuid", uuid);
            jobDetail.getJobDataMap().put("task", task);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(uuid, "group")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
                    .startNow()
                    .build();
            tasks.put(uuid, jobKey);
            scheduler.scheduleJob(jobDetail, trigger);
            logger.debug("Successfully schdule task " + task.getClass() + " with uuid:" + uuid);
            return uuid;
        } catch (SchedulerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void notify(String uuid, ProcessResult processResult) {
        if (listConcurrentHashMap.containsKey(uuid)) {
            for (TaskListener listener : listConcurrentHashMap.get(uuid)) {
                listener.notify(processResult);
            }
        }
    }

    public void registerListener(String uuid, TaskListener listener) {
        if (!listConcurrentHashMap.containsKey(uuid))
            listConcurrentHashMap.put(uuid, new LinkedList<TaskListener>());
        listConcurrentHashMap.get(uuid).add(listener);
    }

    public void removeListener(String uuid, Class<? extends TaskListener> listener) {
        if (listConcurrentHashMap.containsKey(uuid)) {
            for (int i = 0; i < listConcurrentHashMap.get(uuid).size(); i++) {
                if (listConcurrentHashMap.get(uuid).get(i).getClass() == listener) {
                    listConcurrentHashMap.get(uuid).remove(i);
                    break;
                }
            }
        }
    }

    public boolean removeTask(String uuid) {
        try {
            if (scheduler.checkExists(tasks.get(uuid))) {
                scheduler.deleteJob(tasks.get(uuid));
                tasks.remove(uuid);
            }
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean suspendTask(String uuid) {
        try {
            scheduler.pauseJob(tasks.get(uuid));
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restartTask(String uuid) {
        try {
            scheduler.resumeJob(tasks.get(uuid));
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ProcessResult run() {
        for (String key: tasks.keySet()) {
            try {
                boolean exist = scheduler.checkExists(tasks.get(key));
                if(!exist){
                    listConcurrentHashMap.remove(key);
                    tasks.remove(key);
                }
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
        return new ProcessResult(true,"");
    }
}
