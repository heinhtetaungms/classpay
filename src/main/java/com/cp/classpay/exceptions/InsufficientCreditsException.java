package com.cp.classpay.exceptions;

public class InsufficientCreditsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InsufficientCreditsException(String message) {
		super(message);
	}
}
