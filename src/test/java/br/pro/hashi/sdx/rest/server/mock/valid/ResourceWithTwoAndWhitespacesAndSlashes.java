package br.pro.hashi.sdx.rest.server.mock.valid;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ResourceWithTwoAndWhitespacesAndSlashes extends RestResource {
	public ResourceWithTwoAndWhitespacesAndSlashes() {
		super(" \t\n/c/d/// \t\n");
	}
}
