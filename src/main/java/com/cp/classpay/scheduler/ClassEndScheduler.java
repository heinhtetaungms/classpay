package com.cp.classpay.scheduler;

import com.cp.classpay.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClassEndScheduler {
    @Autowired
    private ClassService classService;

    /**
     * Run daily every hour to update expired packages.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void when_class_end_waitlist_user_credit_need_to_be_refunded() {
        log.info("Updated expired packages job count: {}", "");
    }
}

