package br.pro.hashi.sdx.rest.server.exception;

import org.eclipse.jetty.http.HttpStatus;

public class NotAcceptableException extends MessageRestException {
	private static final long serialVersionUID = -753310828379233857L;

	public NotAcceptableException(String message) {
		super(HttpStatus.NOT_ACCEPTABLE_406, message);
	}
}
