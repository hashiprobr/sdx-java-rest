package br.pro.hashi.sdx.rest.server;

import br.pro.hashi.sdx.rest.Builder;

/**
 * Configures and builds objects of type {@link RESTServer}.
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

	/**
	 * Builds a new {@link RESTServer} with the current configuration.
	 * 
	 * @return the {@link RESTServer}.
	 */
	public RESTServer build() {
		return new RESTServer(facade);
	}
}
