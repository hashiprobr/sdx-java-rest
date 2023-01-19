package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestClientTest {
	private Facade facade;
	private HttpClient jettyClient;
	private RestClient c;

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

	private RestClient newRestClient() {
		return newRestClient(null);
	}

	private RestClient newRestClient(String none) {
		return new RestClient(facade, jettyClient, StandardCharsets.UTF_8, none, "http://a");
	}
}
