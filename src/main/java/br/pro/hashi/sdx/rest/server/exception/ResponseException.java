package br.pro.hashi.sdx.rest.server.exception;

public class ResponseException extends RuntimeException {
	private static final long serialVersionUID = 5245051152097777010L;

	private int status;

	public ResponseException(int status, String message) {
		super(message);
		if (status < 400 || status > 599) {
			throw new IllegalArgumentException("Status must be between 400 and 599");
		}
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
