package com.cp.classpay.service.impl;

import com.cp.classpay.entity.User;
import com.cp.classpay.repository.UserRepo;
import com.cp.classpay.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User findByEmail(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Invalid Email."));
        return user;
    }

    @Override
    public User findByEmailOrElse(String email) {
        User user = userRepo.findByEmail(email).orElse(null);
        return user;
    }

    @Override
    public User save(User user) {
        return userRepo.save(user);
    }
}
