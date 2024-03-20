package com.lyk.coursearrange.common;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author: 15760
 * @Date: 2020/6/7
 * @Descripe: 定时任务
 */
@Component
public class TimerTask {

    /**
     * 每3小时执行一次
     */
    @Scheduled(cron = "0 0 0-3 * * ? ")
    public void task() {

    }
}
