package br.pro.hashi.sdx.rest.reflection.exception;

public class ReflectionException extends RuntimeException {
	private static final long serialVersionUID = -2688008540145108945L;

	public ReflectionException(String message) {
		super(message);
	}

	public ReflectionException(Throwable cause) {
		super(cause);
	}
}
