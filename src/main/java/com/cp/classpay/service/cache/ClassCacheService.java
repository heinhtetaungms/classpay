package com.cp.classpay.service.cache;

import com.cp.classpay.entity.Class;
import com.cp.classpay.repository.ClassRepo;
import com.cp.classpay.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ClassCacheService {

    private final ClassRepo classRepo;
    private final RedisUtil redisUtil;

    public ClassCacheService(ClassRepo classRepo, RedisUtil redisUtil) {
        this.classRepo = classRepo;
        this.redisUtil = redisUtil;
    }

    @Value("${app.redis.class_e.key_prefix}")
    private String class_e_key_prefix;
    @Value("${app.redis.class_e.key_ttl}")
    private long class_e_key_ttl;

    @Value("${app.redis.class_l.key_prefix}")
    private String class_l_key_prefix;
    @Value("${app.redis.class_l.key_ttl}")
    private long class_l_key_ttl;

    public Set<Long> classesEndingAroundNow() {
        Set<Long> classesEndingAroundNow = redisUtil.getClassesEndingAroundNow(60);
        return classesEndingAroundNow;
    }

    public Class save(Class clazz) {
        Class record = classRepo.save(clazz);

        String key = class_e_key_prefix + record.getClassId();
        set(key, record);

        zSetAddForClassEndDate(record);

        update_available_class_list_by_country(record);

        return record;
    }

    public Class findById(Long classId) {
        String key = class_e_key_prefix + classId;
        Class record = redisUtil.getHash(key, Class.class);

        if (record == null) {
            record = classRepo.findByClassId(classId);
            set(key, record);
        }
        return record;
    }

    public List<Class> findAllByCountry(String classCountry) {
        String key = class_l_key_prefix + classCountry;
        List<Class> recordList = redisUtil.getList(key, Class.class);

        if (recordList.isEmpty()) {
            recordList = classRepo.findAllByCountry(classCountry);
            setList(key, recordList);
        }
        return recordList;
    }

    private List<Class> update_available_class_list_by_country(Class clazz) {
        String key = class_l_key_prefix + clazz.getCountry();
        List<Class> recordList = redisUtil.getList(key, Class.class);

        if (recordList.isEmpty()) {
            //will invoke db hit only once to consistence with db if redis key is deleted
            recordList = classRepo.findAllByCountry(clazz.getCountry());
        }
        List<Class> updatedList = recordList.stream().filter(record -> !record.getClassId().equals(clazz.getClassId())).collect(Collectors.toList());

        updatedList.add(clazz);
        setList(key, updatedList);

        return updatedList;
    }

    private void set(String key, Class classEntity) {
        redisUtil.setHash(key, classEntity, class_e_key_ttl, TimeUnit.MINUTES);
    }

    private void setList(String key, List<Class> classList) {
        redisUtil.setList(key, classList, class_l_key_ttl, TimeUnit.MINUTES);
    }

    private void zSetAddForClassEndDate(Class clazz) {
        redisUtil.saveClassEndDate(clazz.getClassId(), clazz.getClassEndDate());
    }
}
