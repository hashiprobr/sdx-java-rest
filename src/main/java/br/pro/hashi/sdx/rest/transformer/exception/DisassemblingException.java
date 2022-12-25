package br.pro.hashi.sdx.rest.transformer.exception;

import br.pro.hashi.sdx.rest.transformer.base.Disassembler;

/**
 * Thrown to indicate that a {@link Disassembler} cannot transform a byte
 * representation into an object.
 */
public class DisassemblingException extends Exception {
	private static final long serialVersionUID = -2917639720204125909L;

	/**
	 * Constructs a {@code DisassemblingException} with no detail message.
	 */
	public DisassemblingException() {
	}

	/**
	 * Constructs a {@code DisassemblingException} with the specified detail
	 * message.
	 * 
	 * @param message the detail message.
	 */
	public DisassemblingException(String message) {
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
	 * @param message the detail message.
	 * @param cause   the cause.
	 */
	public DisassemblingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of
	 * {@code (cause == null ? null : cause.toString())}.
	 * 
	 * @param cause the cause.
	 */
	public DisassemblingException(Throwable cause) {
		super(cause);
	}
}
