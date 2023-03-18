package br.pro.hashi.sdx.rest.server.mock.invalid;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ResourceWithoutSlash extends RestResource {
	public ResourceWithoutSlash() {
		super("b");
	}
}
