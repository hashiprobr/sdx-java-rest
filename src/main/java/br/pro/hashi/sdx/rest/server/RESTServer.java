package br.pro.hashi.sdx.rest.server;

import br.pro.hashi.sdx.rest.Facade;

/**
 * Stub.
 */
public class RESTServer {
	private final Facade facade;

	RESTServer(Facade facade) {
		this.facade = facade;
	}

	Facade getFacade() {
		return facade;
	}
}
