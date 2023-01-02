package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.transformer.Transformer;

class RESTClientTest {
	private Transformer transformer;
	private HttpClient jettyClient;
	private RESTClient c;

	private RESTClient newRESTClient(String none) {
		return new RESTClient(transformer, jettyClient, Coding.CHARSET, none, "http://a");
	}

	@BeforeEach
	void setUp() {
		transformer = mock(Transformer.class);
		jettyClient = mock(HttpClient.class);
	}

	@Test
	void stub() {
		c = newRESTClient(null);
		assertNotNull(c);
	}
}
