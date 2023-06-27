package br.pro.hashi.sdx.rest.transform.exception;

/**
 * Thrown to indicate that an object could not be transformed into a text
 * representation.
 */
public class SerializingException extends RuntimeException {
	private static final long serialVersionUID = 8498822355318130173L;

	/**
	 * Constructs a {@code SerializingException} with no detail message.
	 */
	public SerializingException() {
	}

	/**
	 * Constructs a {@code SerializingException} with the specified detail message.
	 * 
	 * @param message the detail message
	 */
	public SerializingException(String message) {
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
	public SerializingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of
	 * {@code (cause == null ? null : cause.toString())}.
	 * 
	 * @param cause the cause
	 */
	public SerializingException(Throwable cause) {
		super(cause);
	}
}
