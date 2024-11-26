package com.cp.classpay.service.cache;

import com.cp.classpay.api.input.waitlist.WaitlistEntry;
import com.cp.classpay.entity.Class;
import com.cp.classpay.entity.User;
import com.cp.classpay.utils.RedisUtil;
import org.springframework.stereotype.Service;

@Service
public class WaitlistCacheService {

    private final RedisUtil redisUtil;

    public WaitlistCacheService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    private static final String WAITLIST_KEY_PREFIX = "class_waitlist:";

    public void addToWaitlist(User user, Class classEntity) {
        WaitlistEntry waitlistEntry= new WaitlistEntry(classEntity.getClassId(), user.getEmail());
        String waitlistKey = WAITLIST_KEY_PREFIX + waitlistEntry.getClassId();
        redisUtil.pushToQueue(waitlistKey, waitlistEntry);
    }

    public WaitlistEntry getFromWaitlist(long classId) {
        String waitlistKey = WAITLIST_KEY_PREFIX + classId;
        return redisUtil.popFromQueue(waitlistKey, WaitlistEntry.class);
    }
}
