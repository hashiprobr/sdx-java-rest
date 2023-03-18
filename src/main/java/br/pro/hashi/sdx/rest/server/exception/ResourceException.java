package br.pro.hashi.sdx.rest.server.exception;

public class ResourceException extends RuntimeException {
	private static final long serialVersionUID = 3168314054819247556L;

	public ResourceException(String typeName, String message) {
		super("%s: %s".formatted(typeName, message));
	}
}
