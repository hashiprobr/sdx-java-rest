package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Facade;
import br.pro.hashi.sdx.rest.coding.Coding;

class RestClientTest {
	private Facade transformer;
	private HttpClient jettyClient;
	private RestClient c;

	private RestClient newRestClient(String none) {
		return new RestClient(transformer, jettyClient, Coding.CHARSET, none, "http://a");
	}

	@BeforeEach
	void setUp() {
		transformer = mock(Facade.class);
		jettyClient = mock(HttpClient.class);
	}

	@Test
	void stub() {
		c = newRestClient(null);
		assertNotNull(c.getFacade());
		assertNotNull(c.getUrlCharset());
	}
}
