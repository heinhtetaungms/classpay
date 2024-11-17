package com.cp.classpay.api.output.user;

import com.cp.classpay.entity.User;

public record TokenResponse (
		String email,
		String accessToken,
		String refreshToken ) {

	public static TokenResponse from(User user, String accessToken, String refreshToken) {
		return new TokenResponse(user.getEmail(),
				accessToken,
				refreshToken);
	}
}
