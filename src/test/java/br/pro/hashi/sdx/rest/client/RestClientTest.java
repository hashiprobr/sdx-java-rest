package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.client.RestClient.Proxy.Task;
import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestClientTest {
	private Facade facade;
	private HttpClient jettyClient;
	private RestResponse response;
	private RestClient c;
	private RestClient.Proxy p;

	@BeforeEach
	void setUp() {
		facade = mock(Facade.class);
		jettyClient = mock(HttpClient.class);
		response = mock(RestResponse.class);
	}

	@Test
	void builds() {
		try (MockedConstruction<RestClientBuilder> construction = mockBuilderConstruction()) {
			assertSame(c, RestClient.to("http://a"));
			RestClientBuilder builder = construction.constructed().get(0);
			verify(builder).build("http://a");
		}
	}

	private MockedConstruction<RestClientBuilder> mockBuilderConstruction() {
		c = mock(RestClient.class);
		return mockConstruction(RestClientBuilder.class, (mock, context) -> {
			when(mock.build(any())).thenReturn(c);
		});
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
		Throwable cause = new Exception();
		doThrow(cause).when(jettyClient).start();
		Exception exception = assertThrows(ClientException.class, () -> {
			c.start();
		});
		assertSame(cause, exception.getCause());
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
		Throwable cause = new Exception();
		doThrow(cause).when(jettyClient).stop();
		Exception exception = assertThrows(ClientException.class, () -> {
			c.stop();
		});
		assertSame(cause, exception.getCause());
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
	void forwardsGetToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			c.get("/");
			p = construction.constructed().get(0);
			verify(p).get("/");
		}
	}

	@Test
	void forwardsPostToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			c.post("/");
			p = construction.constructed().get(0);
			verify(p).post("/");
		}
	}

	@Test
	void forwardsPostToProxyWithBody() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			c.post("/", body);
			p = construction.constructed().get(0);
			verify(p).post("/", body);
		}
	}

	@Test
	void forwardsPostToProxyWithBodyAndHint() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			Hint<Object> hint = new Hint<Object>() {};
			c.post("/", body, hint);
			p = construction.constructed().get(0);
			verify(p).post("/", body, hint);
		}
	}

	@Test
	void forwardsPutToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			c.put("/");
			p = construction.constructed().get(0);
			verify(p).put("/");
		}
	}

	@Test
	void forwardsPutToProxyWithBody() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			c.put("/", body);
			p = construction.constructed().get(0);
			verify(p).put("/", body);
		}
	}

	@Test
	void forwardsPutToProxyWithBodyAndHint() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			Hint<Object> hint = new Hint<Object>() {};
			c.put("/", body, hint);
			p = construction.constructed().get(0);
			verify(p).put("/", body, hint);
		}
	}

	@Test
	void forwardsPatchToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			c.patch("/");
			p = construction.constructed().get(0);
			verify(p).patch("/");
		}
	}

	@Test
	void forwardsPatchToProxyWithBody() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			c.patch("/", body);
			p = construction.constructed().get(0);
			verify(p).patch("/", body);
		}
	}

	@Test
	void forwardsPatchToProxyWithBodyAndHint() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			Hint<Object> hint = new Hint<Object>() {};
			c.patch("/", body, hint);
			p = construction.constructed().get(0);
			verify(p).patch("/", body, hint);
		}
	}

	@Test
	void forwardsDeleteToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			c.delete("/");
			p = construction.constructed().get(0);
			verify(p).delete("/");
		}
	}

	@Test
	void forwardsRequestToProxy() {
		try (MockedConstruction<RestClient.Proxy> construction = mockConstruction(RestClient.Proxy.class)) {
			c = newRestClient();
			c.request("options", "/");
			p = construction.constructed().get(0);
			verify(p).request("options", "/");
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
	void proxyAddsEncodedQuery() {
		p = newProxy();
		assertSame(p, p.q("?=& %3F%3D%26%20+"));
		assertEquals(1, p.getQueries().size());
		RestClient.Entry entry = p.getQueries().get(0);
		assertEquals("%3F%3D%26+%253F%253D%2526%2520%2B", entry.name());
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
		assertSame(p, p.q("name", 0));
		assertEquals(1, p.getQueries().size());
		RestClient.Entry entry = p.getQueries().get(0);
		assertEquals("name", entry.name());
		assertEquals("0", entry.value());
	}

	@Test
	void proxyAddsEncodedQueryWithValue() {
		p = newProxy();
		assertSame(p, p.q("?=& %3F%3D%26%20+", "%3F%3D%26%20+?=& "));
		assertEquals(1, p.getQueries().size());
		RestClient.Entry entry = p.getQueries().get(0);
		assertEquals("%3F%3D%26+%253F%253D%2526%2520%2B", entry.name());
		assertEquals("%253F%253D%2526%2520%2B%3F%3D%26+", entry.value());
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
		assertSame(p, p.h("name", 0));
		assertEquals(1, p.getHeaders().size());
		RestClient.Entry entry = p.getHeaders().get(0);
		assertEquals("name", entry.name());
		assertEquals("0", entry.value());
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
	void proxyDoesNotAddHeaderIfNameNotInUSASCII() {
		p = newProxy();
		Object value = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h("spéçíál", value);
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
	void proxyDoesNotAddHeaderIfValueStringNotInUSASCII() {
		p = newProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h("name", "spéçíál");
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
	void proxyAddsBodyWithHint() {
		p = newProxy();
		Object body = new Object();
		assertSame(p, p.b(body, new Hint<Object>() {}));
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

	@Test
	void proxyGets() {
		p = spyNewProxy();
		doReturn(response).when(p).doRequest("GET", "/");
		assertSame(response, p.get("/"));
	}

	@Test
	void proxyPosts() {
		p = spyNewProxy();
		doReturn(response).when(p).doRequest("POST", "/");
		assertSame(response, p.post("/"));
	}

	@Test
	void proxyPostsWithBody() {
		p = spyNewProxy();
		Object body = new Object();
		doReturn(response).when(p).doRequest("POST", "/");
		assertSame(response, p.post("/", body));
		verify(p).withBody(body);
	}

	@Test
	void proxyPostsWithBodyAndHint() {
		p = spyNewProxy();
		Object body = new Object();
		Hint<Object> hint = new Hint<Object>() {};
		doReturn(response).when(p).doRequest("POST", "/");
		assertSame(response, p.post("/", body, hint));
		verify(p).withBody(body, hint);
	}

	@Test
	void proxyPuts() {
		p = spyNewProxy();
		doReturn(response).when(p).doRequest("PUT", "/");
		assertSame(response, p.put("/"));
	}

	@Test
	void proxyPutsWithBody() {
		p = spyNewProxy();
		Object body = new Object();
		doReturn(response).when(p).doRequest("PUT", "/");
		assertSame(response, p.put("/", body));
		verify(p).withBody(body);
	}

	@Test
	void proxyPutsWithBodyAndHint() {
		p = spyNewProxy();
		Object body = new Object();
		Hint<Object> hint = new Hint<Object>() {};
		doReturn(response).when(p).doRequest("PUT", "/");
		assertSame(response, p.put("/", body, hint));
		verify(p).withBody(body, hint);
	}

	@Test
	void proxyPatches() {
		p = spyNewProxy();
		doReturn(response).when(p).doRequest("PATCH", "/");
		assertSame(response, p.patch("/"));
	}

	@Test
	void proxyPatchesWithBody() {
		p = spyNewProxy();
		Object body = new Object();
		doReturn(response).when(p).doRequest("PATCH", "/");
		assertSame(response, p.patch("/", body));
		verify(p).withBody(body);
	}

	@Test
	void proxyPatchesWithBodyAndHint() {
		p = spyNewProxy();
		Object body = new Object();
		Hint<Object> hint = new Hint<Object>() {};
		doReturn(response).when(p).doRequest("PATCH", "/");
		assertSame(response, p.patch("/", body, hint));
		verify(p).withBody(body, hint);
	}

	@Test
	void proxyDeletes() {
		p = spyNewProxy();
		doReturn(response).when(p).doRequest("DELETE", "/");
		assertSame(response, p.delete("/"));
	}

	@Test
	void proxyRequests() {
		p = spyNewProxy();
		doReturn(response).when(p).doRequest("OPTIONS", "/");
		assertSame(response, p.request("options", "/"));
	}

	@Test
	void proxyDoesNotRequestIfMethodIsNull() {
		p = spyNewProxy();
		assertThrows(NullPointerException.class, () -> {
			p.request(null, "/");
		});
		verify(p, times(0)).doRequest(any(), any());
	}

	@Test
	void proxyDoesNotRequestIfMethodIsBlank() {
		p = spyNewProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.request(" \t\n", "/");
		});
		verify(p, times(0)).doRequest(any(), any());
	}

	@Test
	void proxyDoesRequest() {
		p = spyNewProxy();
		doReturn("/?x").when(p).withQueries("/");
		Request request = mock(Request.class);
		when(jettyClient.newRequest("http://a/?x")).thenReturn(request);
		when(request.method("OPTIONS")).thenReturn(request);
		doNothing().when(p).addHeaders(request);
		List<Task> tasks = new ArrayList<>();
		doReturn(tasks).when(p).addBodiesAndGetTasks(request);
		doReturn(response).when(p).send(eq(request), eq(tasks));
		assertSame(response, p.doRequest("OPTIONS", "/"));
	}

	@Test
	void proxyDoesNotDoRequestIfUriIsNull() {
		p = spyNewProxy();
		assertThrows(NullPointerException.class, () -> {
			p.doRequest("OPTIONS", null);
		});
		verify(p, times(0)).withQueries(any());
		verify(jettyClient, times(0)).newRequest(any(String.class));
	}

	@Test
	void proxyDoesNotDoRequestIfUriIsBlank() {
		p = spyNewProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.doRequest("OPTIONS", " \t\n");
		});
		verify(p, times(0)).withQueries(any());
		verify(jettyClient, times(0)).newRequest(any(String.class));
	}

	@Test
	void proxyDoesNotDoRequestIfUriDoesNotStartWithSlash() {
		p = spyNewProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.doRequest("OPTIONS", "a");
		});
		verify(p, times(0)).withQueries(any());
		verify(jettyClient, times(0)).newRequest(any(String.class));
	}

	@ParameterizedTest
	@CsvSource({
			"/,                /",
			"/,                /?",
			"/?%2F,            /?/",
			"/?%2F%2F%2F,      /?///",
			"/,                ///",
			"/,                ///?",
			"/?%2F,            ///?/",
			"/?%2F%2F%2F,      ///?///",
			"/a,               /a/",
			"/a,               /a/?",
			"/a?%2F,           /a/?/",
			"/a?%2F%2F%2F,     /a/?///",
			"/a,               /a///",
			"/a,               /a///?",
			"/a?%2F,           /a///?/",
			"/a?%2F%2F%2F,     /a///?///",
			"/abc,             /abc/",
			"/abc,             /abc/?",
			"/abc?%2F,         /abc/?/",
			"/abc?%2F%2F%2F,   /abc/?///",
			"/abc,             /abc///",
			"/abc,             /abc///?",
			"/abc?%2F,         /abc///?/",
			"/abc?%2F%2F%2F,   /abc///?///",
			"/a/b/c,           /a/b/c/",
			"/a/b/c,           /a/b/c/?",
			"/a/b/c?%2F,       /a/b/c/?/",
			"/a/b/c?%2F%2F%2F, /a/b/c/?///",
			"/a/b/c,           /a/b/c///",
			"/a/b/c,           /a/b/c///?",
			"/a/b/c?%2F,       /a/b/c///?/",
			"/a/b/c?%2F%2F%2F, /a/b/c///?///" })
	void proxyStripsEndingSlashesBeforeQuestionMark(String expected, String uri) {
		p = spyNewProxy();
		assertEquals(expected, p.withQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"/?&,              /?&",
			"/?&&&,            /?&&&",
			"/?x&y,            /?x&y",
			"/?x&y&z,          /?x&y&z",
			"/%26,             /&",
			"/%26,             /&?",
			"/%26?&,           /&?&",
			"/%26?&&&,         /&?&&&",
			"/%26?x&y,         /&?x&y",
			"/%26?x&y&z,       /&?x&y&z",
			"/%26%26%26,       /&&&",
			"/%26%26%26,       /&&&?",
			"/%26%26%26?&,     /&&&?&",
			"/%26%26%26?&&&,   /&&&?&&&",
			"/%26%26%26?x&y,   /&&&?x&y",
			"/%26%26%26?x&y&z, /&&&?x&y&z",
			"/a%26b%26c,       /a&b&c",
			"/a%26b%26c,       /a&b&c?",
			"/a%26b%26c?&,     /a&b&c?&",
			"/a%26b%26c?&&&,   /a&b&c?&&&",
			"/a%26b%26c?x&y,   /a&b&c?x&y",
			"/a%26b%26c?x&y&z, /a&b&c?x&y&z" })
	void proxyEncodesAmpersandBeforeQuestionMark(String expected, String uri) {
		p = spyNewProxy();
		assertEquals(expected, p.withQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"/?=,            /?=",
			"/?=%3D%3D,      /?===",
			"/?x=y,          /?x=y",
			"/?x=y%3Dz,      /?x=y=z",
			"/%3D,           /=",
			"/%3D,           /=?",
			"/%3D?=,         /=?=",
			"/%3D?=%3D%3D,   /=?===",
			"/%3D?x=y,       /=?x=y",
			"/%3D?x=y%3Dz,   /=?x=y=z",
			"/a%3Db,         /a=b",
			"/a%3Db,         /a=b?",
			"/a%3Db?=,       /a=b?=",
			"/a%3Db?=%3D%3D, /a=b?===",
			"/a%3Db?x=y,     /a=b?x=y",
			"/a%3Db?x=y%3Dz, /a=b?x=y=z" })
	void proxyEncodesEqualsSignBeforeQuestionMarkOrAfterFirstOccurence(String expected, String uri) {
		p = spyNewProxy();
		assertEquals(expected, p.withQueries(uri));
	}

	@Test
	void proxyPercentEncodesBeforeQuestionMarkAndQueryEncodesAfterQuestionMark() {
		p = spyNewProxy();
		assertEquals("/%2B%20%26%3D%3F%20%26%3D?%2F++%26%3D%3F+&=%3F", p.withQueries("/+%20%26%3D%3F &=?/+%20%26%3D%3F &=?"));
	}

	@ParameterizedTest
	@CsvSource({
			"/?y,      /",
			"/?x&y,    /?x",
			"/?x=s&y,  /?x=s",
			"/a?y,     /a",
			"/a?x&y,   /a?x",
			"/a?x=s&y, /a?x=s" })
	void proxyAddsOneQuery(String expected, String uri) {
		p = spyNewProxy();
		p.withQuery("y");
		assertEquals(expected, p.withQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"/?y&z,      /",
			"/?x&y&z,    /?x",
			"/?x=s&y&z,  /?x=s",
			"/a?y&z,     /a",
			"/a?x&y&z,   /a?x",
			"/a?x=s&y&z, /a?x=s" })
	void proxyAddsTwoQueries(String expected, String uri) {
		p = spyNewProxy();
		p.withQuery("y");
		p.withQuery("z");
		assertEquals(expected, p.withQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"/?y&z=2.3,      /",
			"/?x&y&z=2.3,    /?x",
			"/?x=s&y&z=2.3,  /?x=s",
			"/a?y&z=2.3,     /a",
			"/a?x&y&z=2.3,   /a?x",
			"/a?x=s&y&z=2.3, /a?x=s" })
	void proxyAddsOneQueryAndOneQueryWithValue(String expected, String uri) {
		p = spyNewProxy();
		p.withQuery("y");
		p.withQuery("z", 2.3);
		assertEquals(expected, p.withQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"/?y=1,      /",
			"/?x&y=1,    /?x",
			"/?x=s&y=1,  /?x=s",
			"/a?y=1,     /a",
			"/a?x&y=1,   /a?x",
			"/a?x=s&y=1, /a?x=s" })
	void proxyAddsOneQueryWithValue(String expected, String uri) {
		p = spyNewProxy();
		p.withQuery("y", 1);
		assertEquals(expected, p.withQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"/?y=1&z,      /",
			"/?x&y=1&z,    /?x",
			"/?x=s&y=1&z,  /?x=s",
			"/a?y=1&z,     /a",
			"/a?x&y=1&z,   /a?x",
			"/a?x=s&y=1&z, /a?x=s" })
	void proxyAddsOneQueryWithValueAndOneQuery(String expected, String uri) {
		p = spyNewProxy();
		p.withQuery("y", 1);
		p.withQuery("z");
		assertEquals(expected, p.withQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"/?y=1&z=2.3,      /",
			"/?x&y=1&z=2.3,    /?x",
			"/?x=s&y=1&z=2.3,  /?x=s",
			"/a?y=1&z=2.3,     /a",
			"/a?x&y=1&z=2.3,   /a?x",
			"/a?x=s&y=1&z=2.3, /a?x=s" })
	void proxyAddsTwoQueriesWithValue(String expected, String uri) {
		p = spyNewProxy();
		p.withQuery("y", 1);
		p.withQuery("z", 2.3);
		assertEquals(expected, p.withQueries(uri));
	}

	private RestClient.Proxy spyNewProxy() {
		return spy(newProxy());
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
