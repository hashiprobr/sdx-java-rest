package br.pro.hashi.sdx.rest.server.exception;

import br.pro.hashi.sdx.rest.server.RestException;

public class MessageRestException extends RestException {
	private static final long serialVersionUID = 4037858011082212492L;

	public MessageRestException(int status, String message) {
		super(status, message);
	}
}
