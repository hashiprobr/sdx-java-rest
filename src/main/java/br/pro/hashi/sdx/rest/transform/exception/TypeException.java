package br.pro.hashi.sdx.rest.transform.exception;

/**
 * Thrown to indicate that there is no support for transforming an object into a
 * representation or vice-versa.
 */
public class TypeException extends RuntimeException {
	private static final long serialVersionUID = 398124112466236078L;

	/**
	 * Constructs an {@code TypeException} with no detail message.
	 */
	public TypeException() {
	}

	/**
	 * Constructs an {@code TypeException} with the specified detail message.
	 * 
	 * @param message the detail message
	 */
	public TypeException(String message) {
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
	public TypeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of
	 * {@code (cause == null ? null : cause.toString())}.
	 * 
	 * @param cause the cause
	 */
	public TypeException(Throwable cause) {
		super(cause);
	}
}
