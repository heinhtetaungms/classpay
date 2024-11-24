package com.cp.classpay.utils;

import com.cp.classpay.commons.enum_.TokenType;
import com.cp.classpay.entity.User;
import com.cp.classpay.security.token.JwtTokenParser;
import com.cp.classpay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EssentialUtil {
    @Autowired
    private JwtTokenParser jwtTokenParser;
    @Autowired
    private UserService userService;

    public User getUser(String jwtToken) {
        // Validate JWT token and retrieve user
        var authentication = jwtTokenParser.parse(TokenType.Access, jwtToken);
        var user = userService.findByEmail(authentication.getName());
        return user;
    }
}
