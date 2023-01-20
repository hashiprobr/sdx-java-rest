package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.Coding;
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
	void constructs() {
		c = RestClient.to("http://a");
		assertNotNull(c);
	}

	@Test
	void starts() throws Exception {
		c = newRestClient();
		when(jettyClient.isRunning()).thenReturn(false);
		c.start();
		verify(jettyClient).start();
	}

	@Test
	void doesNotStartIfJettyClientAlreadyStarted() throws Exception {
		c = newRestClient();
		when(jettyClient.isRunning()).thenReturn(true);
		c.start();
		verify(jettyClient, times(0)).start();
	}

	@Test
	void doesNotStartIfJettyClientThrowsException() throws Exception {
		c = newRestClient();
		when(jettyClient.isRunning()).thenReturn(false);
		doThrow(Exception.class).when(jettyClient).start();
		assertThrows(ClientException.class, () -> {
			c.start();
		});
	}

	@Test
	void stops() throws Exception {
		c = newRestClient();
		when(jettyClient.isRunning()).thenReturn(true);
		c.stop();
		verify(jettyClient).stop();
	}

	@Test
	void doesNotStopIfJettyClientAlreadyStopped() throws Exception {
		c = newRestClient();
		when(jettyClient.isRunning()).thenReturn(false);
		c.stop();
		verify(jettyClient, times(0)).stop();
	}

	@Test
	void doesNotStopIfJettyClientThrowsException() throws Exception {
		c = newRestClient();
		when(jettyClient.isRunning()).thenReturn(true);
		doThrow(Exception.class).when(jettyClient).stop();
		assertThrows(ClientException.class, () -> {
			c.stop();
		});
	}

	@Test
	void forwardsWithQueryToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			c.q("name");
			p = construction.constructed().get(0);
			verify(p).withQuery("name");
		}
	}

	@Test
	void forwardsWithQueryToProxyWithValue() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object value = new Object();
			c.q("name", value);
			p = construction.constructed().get(0);
			verify(p).withQuery("name", value);
		}
	}

	@Test
	void forwardsWithHeaderToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object value = new Object();
			c.h("name", value);
			p = construction.constructed().get(0);
			verify(p).withHeader("name", value);
		}
	}

	@Test
	void forwardsWithBodyToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			c.b(body);
			p = construction.constructed().get(0);
			verify(p).withBody(body);
		}
	}

	@Test
	void forwardsWithBodyToProxyWithHint() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			Hint<Object> hint = new Hint<Object>() {};
			c.b(body, hint);
			p = construction.constructed().get(0);
			verify(p).withBody(body, hint);
		}
	}

	@Test
	void forwardsWithTimeoutToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			c.t(1);
			p = construction.constructed().get(0);
			verify(p).withTimeout(1);
		}
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
		return new RestClient(facade, jettyClient, StandardCharsets.UTF_8, Coding.LOCALE, none, "http://a");
	}
}
