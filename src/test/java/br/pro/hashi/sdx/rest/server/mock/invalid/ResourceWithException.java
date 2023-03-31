package br.pro.hashi.sdx.rest.server.mock.invalid;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ResourceWithException extends RestResource {
	public ResourceWithException() {
		throw new RuntimeException();
	}
}
