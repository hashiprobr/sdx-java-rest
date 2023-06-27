package br.pro.hashi.sdx.rest.transform.exception;

/**
 * Thrown to indicate that an object could not be transformed into a byte
 * representation.
 */
public class AssemblingException extends RuntimeException {
	private static final long serialVersionUID = 796929757741188672L;

	/**
	 * Constructs a {@code AssemblingException} with no detail message.
	 */
	public AssemblingException() {
	}

	/**
	 * Constructs a {@code AssemblingException} with the specified detail message.
	 * 
	 * @param message the detail message
	 */
	public AssemblingException(String message) {
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
	public AssemblingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of
	 * {@code (cause == null ? null : cause.toString())}.
	 * 
	 * @param cause the cause
	 */
	public AssemblingException(Throwable cause) {
		super(cause);
	}
}
