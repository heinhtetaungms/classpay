package com.cp.classpay.scheduler;

import com.cp.classpay.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemindClassStartTimeScheduler {
    @Autowired
    private ClassService classService;

    @Scheduled(cron = "0 */5 * * * *")
    public void remindClassStartTimeToUser() {
        classService.remindClassStartTimeToUser();
    }
}

