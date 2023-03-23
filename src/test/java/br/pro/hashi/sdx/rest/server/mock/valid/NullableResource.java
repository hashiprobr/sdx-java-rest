package br.pro.hashi.sdx.rest.server.mock.valid;

import br.pro.hashi.sdx.rest.server.RestResource;

public class NullableResource extends RestResource {
	public NullableResource() {
		nullable(null);
	}
}
