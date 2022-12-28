package br.pro.hashi.sdx.rest.base.exception;

public class ReflectionException extends RuntimeException {
	private static final long serialVersionUID = -8487070526479084137L;

	public ReflectionException(String message) {
		super(message);
	}

	public ReflectionException(Throwable cause) {
		super(cause);
	}
}
