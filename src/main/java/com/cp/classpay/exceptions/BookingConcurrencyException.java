package com.cp.classpay.exceptions;

public class BookingConcurrencyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BookingConcurrencyException(String message) {
		super(message);
	}
}
