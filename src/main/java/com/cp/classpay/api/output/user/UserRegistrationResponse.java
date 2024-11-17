package com.cp.classpay.api.output.user;

import com.cp.classpay.entity.User;
import lombok.Getter;
import lombok.Setter;

public record UserRegistrationResponse (
     String username,
     String email,
     String country) {

    public static UserRegistrationResponse from(User user) {
        return new UserRegistrationResponse(
                user.getUsername(),
                user.getEmail(),
                user.getCountry());
    }
}
