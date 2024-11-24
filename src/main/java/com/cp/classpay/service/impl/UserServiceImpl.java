package com.cp.classpay.service.impl;

import com.cp.classpay.entity.User;
import com.cp.classpay.repository.UserRepo;
import com.cp.classpay.service.UserService;
import com.cp.classpay.service.cache.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private UserRepo userRepo;


    @Override
    public User findByEmail(String email) {
        User user = userCacheService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Invalid Email."));
        return user;
    }

    @Override
    public User findByEmailOrElse(String email) {
        User user = userCacheService.findByEmail(email).orElse(null);
        return user;
    }

    @Override
    public User save(User user) {
        // Save to database first
        User savedUser = userRepo.save(user);

        // Cache the saved user
        userCacheService.cacheUser(savedUser);
        return savedUser;
    }


}
