package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestClientTest {
	private Facade facade;
	private HttpClient jettyClient;
	private RestClient c;

	private RestClient newRestClient(String none) {
		return new RestClient(facade, jettyClient, Coding.CHARSET, none, "http://a");
	}

	@BeforeEach
	void setUp() {
		facade = mock(Facade.class);
		jettyClient = mock(HttpClient.class);
	}

	@Test
	void builds() {
		c = RestClient.to("http://a");
		assertNotNull(c);
	}

	@Test
	void stub() {
		c = newRestClient(null);
		assertSame(facade, c.getFacade());
		assertSame(jettyClient, c.getJettyClient());
		assertEquals(Coding.CHARSET, c.getUrlCharset());
		assertNull(c.getNone());
		assertEquals("http://a", c.getUrlPrefix());
	}
}
