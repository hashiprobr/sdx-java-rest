package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transformer.Transformer;

class RESTClientTest {
	private Transformer transformer;
	private TypeCache cache;
	private RESTClient c;

	@BeforeEach
	void setUp() {
		transformer = mock(Transformer.class);
		cache = mock(TypeCache.class);
		c = new RESTClient(transformer, cache);
	}

	@Test
	void stub() {
		assertNotNull(c);
	}
}
