package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestServerTest {
	private Facade facade;
	private RestServer s;

	private RestServer newRestServer() {
		return new RestServer(facade);
	}

	@BeforeEach
	void setUp() {
		facade = mock(Facade.class);
	}

	@Test
	void stub() {
		s = newRestServer();
		assertSame(facade, s.getFacade());
	}
}
