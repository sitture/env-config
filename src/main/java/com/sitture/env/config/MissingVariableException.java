package com.sitture.env.config;

public class MissingVariableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MissingVariableException(Throwable e) {
		super(e);
	}

	public MissingVariableException(String message, Throwable error) {
		super(message, error);
	}

	public MissingVariableException(String message) {
		super(message);
	}
}
