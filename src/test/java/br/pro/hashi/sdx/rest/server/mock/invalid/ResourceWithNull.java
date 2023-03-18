package br.pro.hashi.sdx.rest.server.mock.invalid;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ResourceWithNull extends RestResource {
	public ResourceWithNull() {
		super(null);
	}
}
