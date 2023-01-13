package br.pro.hashi.sdx.rest.server;

import br.pro.hashi.sdx.rest.Builder;

/**
 * Configures and builds objects of type {@link RestServer}.
 */
public non-sealed class RestServerBuilder extends Builder<RestServerBuilder> {
	/**
	 * Constructs a new builder.
	 */
	public RestServerBuilder() {
	}

	/**
	 * @hidden
	 */
	@Override
	protected RestServerBuilder self() {
		return this;
	}

	/**
	 * Builds a new {@link RestServer} with the current configuration.
	 * 
	 * @param packageName stub
	 * @return the {@link RestServer}
	 */
	public RestServer build(String packageName) {
		return new RestServer(facade);
	}
}
