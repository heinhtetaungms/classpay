package com.cp.classpay.api.output.auth;

import com.cp.classpay.entity.User;

public record UserProfileResponse (
        String username,
        String email,
        String password,
        String country,
        boolean isVerified) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getCountry(),
                user.isVerified());
    }
}
