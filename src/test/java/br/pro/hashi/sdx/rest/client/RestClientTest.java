package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestClientTest {
	private Facade facade;
	private HttpClient jettyClient;
	private RestClient c;
	private RestClient.Proxy p;

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
	void constructs() {
		c = newRestClient();
		assertSame(facade, c.getFacade());
		assertSame(jettyClient, c.getJettyClient());
		assertEquals(StandardCharsets.UTF_8, c.getUrlCharset());
		assertNull(c.getNone());
		assertEquals("http://a", c.getUrlPrefix());
	}

	@Test
	void proxyInitializesWithoutQueries() {
		p = newProxy();
		assertTrue(p.getQueries().isEmpty());
	}

	@Test
	void proxyInitializesWithoutHeaders() {
		p = newProxy();
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void proxyInitializesWithoutBodies() {
		p = newProxy();
		assertTrue(p.getBodies().isEmpty());
	}

	@Test
	void proxyInitializesWithoutTimeout() {
		p = newProxy();
		assertEquals(0, p.getTimeout());
	}

	@Test
	void proxyAddsQuery() {
		p = newProxy();
		assertSame(p, p.q("name"));
		assertEquals(1, p.getQueries().size());
		RestClient.Entry entry = p.getQueries().get(0);
		assertEquals("name", entry.name());
		assertNull(entry.value());
	}

	@Test
	void proxyDoesNotAddQuery() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.q(null);
		});
		assertTrue(p.getQueries().isEmpty());
	}

	@Test
	void proxyAddsQueryWithValue() {
		p = newProxy();
		Object value = new Object();
		assertSame(p, p.q("name", value));
		assertEquals(1, p.getQueries().size());
		RestClient.Entry entry = p.getQueries().get(0);
		assertEquals("name", entry.name());
		assertSame(value, entry.value());
	}

	@Test
	void proxyDoesNotAddQueryWithValueIfNameIsNull() {
		p = newProxy();
		Object value = new Object();
		assertThrows(NullPointerException.class, () -> {
			p.q(null, value);
		});
		assertTrue(p.getQueries().isEmpty());
	}

	@Test
	void proxyDoesNotAddQueryWithValueIfValueIsNull() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.q("name", null);
		});
		assertTrue(p.getQueries().isEmpty());
	}

	@Test
	void proxyAddsHeader() {
		p = newProxy();
		Object value = new Object();
		assertSame(p, p.h("name", value));
		assertEquals(1, p.getHeaders().size());
		RestClient.Entry entry = p.getHeaders().get(0);
		assertEquals("name", entry.name());
		assertSame(value, entry.value());
	}

	@Test
	void proxyDoesNotAddHeaderIfNameIsNull() {
		p = newProxy();
		Object value = new Object();
		assertThrows(NullPointerException.class, () -> {
			p.h(null, value);
		});
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void proxyDoesNotAddHeaderIfNameIsBlank() {
		p = newProxy();
		Object value = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h(" \t\n", value);
		});
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void proxyDoesNotAddHeaderIfValueIsNull() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.h("name", null);
		});
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void proxyAddsBody() {
		p = newProxy();
		Object body = new Object();
		assertSame(p, p.b(body));
		List<Body> bodies = p.getBodies();
		assertEquals(1, bodies.size());
		assertSame(body, bodies.get(0).getActual());
	}

	@Test
	void proxyAddsWrappedBody() {
		p = newProxy();
		Object actual = new Object();
		assertSame(p, p.b(new Body(actual)));
		List<Body> bodies = p.getBodies();
		assertEquals(1, bodies.size());
		assertSame(actual, bodies.get(0).getActual());
	}

	@Test
	void proxyAddsBodyWithHint() {
		p = newProxy();
		Object body = new Object();
		assertSame(p, p.b(body, new Hint<Object>() {}));
		List<Body> bodies = p.getBodies();
		assertEquals(1, bodies.size());
		assertSame(body, bodies.get(0).getActual());
	}

	@Test
	void proxyAddsWrappedBodyWithHint() {
		p = newProxy();
		Object actual = new Object();
		assertSame(p, p.b(new Body(actual), new Hint<Body>() {}));
		List<Body> bodies = p.getBodies();
		assertEquals(1, bodies.size());
		assertSame(actual, bodies.get(0).getActual());
	}

	@Test
	void proxySetsTimeout() {
		p = newProxy();
		assertSame(p, p.t(1));
		assertEquals(1, p.getTimeout());
	}

	@Test
	void proxyDoesNotSetTimeout() {
		p = newProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.t(-1);
		});
		assertEquals(0, p.getTimeout());
	}

	private RestClient.Proxy newProxy() {
		return newRestClient().new Proxy();
	}

	private RestClient newRestClient() {
		return newRestClient(null);
	}

	private RestClient newRestClient(String none) {
		return new RestClient(facade, jettyClient, StandardCharsets.UTF_8, none, "http://a");
	}
}
