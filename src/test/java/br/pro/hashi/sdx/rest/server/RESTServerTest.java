package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transformer.Transformer;

class RESTServerTest {
	private Transformer transformer;
	private RESTServer s;

	@BeforeEach
	void setUp() {
		transformer = mock(Transformer.class);
		s = new RESTServer(transformer);
	}

	@Test
	void stub() {
		assertNotNull(s);
	}
}
