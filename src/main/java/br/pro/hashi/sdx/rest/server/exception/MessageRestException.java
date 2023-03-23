package br.pro.hashi.sdx.rest.server.exception;

import br.pro.hashi.sdx.rest.server.RestException;

public class MessageRestException extends RestException {
	private static final long serialVersionUID = 211083894090645557L;

	public MessageRestException(int status, String message) {
		super(status, message);
	}

	@Override
	public String getBody() {
		return (String) super.getBody();
	}
}
