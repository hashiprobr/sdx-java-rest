package br.pro.hashi.sdx.rest.server;

import br.pro.hashi.sdx.rest.base.Builder;

/**
 * Configures and creates objects of type {@link RESTServer}.
 */
public class RESTServerBuilder extends Builder<RESTServerBuilder> {
	/**
	 * Constructs a new builder.
	 */
	public RESTServerBuilder() {
	}

	/**
	 * @hidden
	 */
	@Override
	protected RESTServerBuilder self() {
		return this;
	}
}
