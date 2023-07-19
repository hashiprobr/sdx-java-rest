package br.pro.hashi.sdx.rest.transform.exception;

/**
 * Thrown to indicate that a text representation could not be transformed back
 * into an object.
 */
public class DeserializingException extends RuntimeException {
	private static final long serialVersionUID = 8782038150308982536L;

	/**
	 * Constructs a {@code DeserializingException} with no detail message.
	 */
	public DeserializingException() {
	}

	/**
	 * Constructs a {@code DeserializingException} with the specified detail
	 * message.
	 *
	 * @param message the detail message
	 */
	public DeserializingException(String message) {
		super(message);
	}

	/**
	 * <p>
	 * Constructs a new exception with the specified detail message and cause.
	 * </p>
	 * <p>
	 * Note that the detail message associated with cause is not automatically
	 * incorporated in this exception's detail message.
	 * </p>
	 *
	 * @param message the detail message
	 * @param cause   the cause
	 */
	public DeserializingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of
	 * {@code (cause == null ? null : cause.toString())}.
	 *
	 * @param cause the cause
	 */
	public DeserializingException(Throwable cause) {
		super(cause);
	}
}
