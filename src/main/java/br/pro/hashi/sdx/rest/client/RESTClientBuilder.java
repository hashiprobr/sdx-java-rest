package br.pro.hashi.sdx.rest.client;

import br.pro.hashi.sdx.rest.base.Builder;

/**
 * Configures and builds objects of type {@link RESTClient}.
 */
public class RESTClientBuilder extends Builder<RESTClientBuilder> {
	private final TypeCache cache;

	/**
	 * Constructs a new builder.
	 */
	public RESTClientBuilder() {
		super(RESTClientBuilder.class);
		this.cache = new TypeCache();
	}

	/**
	 * @hidden
	 */
	@Override
	protected RESTClientBuilder self() {
		return this;
	}

	/**
	 * Builds a new {@link RESTClient} with the current configuration.
	 * 
	 * @return the {@link RESTClient}.
	 */
	public RESTClient build() {
		return new RESTClient(transformer, cache);
	}
}
