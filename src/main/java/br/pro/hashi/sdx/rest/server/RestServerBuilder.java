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
	 * Builds a REST server using the resources of a specified package.
	 * 
	 * @param packageName the package name
	 * @return the REST server
	 */
	public RestServer build(String packageName) {
		return new RestServer(facade);
	}

	/**
	 * @hidden
	 */
	@Override
	protected RestServerBuilder self() {
		return this;
	}
}
