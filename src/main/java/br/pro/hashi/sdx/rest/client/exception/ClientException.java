package br.pro.hashi.sdx.rest.client.exception;

public class ClientException extends RuntimeException {
	private static final long serialVersionUID = -8133070579984492485L;

	public ClientException(String message) {
		super(message);
	}

	public ClientException(Throwable cause) {
		super(cause);
	}
}
