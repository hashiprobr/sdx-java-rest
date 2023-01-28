package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Request.Content;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.MultiPartRequestContent;
import org.eclipse.jetty.client.util.OutputStreamRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.client.RestClient.Proxy;
import br.pro.hashi.sdx.rest.client.RestClient.Proxy.Entry;
import br.pro.hashi.sdx.rest.client.RestClient.Proxy.Task;
import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.server.exception.ServerException;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestClientTest {
	private static final String USASCII_BODY = "special";
	private static final String SPECIAL_BODY = "spéçíál";

	private Facade facade;
	private HttpClient jettyClient;
	private Request request;
	private Response response;
	private RestClient c;
	private Proxy p;

	@BeforeEach
	void setUp() {
		facade = mock(Facade.class);
		jettyClient = mock(HttpClient.class);
		request = mock(Request.class);
		response = mock(Response.class);
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
			when(mock.build("http://a")).thenReturn(c);
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
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			c.q("name");
			p = construction.constructed().get(0);
			verify(p).withQuery("name");
		}
	}

	@Test
	void forwardsWithQueryToProxyWithValue() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			Object value = new Object();
			c.q("name", value);
			p = construction.constructed().get(0);
			verify(p).withQuery("name", value);
		}
	}

	@Test
	void forwardsWithHeaderToProxy() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			Object value = new Object();
			c.h("name", value);
			p = construction.constructed().get(0);
			verify(p).withHeader("name", value);
		}
	}

	@Test
	void forwardsWithBodyToProxy() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			c.b(body);
			p = construction.constructed().get(0);
			verify(p).withBody(body);
		}
	}

	@Test
	void forwardsWithBodyToProxyWithHint() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
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
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			c.t(1);
			p = construction.constructed().get(0);
			verify(p).withTimeout(1);
		}
	}

	@Test
	void forwardsGetToProxy() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			c.get("/");
			p = construction.constructed().get(0);
			verify(p).get("/");
		}
	}

	@Test
	void forwardsPostToProxy() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			c.post("/");
			p = construction.constructed().get(0);
			verify(p).post("/");
		}
	}

	@Test
	void forwardsPostToProxyWithBody() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			c.post("/", body);
			p = construction.constructed().get(0);
			verify(p).post("/", body);
		}
	}

	@Test
	void forwardsPostToProxyWithBodyAndHint() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
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
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			c.put("/");
			p = construction.constructed().get(0);
			verify(p).put("/");
		}
	}

	@Test
	void forwardsPutToProxyWithBody() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			c.put("/", body);
			p = construction.constructed().get(0);
			verify(p).put("/", body);
		}
	}

	@Test
	void forwardsPutToProxyWithBodyAndHint() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
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
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			c.patch("/");
			p = construction.constructed().get(0);
			verify(p).patch("/");
		}
	}

	@Test
	void forwardsPatchToProxyWithBody() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			Object body = new Object();
			c.patch("/", body);
			p = construction.constructed().get(0);
			verify(p).patch("/", body);
		}
	}

	@Test
	void forwardsPatchToProxyWithBodyAndHint() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
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
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
			c = newRestClient();
			c.delete("/");
			p = construction.constructed().get(0);
			verify(p).delete("/");
		}
	}

	@Test
	void forwardsRequestToProxy() {
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class)) {
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
		Entry entry = p.getQueries().get(0);
		assertEquals("name", entry.name());
		assertNull(entry.valueString());
	}

	@Test
	void proxyAddsEncodedQuery() {
		p = newProxy();
		assertSame(p, p.q("?=& %3F%3D%26%20+"));
		assertEquals(1, p.getQueries().size());
		Entry entry = p.getQueries().get(0);
		assertEquals("%3F%3D%26+%253F%253D%2526%2520%2B", entry.name());
		assertNull(entry.valueString());
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
		Entry entry = p.getQueries().get(0);
		assertEquals("name", entry.name());
		assertEquals("0", entry.valueString());
	}

	@Test
	void proxyAddsEncodedQueryWithValue() {
		p = newProxy();
		assertSame(p, p.q("?=& %3F%3D%26%20+", "%3F%3D%26%20+?=& "));
		assertEquals(1, p.getQueries().size());
		Entry entry = p.getQueries().get(0);
		assertEquals("%3F%3D%26+%253F%253D%2526%2520%2B", entry.name());
		assertEquals("%253F%253D%2526%2520%2B%3F%3D%26+", entry.valueString());
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
	void proxyDoesNotAddQueryWithValueIfValueStringIsNull() {
		p = newProxy();
		Object value = new Object() {
			@Override
			public String toString() {
				return null;
			}
		};
		assertThrows(NullPointerException.class, () -> {
			p.q("name", value);
		});
		assertTrue(p.getQueries().isEmpty());
	}

	@Test
	void proxyAddsHeader() {
		p = newProxy();
		assertSame(p, p.h("name", 0));
		assertEquals(1, p.getHeaders().size());
		Entry entry = p.getHeaders().get(0);
		assertEquals("name", entry.name());
		assertEquals("0", entry.valueString());
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
	void proxyDoesNotAddHeaderIfValueStringIsNull() {
		p = newProxy();
		Object value = new Object() {
			@Override
			public String toString() {
				return null;
			}
		};
		assertThrows(NullPointerException.class, () -> {
			p.h("name", value);
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
		List<RestBody> bodies = p.getBodies();
		assertEquals(1, bodies.size());
		assertSame(body, bodies.get(0).getActual());
	}

	@Test
	void proxyAddsBodyWithHint() {
		p = newProxy();
		Object body = new Object();
		assertSame(p, p.b(body, new Hint<Object>() {}));
		List<RestBody> bodies = p.getBodies();
		assertEquals(1, bodies.size());
		assertSame(body, bodies.get(0).getActual());
	}

	@Test
	void proxyAddsWrappedBody() {
		p = newProxy();
		Object actual = new Object();
		assertSame(p, p.b(new RestBody(actual)));
		List<RestBody> bodies = p.getBodies();
		assertEquals(1, bodies.size());
		assertSame(actual, bodies.get(0).getActual());
	}

	@Test
	void proxyAddsWrappedBodyWithHint() {
		p = newProxy();
		Object actual = new Object();
		assertSame(p, p.b(new RestBody(actual), new Hint<RestBody>() {}));
		List<RestBody> bodies = p.getBodies();
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
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("GET", "/");
		assertSame(restResponse, p.get("/"));
	}

	@Test
	void proxyPosts() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("POST", "/");
		assertSame(restResponse, p.post("/"));
	}

	@Test
	void proxyPostsWithBody() {
		p = spyNewProxy();
		Object body = new Object();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("POST", "/");
		assertSame(restResponse, p.post("/", body));
		verify(p).withBody(body);
	}

	@Test
	void proxyPostsWithBodyAndHint() {
		p = spyNewProxy();
		Object body = new Object();
		Hint<Object> hint = new Hint<Object>() {};
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("POST", "/");
		assertSame(restResponse, p.post("/", body, hint));
		verify(p).withBody(body, hint);
	}

	@Test
	void proxyPuts() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("PUT", "/");
		assertSame(restResponse, p.put("/"));
	}

	@Test
	void proxyPutsWithBody() {
		p = spyNewProxy();
		Object body = new Object();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("PUT", "/");
		assertSame(restResponse, p.put("/", body));
		verify(p).withBody(body);
	}

	@Test
	void proxyPutsWithBodyAndHint() {
		p = spyNewProxy();
		Object body = new Object();
		Hint<Object> hint = new Hint<Object>() {};
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("PUT", "/");
		assertSame(restResponse, p.put("/", body, hint));
		verify(p).withBody(body, hint);
	}

	@Test
	void proxyPatches() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("PATCH", "/");
		assertSame(restResponse, p.patch("/"));
	}

	@Test
	void proxyPatchesWithBody() {
		p = spyNewProxy();
		Object body = new Object();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("PATCH", "/");
		assertSame(restResponse, p.patch("/", body));
		verify(p).withBody(body);
	}

	@Test
	void proxyPatchesWithBodyAndHint() {
		p = spyNewProxy();
		Object body = new Object();
		Hint<Object> hint = new Hint<Object>() {};
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("PATCH", "/");
		assertSame(restResponse, p.patch("/", body, hint));
		verify(p).withBody(body, hint);
	}

	@Test
	void proxyDeletes() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("DELETE", "/");
		assertSame(restResponse, p.delete("/"));
	}

	@Test
	void proxyRequests() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("OPTIONS", "/");
		assertSame(restResponse, p.request("options", "/"));
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
		when(jettyClient.newRequest("http://a/?x")).thenReturn(request);
		when(request.method("OPTIONS")).thenReturn(request);
		doNothing().when(p).addHeaders(request);
		List<Task> tasks = List.of();
		doReturn(tasks).when(p).addBodiesAndGetTasks(request);
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).send(same(request), same(tasks));
		assertSame(restResponse, p.doRequest("OPTIONS", "/"));
	}

	@Test
	void proxyDoesNotDoRequestIfUriIsNull() {
		p = spyNewProxy();
		assertThrows(NullPointerException.class, () -> {
			p.doRequest("OPTIONS", null);
		});
		verify(p, times(0)).withQueries(any());
		verify(jettyClient, times(0)).newRequest((String) any());
	}

	@Test
	void proxyDoesNotDoRequestIfUriIsBlank() {
		p = spyNewProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.doRequest("OPTIONS", " \t\n");
		});
		verify(p, times(0)).withQueries(any());
		verify(jettyClient, times(0)).newRequest((String) any());
	}

	@Test
	void proxyDoesNotDoRequestIfUriDoesNotStartWithSlash() {
		p = spyNewProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.doRequest("OPTIONS", "a");
		});
		verify(p, times(0)).withQueries(any());
		verify(jettyClient, times(0)).newRequest((String) any());
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
		p = newProxy();
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
		p = newProxy();
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
		p = newProxy();
		assertEquals(expected, p.withQueries(uri));
	}

	@Test
	void proxyPercentEncodesBeforeQuestionMarkAndQueryEncodesAfterQuestionMark() {
		p = newProxy();
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
		p = newProxy();
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
		p = newProxy();
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
		p = newProxy();
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
		p = newProxy();
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
		p = newProxy();
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
		p = newProxy();
		p.withQuery("y", 1);
		p.withQuery("z", 2.3);
		assertEquals(expected, p.withQueries(uri));
	}

	@Test
	void proxyAddsHeaders() {
		p = newProxy();
		p.withHeader("x", "s");
		p.withHeader("y", 1);
		p.withHeader("z", 2.3);
		HttpFields.Mutable fields = mock(HttpFields.Mutable.class);
		doAnswer((invocation) -> {
			Consumer<HttpFields> consumer = invocation.getArgument(0);
			consumer.accept(fields);
			return null;
		}).when(request).headers(any());
		p.addHeaders(request);
		verify(fields).add("x", "s");
		verify(fields).add("y", "1");
		verify(fields).add("z", "2.3");
		verifyNoMoreInteractions(fields);
	}

	@Test
	void proxyAddsZeroBodiesAndGetsZeroTasks() {
		p = spyNewProxy();
		mockContents();
		List<Task> tasks = p.addBodiesAndGetTasks(request);
		verify(request, times(0)).body(any());
		assertTrue(tasks.isEmpty());
	}

	@Test
	void proxyAddsOneBodyAndGetsOneTask() {
		try (MockedConstruction<MultiPartRequestContent> construction = mockConstruction(MultiPartRequestContent.class)) {
			p = spyNewProxy();
			p.withBody(new Object());
			List<Content> contents = mockContents();
			List<Task> tasks = p.addBodiesAndGetTasks(request);
			assertTrue(construction.constructed().isEmpty());
			verify(request).body(contents.get(0));
			assertEquals(1, tasks.size());
		}
	}

	@Test
	void proxyAddsTwoBodiesAndGetsTwoTasks() {
		try (MockedConstruction<MultiPartRequestContent> construction = mockConstruction(MultiPartRequestContent.class)) {
			p = spyNewProxy();
			p.withBody(new RestBody(new Object()).withName("one"));
			p.withBody(new RestBody(new Object()).withName("two"));
			List<Content> contents = mockContents();
			List<Task> tasks = p.addBodiesAndGetTasks(request);
			MultiPartRequestContent content = construction.constructed().get(0);
			verify(content).addFieldPart("one", contents.get(0), null);
			verify(content).addFieldPart("two", contents.get(1), null);
			verify(content).close();
			verifyNoMoreInteractions(content);
			verify(request).body(content);
			assertEquals(2, tasks.size());
		}
	}

	private List<Content> mockContents() {
		List<Content> contents = new ArrayList<>();
		doAnswer((invocation) -> {
			List<Task> tasks = invocation.getArgument(0);
			tasks.add(mock(Task.class));
			Content content = mock(Content.class);
			contents.add(content);
			return content;
		}).when(p).addTaskAndGetContent(any(), any());
		return contents;
	}

	@Test
	void proxyAddsTaskAndGetsContent() {
		try (MockedConstruction<OutputStreamRequestContent> construction = mockContentConstruction()) {
			RestBody body = new RestBody(SPECIAL_BODY).in(StandardCharsets.ISO_8859_1);
			OutputStreamRequestContent content = (OutputStreamRequestContent) mockContent(body);
			assertEquals("type/subtype;charset=ISO-8859-1", content.getContentType());
			byte[] bytes = ((ByteArrayOutputStream) content.getOutputStream()).toByteArray();
			assertEquals(SPECIAL_BODY, new String(bytes, StandardCharsets.ISO_8859_1));
		}
	}

	@Test
	void proxyAddsTaskAndGetsContentInBase64() {
		try (MockedConstruction<OutputStreamRequestContent> construction = mockContentConstruction()) {
			RestBody body = new RestBody(SPECIAL_BODY).in(StandardCharsets.UTF_8).inBase64();
			OutputStreamRequestContent content = (OutputStreamRequestContent) mockContent(body);
			assertEquals("type/subtype;charset=UTF-8;base64", content.getContentType());
			byte[] bytes = ((ByteArrayOutputStream) content.getOutputStream()).toByteArray();
			assertEquals(SPECIAL_BODY, new String(Base64.getDecoder().decode(bytes), StandardCharsets.UTF_8));
		}
	}

	private Content mockContent(RestBody body) {
		p = newProxy();
		List<Task> tasks = new ArrayList<>();
		Object actual = body.getActual();
		Serializer serializer = mock(Serializer.class);
		when(facade.isBinary(String.class)).thenReturn(false);
		when(facade.cleanForSerializing(null, actual)).thenReturn("type/subtype");
		when(facade.getSerializer("type/subtype")).thenReturn(serializer);
		Content content = p.addTaskAndGetContent(tasks, body);
		assertEquals(1, tasks.size());
		Task task = tasks.get(0);
		doAnswer((invocation) -> {
			String str = invocation.getArgument(0);
			Writer writer = invocation.getArgument(2);
			writer.write(str);
			writer.close();
			return null;
		}).when(serializer).write(same(actual), eq(String.class), any());
		task.consumer().accept(task.stream());
		return content;
	}

	@Test
	void proxyAddsTaskAndGetsBinaryContent() {
		try (MockedConstruction<OutputStreamRequestContent> construction = mockContentConstruction()) {
			RestBody body = new RestBody(USASCII_BODY);
			OutputStreamRequestContent content = (OutputStreamRequestContent) mockBinaryContent(body);
			assertEquals("type/subtype", content.getContentType());
			byte[] bytes = ((ByteArrayOutputStream) content.getOutputStream()).toByteArray();
			assertEquals(USASCII_BODY, new String(bytes, StandardCharsets.US_ASCII));
		}
	}

	@Test
	void proxyAddsTaskAndGetsBinaryContentInBase64() {
		try (MockedConstruction<OutputStreamRequestContent> construction = mockContentConstruction()) {
			RestBody body = new RestBody(USASCII_BODY).inBase64();
			OutputStreamRequestContent content = (OutputStreamRequestContent) mockBinaryContent(body);
			assertEquals("type/subtype;base64", content.getContentType());
			byte[] bytes = ((ByteArrayOutputStream) content.getOutputStream()).toByteArray();
			assertEquals(USASCII_BODY, new String(Base64.getDecoder().decode(bytes), StandardCharsets.US_ASCII));
		}
	}

	private Content mockBinaryContent(RestBody body) {
		p = newProxy();
		List<Task> tasks = new ArrayList<>();
		Object actual = body.getActual();
		Assembler assembler = mock(Assembler.class);
		when(facade.isBinary(String.class)).thenReturn(true);
		when(facade.cleanForAssembling(null, actual)).thenReturn("type/subtype");
		when(facade.getAssembler("type/subtype")).thenReturn(assembler);
		Content content = p.addTaskAndGetContent(tasks, body);
		assertEquals(1, tasks.size());
		Task task = tasks.get(0);
		doAnswer((invocation) -> {
			String str = invocation.getArgument(0);
			OutputStream stream = invocation.getArgument(2);
			stream.write(str.getBytes(StandardCharsets.US_ASCII));
			stream.close();
			return null;
		}).when(assembler).write(same(actual), eq(String.class), any());
		task.consumer().accept(task.stream());
		return content;
	}

	private MockedConstruction<OutputStreamRequestContent> mockContentConstruction() {
		return mockConstruction(OutputStreamRequestContent.class, (mock, context) -> {
			when(mock.getContentType()).thenReturn((String) context.arguments().get(0));
			when(mock.getOutputStream()).thenReturn(new ByteArrayOutputStream());
		});
	}

	@Test
	void proxySends() {
		try (MockedConstruction<InputStreamResponseListener> construction = mockListenerConstruction()) {
			p = newProxy();
			Consumer<OutputStream> consumer = (stream) -> {
				try {
					stream.write(USASCII_BODY.getBytes(StandardCharsets.US_ASCII));
					stream.close();
				} catch (IOException exception) {
					throw new AssertionError(exception);
				}
			};
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			List<Task> tasks = List.of(new Task(consumer, stream));
			HttpFields fields = mock(HttpFields.class);
			when(fields.get("Content-Type")).thenReturn("type/subtype");
			when(response.getStatus()).thenReturn(600);
			when(response.getHeaders()).thenReturn(fields);
			RestResponse restResponse = p.send(request, tasks);
			InputStreamResponseListener listener = construction.constructed().get(0);
			verify(request).send(listener);
			assertEquals(USASCII_BODY, new String(stream.toByteArray(), StandardCharsets.US_ASCII));
			assertEquals(600, restResponse.getStatus());
			assertEquals(fields, restResponse.getFields());
			assertEquals("type/subtype", restResponse.getContentType());
			assertEquals(listener.getInputStream(), restResponse.getStream());
		}
	}

	private MockedConstruction<InputStreamResponseListener> mockListenerConstruction() {
		return mockConstruction(InputStreamResponseListener.class, (mock, context) -> {
			when(mock.get(0, TimeUnit.SECONDS)).thenReturn(response);
			when(mock.getInputStream()).thenReturn(InputStream.nullInputStream());
		});
	}

	@Test
	void proxyDoesNotSendIfCloseThrows() throws IOException {
		try (MockedConstruction<InputStreamResponseListener> construction = mockConstruction(InputStreamResponseListener.class)) {
			p = newProxy();
			Consumer<OutputStream> consumer = (stream) -> {};
			OutputStream stream = spy(OutputStream.nullOutputStream());
			IOException cause = new IOException();
			doThrow(cause).when(stream).close();
			List<Task> tasks = List.of(new Task(consumer, stream));
			Exception exception = assertThrows(UncheckedIOException.class, () -> {
				p.send(request, tasks);
			});
			assertSame(cause, exception.getCause());
		}
	}

	@Test
	void proxyDoesNotSendIfListenerThrowsExecutionException() {
		Throwable cause = new Throwable();
		Exception exception = new ExecutionException(cause);
		try (MockedConstruction<InputStreamResponseListener> construction = mockListenerConstruction(exception)) {
			p = newProxy();
			List<Task> tasks = List.of();
			exception = assertThrows(ServerException.class, () -> {
				p.send(request, tasks);
			});
			assertSame(cause, exception.getCause());
		}
	}

	@Test
	void proxyDoesNotSendIfListenerThrowsTimeoutException() {
		TimeoutException cause = new TimeoutException();
		try (MockedConstruction<InputStreamResponseListener> construction = mockListenerConstruction(cause)) {
			p = newProxy();
			List<Task> tasks = List.of();
			Exception exception = assertThrows(ServerException.class, () -> {
				p.send(request, tasks);
			});
			assertSame(cause, exception.getCause());
		}
	}

	@Test
	void proxyDoesNotSendIfListenerThrowsInterruptedException() {
		InterruptedException cause = new InterruptedException();
		try (MockedConstruction<InputStreamResponseListener> construction = mockListenerConstruction(cause)) {
			p = newProxy();
			List<Task> tasks = List.of();
			Error error = assertThrows(AssertionError.class, () -> {
				p.send(request, tasks);
			});
			assertSame(cause, error.getCause());
		}
	}

	private MockedConstruction<InputStreamResponseListener> mockListenerConstruction(Exception exception) {
		return mockConstruction(InputStreamResponseListener.class, (mock, context) -> {
			when(mock.get(0, TimeUnit.SECONDS)).thenThrow(exception);
		});
	}

	private Proxy spyNewProxy() {
		return spy(newProxy());
	}

	private Proxy newProxy() {
		return newRestClient().new Proxy();
	}

	private RestClient newRestClient() {
		return newRestClient(null);
	}

	private RestClient newRestClient(String none) {
		return new RestClient(facade, jettyClient, StandardCharsets.UTF_8, Coding.LOCALE, none, "http://a");
	}
}
