package com.cp.classpay.service;

import com.cp.classpay.entity.User;

public interface UserService {
    User findByEmail(String email);
    User findByEmailOrElse(String email);
    User save(User user);
}