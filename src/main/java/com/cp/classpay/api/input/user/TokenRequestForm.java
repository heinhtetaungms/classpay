package com.cp.classpay.api.input.user;

import jakarta.validation.constraints.NotBlank;

public record TokenRequestForm(
		@NotBlank(message = "Please enter login id.")
		String email,
		@NotBlank(message = "Please enter password.")
		String password) {

}
