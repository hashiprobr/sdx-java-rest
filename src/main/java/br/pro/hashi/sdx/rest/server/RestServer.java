package br.pro.hashi.sdx.rest.server;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

/**
 * Stub.
 */
public class RestServer {
	private final Facade facade;

	RestServer(Facade facade) {
		this.facade = facade;
	}

	Facade getFacade() {
		return facade;
	}
}
