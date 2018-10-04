package com.sitture.env.config;

public class ConfigException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new runtime exception.
	 * @param error
	 *            a {@link java.lang.Throwable} object.
	 */
	public ConfigException(Throwable error) {
		super(error);
	}

	/**
	 * Constructs a new runtime exception.
	 * @param message
	 *            a {@link java.lang.String} object.
	 * @param error
	 *            a {@link java.lang.Throwable} object.
	 */
	public ConfigException(String message, Throwable error) {
		super(message, error);
	}

	/**
	 * Constructs a new runtime exception.
	 * @param message
	 *            a {@link java.lang.String} object.
	 */
	public ConfigException(String message) {
		super(message);
	}

}
