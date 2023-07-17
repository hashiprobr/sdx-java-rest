package br.pro.hashi.sdx.rest.server;

import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.Hint;

/**
 * Thrown to indicate that an error occurred in an endpoint.
 */
public class RestException extends RuntimeException {
	private static final long serialVersionUID = 8263284704228720673L;

	private static Type getType(Object body) {
		if (body == null) {
			throw new NullPointerException("Body cannot be null");
		}
		Class<?> type = body.getClass();
		if (type.getTypeParameters().length > 0) {
			throw new IllegalArgumentException("Body type cannot be generic");
		}
		return type;
	}

	private static Type getType(Hint<?> hint) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		return hint.getType();
	}

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	private final int status;

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	private final Object body;

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	private final Type type;

	/**
	 * <p>
	 * Constructs a {@code RestException} with a status and a body.
	 * </p>
	 * <p>
	 * This constructor calls {@code body.getClass()} to obtain the body type, so do
	 * not call it if the body is null. Also, since {@code body.getClass()} loses
	 * generic information due to type erasure, do not call it if the type is
	 * generic. In both cases, call {@code RestException(int, T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param status the status
	 * @param body   the body
	 * @throws NullPointerException     if the body is null
	 * @throws IllegalArgumentException if the status is invalid or if the body type
	 *                                  is generic
	 */
	public RestException(int status, Object body) {
		this(status, body, getType(body));
	}

	/**
	 * <p>
	 * Constructs a {@code RestException} with a status and a body with hinted type.
	 * </p>
	 * <p>
	 * Call this constructor if the body is null or the type is generic.
	 * </p>
	 * 
	 * @param <T>    the body type
	 * @param status the status
	 * @param body   the body
	 * @param hint   a {@link Hint} representing {@code T}
	 * @throws NullPointerException     if the hint is null
	 * @throws IllegalArgumentException if the status is invalid
	 */
	public <T> RestException(int status, T body, Hint<T> hint) {
		this(status, body, getType(hint));
	}

	private RestException(int status, Object body, Type type) {
		if (status < 400 || status > 599) {
			throw new IllegalArgumentException("Status must be between 400 and 599");
		}
		this.status = status;
		this.body = body;
		this.type = type;
	}

	/**
	 * Obtains the status code of this exception.
	 * 
	 * @return the status
	 */
	public final int getStatus() {
		return status;
	}

	/**
	 * Obtains the response body of this exception.
	 * 
	 * @return the body
	 */
	public final Object getBody() {
		return body;
	}

	/**
	 * Obtains the type of the response body.
	 * 
	 * @return the type
	 */
	public final Type getType() {
		return type;
	}
}
