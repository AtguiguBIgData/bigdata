/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class SpringContextHelper implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHelper.applicationContext = applicationContext;
    }


    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static <T>T getBean(String beanName , Class<T>clazz) {
        return applicationContext.getBean(beanName , clazz);
    }

    public static <T>T getBean(Class<T>clazz) {
        return applicationContext.getBean(clazz);
    }
}