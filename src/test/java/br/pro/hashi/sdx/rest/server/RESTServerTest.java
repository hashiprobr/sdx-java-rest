package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Facade;

class RESTServerTest {
	private Facade transformer;
	private RESTServer s;

	private RESTServer newRESTServer() {
		return new RESTServer(transformer);
	}

	@BeforeEach
	void setUp() {
		transformer = mock(Facade.class);
	}

	@Test
	void stub() {
		s = newRESTServer();
		assertNotNull(s.getFacade());
	}
}
