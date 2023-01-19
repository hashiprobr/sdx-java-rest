package br.pro.hashi.sdx.rest.server;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

/**
 * Main object for receiving REST requests.
 */
public final class RestServer {
	/**
	 * Instantiates a default REST server using the resources of a specified
	 * package.
	 * 
	 * @param packageName the package name
	 * @return the server
	 */
	public static RestServer from(String packageName) {
		return builder().build(packageName);
	}

	/**
	 * Convenience method for instantiating a REST server builder.
	 * 
	 * @return the server builder
	 */
	public static RestServerBuilder builder() {
		return new RestServerBuilder();
	}

	private final Facade facade;

	RestServer(Facade facade) {
		this.facade = facade;
	}

	Facade getFacade() {
		return facade;
	}
}
