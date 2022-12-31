package br.pro.hashi.sdx.rest.client;

import br.pro.hashi.sdx.rest.transformer.Transformer;

/**
 * Stub.
 */
public class RESTClient {
	private final Transformer transformer;
	private final TypeCache cache;

	RESTClient(Transformer transformer, TypeCache cache) {
		this.transformer = transformer;
		this.cache = cache;
	}
}
