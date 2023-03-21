package br.pro.hashi.sdx.rest.server.exception;

import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.Hint;

public class ResponseException extends RuntimeException {
	private static final long serialVersionUID = -6573374380902177381L;

	private final int status;
	private final Object body;
	private final Type type;

	public ResponseException(int status, Object body) {
		this(status, body, body.getClass());
	}

	public <T> ResponseException(int status, T body, Hint<T> hint) {
		this(status, body, hint.getType());
	}

	private ResponseException(int status, Object body, Type type) {
		if (status < 400 || status > 599) {
			throw new IllegalArgumentException("Status must be between 400 and 599");
		}
		this.status = status;
		this.body = body;
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public Object getBody() {
		return body;
	}

	public Type getType() {
		return type;
	}
}
