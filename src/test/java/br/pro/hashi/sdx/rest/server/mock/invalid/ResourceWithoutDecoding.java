package br.pro.hashi.sdx.rest.server.mock.invalid;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ResourceWithoutDecoding extends RestResource {
	public ResourceWithoutDecoding() {
		super("/%b");
	}
}
