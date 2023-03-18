package br.pro.hashi.sdx.rest.server.mock.invalid;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ResourceWithoutCompliance extends RestResource {
	public ResourceWithoutCompliance() {
		super("//b");
	}
}
