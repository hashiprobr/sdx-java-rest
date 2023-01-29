package br.pro.hashi.sdx.rest.fields.exception;

public class FieldsException extends RuntimeException {
	private static final long serialVersionUID = 3060624636475024309L;

	public FieldsException(String message) {
		super(message);
	}

	public FieldsException(Throwable cause) {
		super(cause);
	}
}
