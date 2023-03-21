package br.pro.hashi.sdx.rest.server.exception;

public class MessageResponseException extends ResponseException {
	private static final long serialVersionUID = -912148051179981197L;

	public MessageResponseException(int status, String message) {
		super(status, message);
	}

	@Override
	public String getBody() {
		return (String) super.getBody();
	}
}
