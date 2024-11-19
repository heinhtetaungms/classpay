package com.cp.classpay.security.token;


import com.cp.classpay.api.input.auth.TokenRefreshForm;
import com.cp.classpay.api.input.auth.TokenRequestForm;
import com.cp.classpay.api.output.auth.TokenResponse;
import com.cp.classpay.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenManagementService {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenParser jwtTokenParser;

	@Autowired
	private JwtTokenGenerator jwtTokenGenerator;

	@Autowired
	private UserRepo userRepo;

	@Transactional(readOnly = true)
	public TokenResponse generate(TokenRequestForm form) {

		var usernamePasswordToken = UsernamePasswordAuthenticationToken.unauthenticated(form.email(), form.password());
		var authentication = authenticationManager.authenticate(usernamePasswordToken);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		return getResponse(authentication);
	}

	@Transactional(readOnly = true)
	public TokenResponse refresh(TokenRefreshForm form) {
		var authentication = jwtTokenParser.parse(TokenType.Refresh, form.refreshToken());
		return getResponse(authentication);
	}


	private TokenResponse getResponse(Authentication authentication) {

		var user = userRepo.findByEmail(authentication.getName())
				.orElseThrow(() -> new UsernameNotFoundException("Invalid login id."));

		var accessToken = jwtTokenGenerator.generate(TokenType.Access, authentication);
		var refreshToken = jwtTokenGenerator.generate(TokenType.Refresh, authentication);

		return TokenResponse.from(user, accessToken, refreshToken);
	}
}
