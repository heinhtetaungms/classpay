package com.cp.classpay.exceptions;

public class Api_Rate_Limited_Exception extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public Api_Rate_Limited_Exception(String message) {
		super(message);
	}
}
