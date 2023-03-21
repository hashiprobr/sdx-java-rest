package br.pro.hashi.sdx.rest.server.exception;

import org.eclipse.jetty.http.HttpStatus;

public class NotFoundException extends MessageResponseException {
	private static final long serialVersionUID = 8774395330726942543L;

	public NotFoundException() {
		super(HttpStatus.NOT_FOUND_404, "Endpoint not found");
	}
}
