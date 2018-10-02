package com.sky.sdc.qa.config;

public class ConfigException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param e
	 *            a {@link java.lang.Throwable} object.
	 */
	public ConfigException(Throwable e) {
		super(e);
	}

	/**
	 * @param message
	 *            a {@link java.lang.String} object.
	 * @param e
	 *            a {@link java.lang.Throwable} object.
	 */
	public ConfigException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * @param message
	 *            a {@link java.lang.String} object.
	 */
	public ConfigException(String message) {
		super(message);
	}

}
