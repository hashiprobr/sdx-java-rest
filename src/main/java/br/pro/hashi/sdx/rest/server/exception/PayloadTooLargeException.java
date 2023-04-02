package br.pro.hashi.sdx.rest.server.exception;

import org.eclipse.jetty.http.HttpStatus;

public class PayloadTooLargeException extends MessageRestException {
	private static final long serialVersionUID = 5685831474227698125L;

	public PayloadTooLargeException(String message) {
		super(HttpStatus.PAYLOAD_TOO_LARGE_413, message);
	}
}
