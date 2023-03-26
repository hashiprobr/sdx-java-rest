package br.pro.hashi.sdx.rest.server.mock.valid;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ResourceWithOneAndWhitespacesAndSlashes extends RestResource {
	public ResourceWithOneAndWhitespacesAndSlashes() {
		super(" \t\n/c/// \t\n");
	}

	public void get() {
	}
}
