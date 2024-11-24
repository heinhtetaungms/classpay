package com.cp.classpay.service.cache;

import com.cp.classpay.commons.enum_.TokenType;
import com.cp.classpay.entity.User;
import com.cp.classpay.repository.UserRepo;
import com.cp.classpay.security.token.JwtTokenParser;
import com.cp.classpay.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserCacheService {

    private static final String USER_CACHE_KEY_PREFIX = "user:";

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private JwtTokenParser jwtTokenParser;
    @Autowired
    private UserRepo userRepo;

    public void cacheUser(User user) {
        String cacheKey = USER_CACHE_KEY_PREFIX + user.getEmail();
        redisUtil.setHash(cacheKey, user, 1, TimeUnit.HOURS);
    }

    public Optional<User> getUserFromCache(String email) {
        String cacheKey = USER_CACHE_KEY_PREFIX + email;
        User user = redisUtil.getHash(cacheKey, User.class);
        return Optional.ofNullable(user);
    }


    public User getUser(String jwtToken) {
        // Validate JWT token and retrieve user
        var authentication = jwtTokenParser.parse(TokenType.Access, jwtToken);
        var user = findByEmail(authentication.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user;
    }

    //TODO
    public User getUser(Long userId) {
        return userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Optional<User> findByEmail(String email) {
        // Attempt to retrieve user from cache
        Optional<User> cachedUser = getUserFromCache(email);
        if (cachedUser.isPresent()) {
            return cachedUser;
        }

        // If not in cache, load from database
        Optional<User> userFromDb = userRepo.findByEmail(email);
        // Cache user after retrieval
        userFromDb.ifPresent(this::cacheUser);
        return userFromDb;
    }
}
