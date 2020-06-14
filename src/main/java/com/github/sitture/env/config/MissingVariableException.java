package com.github.sitture.env.config;

public class MissingVariableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MissingVariableException(final Throwable error) {
		super(error);
	}

	public MissingVariableException(final String message, final Throwable error) {
		super(message, error);
	}

	public MissingVariableException(final String message) {
		super(message);
	}
}
