package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestServerTest {
	private Facade transformer;
	private RestServer s;

	private RestServer newRestServer() {
		return new RestServer(transformer);
	}

	@BeforeEach
	void setUp() {
		transformer = mock(Facade.class);
	}

	@Test
	void stub() {
		s = newRestServer();
		assertNotNull(s.getFacade());
	}
}
