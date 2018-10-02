package com.sky.sdc.qa.config;

public class MissingVariableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MissingVariableException(Throwable e) {
		super(e);
	}

	public MissingVariableException(String message, Throwable e) {
		super(message, e);
	}

	public MissingVariableException(String message) {
		super(message);
	}
}
