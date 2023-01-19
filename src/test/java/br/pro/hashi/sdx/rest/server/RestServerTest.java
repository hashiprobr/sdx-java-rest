package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class RestServerTest {
	private RestServer s;

	@Test
	void constructs() {
		s = RestServer.from("package");
		assertNotNull(s);
	}
}
