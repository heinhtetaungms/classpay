package com.cp.classpay.service.cache;

import com.cp.classpay.entity.Class;
import com.cp.classpay.repository.ClassRepo;
import com.cp.classpay.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ClassCacheService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ClassRepo classRepo;

    private static final String CLASS_CACHE_KEY_PREFIX = "class:";
    private static final String AVAILABLE_CLASS_CACHE_KEY_PREFIX = "available_class:";

    public Set<Long> classesEndingAroundNow() {
        Set<Long> classesEndingAroundNow = redisUtil.getClassesEndingAroundNow(60);
        return classesEndingAroundNow;
    }

    public Class saveClass(Class clazz) {
        Class savedClass = classRepo.save(clazz);

        redisUtil.saveClassEndDate(savedClass.getClassId(), savedClass.getClassEndDate());
        cacheClassEntity(savedClass);

        updateCacheForAvailableClassesByCountry(savedClass.getCountry());
        return savedClass;
    }

    public Optional<Class> getClassEntity(Long classId) {
        String cacheKey = CLASS_CACHE_KEY_PREFIX + classId;
        Class cachedClass = redisUtil.getHash(cacheKey, Class.class);

        if (cachedClass == null) {
            cachedClass = classRepo.findById(classId).orElseThrow(RuntimeException::new);
            cacheClassEntity(cachedClass);
        }
        return Optional.ofNullable(cachedClass);
    }

    private void cacheClassEntity(Class classEntity) {
        String cacheKey = CLASS_CACHE_KEY_PREFIX + classEntity.getClassId();
        redisUtil.setHash(cacheKey, classEntity, 1, TimeUnit.HOURS);
    }

    public void updateCacheClassEntity(Class classEntity) {
        String cacheKey = CLASS_CACHE_KEY_PREFIX + classEntity.getClassId();
        redisUtil.delete(cacheKey);
        redisUtil.setHash(cacheKey, classEntity, 1, TimeUnit.HOURS);
    }

    public List<Class> updateCacheForAvailableClassesByCountry(String classCountry) {
        String cacheKey = AVAILABLE_CLASS_CACHE_KEY_PREFIX + classCountry;
        redisUtil.delete(cacheKey);
        List<Class> updatedClasses = classRepo.findAllByCountry(classCountry);
        cacheAvailableClasses(cacheKey, updatedClasses);
        return updatedClasses;
    }

    public List<Class> getAvailableClassesByCountry(String classCountry) {
        String cacheKey = AVAILABLE_CLASS_CACHE_KEY_PREFIX + classCountry;

        List<Class> cachedClasses = redisUtil.getList(cacheKey, Class.class);

        if (cachedClasses == null || cachedClasses.isEmpty()) {
            cachedClasses = classRepo.findAllByCountry(classCountry);
            cacheAvailableClasses(cacheKey, cachedClasses);
        }

        return cachedClasses;
    }

    private void cacheAvailableClasses(String cacheKey, List<Class> classList) {
        redisUtil.delete(cacheKey);
        redisUtil.setList(cacheKey, classList, 1, TimeUnit.HOURS);
    }

}
