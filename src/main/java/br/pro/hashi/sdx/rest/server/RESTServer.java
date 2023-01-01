package br.pro.hashi.sdx.rest.server;

import br.pro.hashi.sdx.rest.transformer.Transformer;

/**
 * Stub.
 */
public class RESTServer {
	private final Transformer transformer;

	RESTServer(Transformer transformer) {
		this.transformer = transformer;
	}

	Transformer getTransformer() {
		return transformer;
	}
}
