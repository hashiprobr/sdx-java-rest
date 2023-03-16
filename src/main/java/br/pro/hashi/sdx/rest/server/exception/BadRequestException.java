package br.pro.hashi.sdx.rest.server.exception;

import org.eclipse.jetty.http.HttpStatus;

public class BadRequestException extends ResponseException {
	private static final long serialVersionUID = -6428770178527056029L;

	public BadRequestException(String message) {
		super(HttpStatus.BAD_REQUEST_400, message);
	}
}
