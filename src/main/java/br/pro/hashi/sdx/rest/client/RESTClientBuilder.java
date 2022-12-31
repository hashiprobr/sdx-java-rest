package br.pro.hashi.sdx.rest.client;

import br.pro.hashi.sdx.rest.base.Builder;

/**
 * Configures and creates objects of type {@link RESTClient}.
 */
public class RESTClientBuilder extends Builder<RESTClientBuilder> {
	/**
	 * Constructs a new builder.
	 */
	public RESTClientBuilder() {
	}

	/**
	 * @hidden
	 */
	@Override
	protected RESTClientBuilder self() {
		return this;
	}
}
