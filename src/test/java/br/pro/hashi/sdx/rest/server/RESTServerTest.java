package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transformer.Transformer;

class RESTServerTest {
	private Transformer transformer;
	private RESTServer s;

	private RESTServer newRESTServer() {
		return new RESTServer(transformer);
	}

	@BeforeEach
	void setUp() {
		transformer = mock(Transformer.class);
	}

	@Test
	void stub() {
		s = newRESTServer();
		assertNotNull(s);
	}
}
