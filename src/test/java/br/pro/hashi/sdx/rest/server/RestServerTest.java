package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestServerTest {
	private Facade facade;
	private RestServer s;

	@BeforeEach
	void setUp() {
		facade = mock(Facade.class);
	}

	@Test
	void builds() {
		s = RestServer.from("package");
		assertNotNull(s);
	}

	@Test
	void constructs() {
		s = newRestServer();
		assertSame(facade, s.getFacade());
	}

	private RestServer newRestServer() {
		return new RestServer(facade);
	}
}
