package br.pro.hashi.sdx.rest.server.mock.invalid;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ResourceWithBlank extends RestResource {
	public ResourceWithBlank() {
		super(" \t\n");
	}
}
