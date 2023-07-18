package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.MultiPartRequestContent;
import org.eclipse.jetty.client.util.OutputStreamRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedConstruction.MockInitializer;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.client.RestClient.Proxy;
import br.pro.hashi.sdx.rest.client.RestClient.Proxy.Entry;
import br.pro.hashi.sdx.rest.client.RestClient.Proxy.Task;
import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.coding.PathCoder;
import br.pro.hashi.sdx.rest.coding.QueryCoder;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

class RestClientTest {
	private static final String URL_PREFIX = "http://a";
	private static final String URI = "/b";
	private static final String CONTENT_TYPE = "type/subtype";
	private static final String REGULAR_CONTENT = "regular";
	private static final String SPECIAL_CONTENT = "spéçìal";

	private AutoCloseable mocks;
	private @Mock QueryCoder queryCoder;
	private @Mock PathCoder pathCoder;
	private @Mock MediaCoder mediaCoder;
	private @Mock TransformManager manager;
	private @Mock HttpClient jettyClient;
	private @Mock Request request;
	private @Mock Response response;
	private RestClient c;
	private Proxy p;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		when(queryCoder.recode(any(String.class), eq(StandardCharsets.UTF_8))).thenAnswer((invocation) -> {
			String item = invocation.getArgument(0);
			if (item.isBlank()) {
				return "";
			}
			return "qr(%s)".formatted(item);
		});
		when(queryCoder.encode(any(String.class), eq(StandardCharsets.UTF_8))).thenAnswer((invocation) -> {
			String item = invocation.getArgument(0);
			return "qe(%s)".formatted(item);
		});

		when(pathCoder.stripEndingSlashes(any(String.class))).thenAnswer((invocation) -> {
			return invocation.getArgument(0);
		});
		when(pathCoder.recode(any(String.class), eq(StandardCharsets.UTF_8))).thenAnswer((invocation) -> {
			String path = invocation.getArgument(0);
			return "pr(%s)".formatted(path);
		});

		when(mediaCoder.encode(any(OutputStream.class))).thenAnswer((invocation) -> {
			return invocation.getArgument(0);
		});

		c = new RestClient(queryCoder, pathCoder, mediaCoder, manager, jettyClient, Locale.ROOT, StandardCharsets.UTF_8, URL_PREFIX);
	}

	@AfterEach
	void tearDown() {
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsInstance() {
		RestClient client;
		try (MockedStatic<PathCoder> pathCoderStatic = mockStatic(PathCoder.class)) {
			pathCoderStatic.when(() -> PathCoder.getInstance()).thenReturn(pathCoder);
			try (MockedStatic<MediaCoder> mediaCoderStatic = mockStatic(MediaCoder.class)) {
				mediaCoderStatic.when(() -> MediaCoder.getInstance()).thenReturn(mediaCoder);
				client = RestClient.newInstance(manager, jettyClient, Locale.ROOT, StandardCharsets.UTF_8, URL_PREFIX);
			}
		}
		assertSame(manager, client.getManager());
		assertEquals(Locale.ROOT, client.getLocale());
		assertEquals(StandardCharsets.UTF_8, client.getUrlCharset());
		assertEquals(URL_PREFIX, client.getUrlPrefix());
		assertSame(jettyClient, client.getJettyClient());
	}

	@Test
	void gets() {
		MockInitializer<RestClientBuilder> initializer = (mock, context) -> {
			when(mock.build(URL_PREFIX)).thenReturn(c);
		};
		try (MockedConstruction<RestClientBuilder> construction = mockConstruction(RestClientBuilder.class, initializer)) {
			assertSame(c, RestClient.to(URL_PREFIX));
		}
	}

	@Test
	void starts() {
		when(jettyClient.isRunning()).thenReturn(false);
		c.start();
		assertDoesNotThrow(() -> {
			verify(jettyClient).start();
		});
	}

	@Test
	void doesNotStartIfJettyClientStarted() {
		when(jettyClient.isRunning()).thenReturn(true);
		c.start();
		assertDoesNotThrow(() -> {
			verify(jettyClient, times(0)).start();
		});
	}

	@Test
	void doesNotStartIfJettyClientThrows() {
		when(jettyClient.isRunning()).thenReturn(false);
		Throwable cause = new Exception();
		assertDoesNotThrow(() -> {
			doThrow(cause).when(jettyClient).start();
		});
		Exception exception = assertThrows(ClientException.class, () -> {
			c.start();
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void stops() {
		when(jettyClient.isRunning()).thenReturn(true);
		c.stop();
		assertDoesNotThrow(() -> {
			verify(jettyClient).stop();
		});
	}

	@Test
	void doesNotStopIfJettyClientStopped() {
		when(jettyClient.isRunning()).thenReturn(false);
		c.stop();
		assertDoesNotThrow(() -> {
			verify(jettyClient, times(0)).stop();
		});
	}

	@Test
	void doesNotStopIfJettyClientThrows() {
		when(jettyClient.isRunning()).thenReturn(true);
		Throwable cause = new Exception();
		assertDoesNotThrow(() -> {
			doThrow(cause).when(jettyClient).stop();
		});
		Exception exception = assertThrows(ClientException.class, () -> {
			c.stop();
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void forwardsWithQueryWithoutValueToProxy() {
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.withQuery("parameter")).thenReturn(mock);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			p = c.q("parameter");
			assertSame(p, construction.constructed().get(0));
		}
		verify(p).withQuery("parameter");
	}

	@Test
	void forwardsWithQueryToProxy() {
		Object value = new Object();
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.withQuery("name", value)).thenReturn(mock);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			p = c.q("name", value);
			assertSame(p, construction.constructed().get(0));
		}
		verify(p).withQuery("name", value);
	}

	@Test
	void forwardsWithHeaderToProxy() {
		Object value = new Object();
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.withHeader("name", value)).thenReturn(mock);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			p = c.h("name", value);
			assertSame(p, construction.constructed().get(0));
		}
		verify(p).withHeader("name", value);
	}

	@Test
	void forwardsWithBodyToProxy() {
		Object body = new Object();
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.withBody(body)).thenReturn(mock);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			p = c.b(body);
			assertSame(p, construction.constructed().get(0));
		}
		verify(p).withBody(body);
	}

	@Test
	void forwardsWithPartToProxy() {
		Object part = new Object();
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.withPart("name", part)).thenReturn(mock);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			p = c.p("name", part);
			assertSame(p, construction.constructed().get(0));
		}
		verify(p).withPart("name", part);
	}

	@Test
	void forwardsWithTimeoutToProxy() {
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.withTimeout(0)).thenReturn(mock);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			p = c.t(0);
			assertSame(p, construction.constructed().get(0));
		}
		verify(p).withTimeout(0);
	}

	@Test
	void forwardsGetToProxy() {
		RestResponse restResponse = mock(RestResponse.class);
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.get(URI)).thenReturn(restResponse);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			assertSame(restResponse, c.get(URI));
		}
	}

	@Test
	void forwardsPostWithoutBodyToProxy() {
		RestResponse restResponse = mock(RestResponse.class);
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.post(URI)).thenReturn(restResponse);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			assertSame(restResponse, c.post(URI));
		}
	}

	@Test
	void forwardsPostToProxy() {
		Object body = new Object();
		RestResponse restResponse = mock(RestResponse.class);
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.post(URI, body)).thenReturn(restResponse);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			assertSame(restResponse, c.post(URI, body));
		}
	}

	@Test
	void forwardsPutWithoutBodyToProxy() {
		RestResponse restResponse = mock(RestResponse.class);
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.put(URI)).thenReturn(restResponse);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			assertSame(restResponse, c.put(URI));
		}
	}

	@Test
	void forwardsPutToProxy() {
		Object body = new Object();
		RestResponse restResponse = mock(RestResponse.class);
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.put(URI, body)).thenReturn(restResponse);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			assertSame(restResponse, c.put(URI, body));
		}
	}

	@Test
	void forwardsPatchWithoutBodyToProxy() {
		RestResponse restResponse = mock(RestResponse.class);
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.patch(URI)).thenReturn(restResponse);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			assertSame(restResponse, c.patch(URI));
		}
	}

	@Test
	void forwardsPatchToProxy() {
		Object body = new Object();
		RestResponse restResponse = mock(RestResponse.class);
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.patch(URI, body)).thenReturn(restResponse);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			assertSame(restResponse, c.patch(URI, body));
		}
	}

	@Test
	void forwardsDeleteToProxy() {
		RestResponse restResponse = mock(RestResponse.class);
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.delete(URI)).thenReturn(restResponse);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			assertSame(restResponse, c.delete(URI));
		}
	}

	@Test
	void forwardsRequestToProxy() {
		RestResponse restResponse = mock(RestResponse.class);
		MockInitializer<Proxy> initializer = (mock, context) -> {
			when(mock.request(" \t\noptions \t\n", URI)).thenReturn(restResponse);
		};
		try (MockedConstruction<Proxy> construction = mockConstruction(Proxy.class, initializer)) {
			assertSame(restResponse, c.request(" \t\noptions \t\n", URI));
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
	void proxyInitializesWithoutParts() {
		p = newProxy();
		assertTrue(p.getParts().isEmpty());
	}

	@Test
	void proxyInitializesWithoutBody() {
		p = newProxy();
		assertNull(p.getBody());
	}

	@Test
	void proxyInitializesWithDefaultTimeout() {
		p = newProxy();
		assertEquals(RestClient.TIMEOUT, p.getTimeout());
	}

	@Test
	void proxyAddsQueryWithoutValue() {
		p = newProxy();
		assertSame(p, p.q("parameter"));
		assertEquals(1, p.getQueries().size());
		Entry entry = p.getQueries().get(0);
		assertEquals("qe(parameter)", entry.name());
		assertNull(entry.valueString());
	}

	@Test
	void proxyDoesNotAddQueryWithoutValueWithNullName() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.q(null);
		});
	}

	@Test
	void proxyAddsQuery() {
		p = newProxy();
		assertSame(p, p.q("name", 0));
		assertEquals(1, p.getQueries().size());
		Entry entry = p.getQueries().get(0);
		assertEquals("qe(name)", entry.name());
		assertEquals("qe(0)", entry.valueString());
	}

	@Test
	void proxyDoesNotAddQueryWithNullName() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.q(null, 0);
		});
	}

	@Test
	void proxyDoesNotAddQueryWithNullValue() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.q("name", null);
		});
	}

	@Test
	void proxyDoesNotAddQueryWithNullValueString() {
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
	}

	@Test
	void proxyAddsHeader() {
		p = newProxy();
		assertSame(p, p.h(" \t\nname \t\n", 0));
		assertEquals(1, p.getHeaders().size());
		Entry entry = p.getHeaders().get(0);
		assertEquals("name", entry.name());
		assertEquals("0", entry.valueString());
	}

	@Test
	void proxyDoesNotAddHeaderWithNullName() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.h(null, 0);
		});
	}

	@Test
	void proxyDoesNotAddHeaderWithBlankName() {
		p = newProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h(" \t\n", 0);
		});
	}

	@Test
	void proxyDoesNotAddHeaderWithSpecialName() {
		p = newProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h(SPECIAL_CONTENT, 0);
		});
	}

	@Test
	void proxyDoesNotAddHeaderWithNullValue() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.h("name", null);
		});
	}

	@Test
	void proxyDoesNotAddHeaderWithNullStringValue() {
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
	}

	@Test
	void proxyDoesNotAddHeaderWithSpecialValue() {
		p = newProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h("name", SPECIAL_CONTENT);
		});
	}

	@Test
	void proxySetsBody() {
		p = newProxy();
		Object body = new Object();
		RestBody restBody = mock(RestBody.class);
		try (MockedStatic<RestBody> bodyStatic = mockStatic(RestBody.class)) {
			bodyStatic.when(() -> RestBody.of(body)).thenReturn(restBody);
			assertSame(p, p.b(body));
		}
		assertSame(restBody, p.getBody());
	}

	@Test
	void proxyDoesNotSetBodyWithPart() {
		p = newProxy();
		p.p("name", mock(RestPart.class));
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			p.b(body);
		});
	}

	@Test
	void proxySetsRestBody() {
		p = newProxy();
		RestBody body = mock(RestBody.class);
		assertSame(p, p.b(body));
		assertSame(body, p.getBody());
	}

	@Test
	void proxyDoesNotSetRestPart() {
		p = newProxy();
		RestPart body = mock(RestPart.class);
		assertThrows(IllegalArgumentException.class, () -> {
			p.b(body);
		});
	}

	@Test
	void proxyAddsPart() {
		p = newProxy();
		Object part = new Object();
		RestPart restPart = mock(RestPart.class);
		try (MockedStatic<RestPart> partStatic = mockStatic(RestPart.class)) {
			partStatic.when(() -> RestPart.of(part)).thenReturn(restPart);
			assertSame(p, p.p("name", part));
		}
		List<RestPart> parts = p.getParts();
		assertEquals(1, parts.size());
		assertSame(restPart, parts.get(0));
		verify(restPart).setName("name");
	}

	@Test
	void proxyDoesNotAddPartWithBody() {
		p = newProxy();
		p.b(mock(RestBody.class));
		Object part = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			p.p("name", part);
		});
	}

	@Test
	void proxyDoesNotAddPartWithNullName() {
		p = newProxy();
		Object part = new Object();
		assertThrows(NullPointerException.class, () -> {
			p.p(null, part);
		});
	}

	@Test
	void proxyAddsRestPart() {
		p = newProxy();
		RestPart part = mock(RestPart.class);
		assertSame(p, p.p("name", part));
		List<RestPart> parts = p.getParts();
		assertEquals(1, parts.size());
		assertSame(part, parts.get(0));
		verify(part).setName("name");
	}

	@Test
	void proxyDoesNotAddRestBody() {
		p = newProxy();
		RestBody part = mock(RestBody.class);
		assertThrows(IllegalArgumentException.class, () -> {
			p.p("name", part);
		});
	}

	@Test
	void proxySetsTimeout() {
		p = newProxy();
		assertSame(p, p.t(0));
		assertEquals(0, p.getTimeout());
	}

	@Test
	void proxyGets() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("GET", URI);
		assertSame(restResponse, p.get(URI));
	}

	@Test
	void proxyPostsWithoutBody() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("POST", URI);
		assertSame(restResponse, p.post(URI));
	}

	@Test
	void proxyPosts() {
		p = spyNewProxy();
		Object body = new Object();
		doReturn(p).when(p).withBody(body);
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).post(URI);
		assertSame(restResponse, p.post(URI, body));
		verify(p).withBody(body);
	}

	@Test
	void proxyPuts() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("PUT", URI);
		assertSame(restResponse, p.put(URI));
	}

	@Test
	void proxyPutsWithBody() {
		p = spyNewProxy();
		Object body = new Object();
		doReturn(p).when(p).withBody(body);
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).put(URI);
		assertSame(restResponse, p.put(URI, body));
		verify(p).withBody(body);
	}

	@Test
	void proxyPatches() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("PATCH", URI);
		assertSame(restResponse, p.patch(URI));
	}

	@Test
	void proxyPatchesWithBody() {
		p = spyNewProxy();
		Object body = new Object();
		doReturn(p).when(p).withBody(body);
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).patch(URI);
		assertSame(restResponse, p.patch(URI, body));
		verify(p).withBody(body);
	}

	@Test
	void proxyDeletes() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("DELETE", URI);
		assertSame(restResponse, p.delete(URI));
	}

	@Test
	void proxyRequests() {
		p = spyNewProxy();
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).doRequest("OPTIONS", URI);
		assertSame(restResponse, p.request(" \t\noptions \t\n", URI));
	}

	@Test
	void proxyDoesNotRequestIfMethodIsNull() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.request(null, URI);
		});
	}

	@Test
	void proxyDoesNotRequestIfMethodIsBlank() {
		p = newProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.request(" \t\n", URI);
		});
	}

	@Test
	void proxyDoesRequest() {
		p = spyNewProxy();
		doReturn("/b?x").when(p).completeQueries(URI);
		when(jettyClient.newRequest("http://a/b?x")).thenReturn(request);
		when(request.method("OPTIONS")).thenReturn(request);
		doNothing().when(p).addHeaders(request);
		List<Task> tasks = List.of();
		doReturn(tasks).when(p).consumeBodyAndBuildTasks(request);
		RestResponse restResponse = mock(RestResponse.class);
		doReturn(restResponse).when(p).send(request, tasks);
		assertSame(restResponse, p.doRequest("OPTIONS", " \t\n/b \t\n"));
	}

	@Test
	void proxyDoesNotDoRequestIfUriIsNull() {
		p = newProxy();
		assertThrows(NullPointerException.class, () -> {
			p.doRequest("OPTIONS", null);
		});
	}

	@Test
	void proxyDoesNotDoRequestIfUriIsBlank() {
		p = newProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.doRequest("OPTIONS", " \t\n");
		});
	}

	@Test
	void proxyDoesNotDoRequestIfUriDoesNotStartWithSlash() {
		p = newProxy();
		assertThrows(IllegalArgumentException.class, () -> {
			p.doRequest("OPTIONS", "b");
		});
	}

	@ParameterizedTest
	@CsvSource({
			"pr(/b),                   /b",
			"pr(/b),                   /b?",
			"pr(/b)?qr(?),             /b??",
			"pr(/b)?&,                 /b?&",
			"pr(/b)?=,                 /b?=",
			"pr(/b)?qr(x),             /b?x",
			"pr(/b)?qr(x?),            /b?x?",
			"pr(/b)?qr(x)&,            /b?x&",
			"pr(/b)?qr(x)&qr(?),       /b?x&?",
			"pr(/b)?qr(x)&&,           /b?x&&",
			"pr(/b)?qr(x)&=,           /b?x&=",
			"pr(/b)?qr(x)&qr(y),       /b?x&y",
			"pr(/b)?qr(x)&qr(y?),      /b?x&y?",
			"pr(/b)?qr(x)&qr(y)&,      /b?x&y&",
			"pr(/b)?qr(x)&qr(y)&qr(?), /b?x&y&?",
			"pr(/b)?qr(x)&qr(y)&&,     /b?x&y&&",
			"pr(/b)?qr(x)&qr(y)&=,     /b?x&y&=",
			"pr(/b)?qr(x)&qr(y)&qr(z), /b?x&y&z",
			"pr(/b)?qr(x)&qr(y)=,      /b?x&y=",
			"pr(/b)?qr(x)&qr(y)=qr(?), /b?x&y=?",
			"pr(/b)?qr(x)&qr(y)=&,     /b?x&y=&",
			"pr(/b)?qr(x)&qr(y)=qr(=), /b?x&y==",
			"pr(/b)?qr(x)&qr(y)=qr(z), /b?x&y=z",
			"pr(/b)?qr(x)=,            /b?x=",
			"pr(/b)?qr(x)=qr(?),       /b?x=?",
			"pr(/b)?qr(x)=&,           /b?x=&",
			"pr(/b)?qr(x)=qr(=),       /b?x==",
			"pr(/b)?qr(x)=qr(y),       /b?x=y",
			"pr(/b)?qr(x)=qr(y?),      /b?x=y?",
			"pr(/b)?qr(x)=qr(y)&,      /b?x=y&",
			"pr(/b)?qr(x)=qr(y)&qr(?), /b?x=y&?",
			"pr(/b)?qr(x)=qr(y)&&,     /b?x=y&&",
			"pr(/b)?qr(x)=qr(y)&=,     /b?x=y&=",
			"pr(/b)?qr(x)=qr(y)&qr(z), /b?x=y&z",
			"pr(/b)?qr(x)=qr(y=),      /b?x=y=",
			"pr(/b)?qr(x)=qr(y=?),     /b?x=y=?",
			"pr(/b)?qr(x)=qr(y=)&,     /b?x=y=&",
			"pr(/b)?qr(x)=qr(y==),     /b?x=y==",
			"pr(/b)?qr(x)=qr(y=z),     /b?x=y=z",
			"pr(/b&),                  /b&",
			"pr(/b=),                  /b=" })
	void proxyCompletesQueries(String expected, String uri) {
		p = newProxy();
		assertEquals(expected, p.completeQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"pr(/b)?qe(parameter),                   /b",
			"pr(/b)?qe(parameter),                   /b?",
			"pr(/b)?qr(?)&qe(parameter),             /b??",
			"pr(/b)?&&qe(parameter),                 /b?&",
			"pr(/b)?=&qe(parameter),                 /b?=",
			"pr(/b)?qr(x)&qe(parameter),             /b?x",
			"pr(/b)?qr(x?)&qe(parameter),            /b?x?",
			"pr(/b)?qr(x)&&qe(parameter),            /b?x&",
			"pr(/b)?qr(x)&qr(?)&qe(parameter),       /b?x&?",
			"pr(/b)?qr(x)&&&qe(parameter),           /b?x&&",
			"pr(/b)?qr(x)&=&qe(parameter),           /b?x&=",
			"pr(/b)?qr(x)&qr(y)&qe(parameter),       /b?x&y",
			"pr(/b)?qr(x)&qr(y?)&qe(parameter),      /b?x&y?",
			"pr(/b)?qr(x)&qr(y)&&qe(parameter),      /b?x&y&",
			"pr(/b)?qr(x)&qr(y)&qr(?)&qe(parameter), /b?x&y&?",
			"pr(/b)?qr(x)&qr(y)&&&qe(parameter),     /b?x&y&&",
			"pr(/b)?qr(x)&qr(y)&=&qe(parameter),     /b?x&y&=",
			"pr(/b)?qr(x)&qr(y)&qr(z)&qe(parameter), /b?x&y&z",
			"pr(/b)?qr(x)&qr(y)=&qe(parameter),      /b?x&y=",
			"pr(/b)?qr(x)&qr(y)=qr(?)&qe(parameter), /b?x&y=?",
			"pr(/b)?qr(x)&qr(y)=&&qe(parameter),     /b?x&y=&",
			"pr(/b)?qr(x)&qr(y)=qr(=)&qe(parameter), /b?x&y==",
			"pr(/b)?qr(x)&qr(y)=qr(z)&qe(parameter), /b?x&y=z",
			"pr(/b)?qr(x)=&qe(parameter),            /b?x=",
			"pr(/b)?qr(x)=qr(?)&qe(parameter),       /b?x=?",
			"pr(/b)?qr(x)=&&qe(parameter),           /b?x=&",
			"pr(/b)?qr(x)=qr(=)&qe(parameter),       /b?x==",
			"pr(/b)?qr(x)=qr(y)&qe(parameter),       /b?x=y",
			"pr(/b)?qr(x)=qr(y?)&qe(parameter),      /b?x=y?",
			"pr(/b)?qr(x)=qr(y)&&qe(parameter),      /b?x=y&",
			"pr(/b)?qr(x)=qr(y)&qr(?)&qe(parameter), /b?x=y&?",
			"pr(/b)?qr(x)=qr(y)&&&qe(parameter),     /b?x=y&&",
			"pr(/b)?qr(x)=qr(y)&=&qe(parameter),     /b?x=y&=",
			"pr(/b)?qr(x)=qr(y)&qr(z)&qe(parameter), /b?x=y&z",
			"pr(/b)?qr(x)=qr(y=)&qe(parameter),      /b?x=y=",
			"pr(/b)?qr(x)=qr(y=?)&qe(parameter),     /b?x=y=?",
			"pr(/b)?qr(x)=qr(y=)&&qe(parameter),     /b?x=y=&",
			"pr(/b)?qr(x)=qr(y==)&qe(parameter),     /b?x=y==",
			"pr(/b)?qr(x)=qr(y=z)&qe(parameter),     /b?x=y=z",
			"pr(/b&)?qe(parameter),                  /b&",
			"pr(/b=)?qe(parameter),                  /b=" })
	void proxyAddsOneQueryWithoutValueAndCompletesQueries(String expected, String uri) {
		p = newProxy();
		p.withQuery("parameter");
		assertEquals(expected, p.completeQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"pr(/b)?qe(parameter)&qe(name),                   /b",
			"pr(/b)?qe(parameter)&qe(name),                   /b?",
			"pr(/b)?qr(?)&qe(parameter)&qe(name),             /b??",
			"pr(/b)?&&qe(parameter)&qe(name),                 /b?&",
			"pr(/b)?=&qe(parameter)&qe(name),                 /b?=",
			"pr(/b)?qr(x)&qe(parameter)&qe(name),             /b?x",
			"pr(/b)?qr(x?)&qe(parameter)&qe(name),            /b?x?",
			"pr(/b)?qr(x)&&qe(parameter)&qe(name),            /b?x&",
			"pr(/b)?qr(x)&qr(?)&qe(parameter)&qe(name),       /b?x&?",
			"pr(/b)?qr(x)&&&qe(parameter)&qe(name),           /b?x&&",
			"pr(/b)?qr(x)&=&qe(parameter)&qe(name),           /b?x&=",
			"pr(/b)?qr(x)&qr(y)&qe(parameter)&qe(name),       /b?x&y",
			"pr(/b)?qr(x)&qr(y?)&qe(parameter)&qe(name),      /b?x&y?",
			"pr(/b)?qr(x)&qr(y)&&qe(parameter)&qe(name),      /b?x&y&",
			"pr(/b)?qr(x)&qr(y)&qr(?)&qe(parameter)&qe(name), /b?x&y&?",
			"pr(/b)?qr(x)&qr(y)&&&qe(parameter)&qe(name),     /b?x&y&&",
			"pr(/b)?qr(x)&qr(y)&=&qe(parameter)&qe(name),     /b?x&y&=",
			"pr(/b)?qr(x)&qr(y)&qr(z)&qe(parameter)&qe(name), /b?x&y&z",
			"pr(/b)?qr(x)&qr(y)=&qe(parameter)&qe(name),      /b?x&y=",
			"pr(/b)?qr(x)&qr(y)=qr(?)&qe(parameter)&qe(name), /b?x&y=?",
			"pr(/b)?qr(x)&qr(y)=&&qe(parameter)&qe(name),     /b?x&y=&",
			"pr(/b)?qr(x)&qr(y)=qr(=)&qe(parameter)&qe(name), /b?x&y==",
			"pr(/b)?qr(x)&qr(y)=qr(z)&qe(parameter)&qe(name), /b?x&y=z",
			"pr(/b)?qr(x)=&qe(parameter)&qe(name),            /b?x=",
			"pr(/b)?qr(x)=qr(?)&qe(parameter)&qe(name),       /b?x=?",
			"pr(/b)?qr(x)=&&qe(parameter)&qe(name),           /b?x=&",
			"pr(/b)?qr(x)=qr(=)&qe(parameter)&qe(name),       /b?x==",
			"pr(/b)?qr(x)=qr(y)&qe(parameter)&qe(name),       /b?x=y",
			"pr(/b)?qr(x)=qr(y?)&qe(parameter)&qe(name),      /b?x=y?",
			"pr(/b)?qr(x)=qr(y)&&qe(parameter)&qe(name),      /b?x=y&",
			"pr(/b)?qr(x)=qr(y)&qr(?)&qe(parameter)&qe(name), /b?x=y&?",
			"pr(/b)?qr(x)=qr(y)&&&qe(parameter)&qe(name),     /b?x=y&&",
			"pr(/b)?qr(x)=qr(y)&=&qe(parameter)&qe(name),     /b?x=y&=",
			"pr(/b)?qr(x)=qr(y)&qr(z)&qe(parameter)&qe(name), /b?x=y&z",
			"pr(/b)?qr(x)=qr(y=)&qe(parameter)&qe(name),      /b?x=y=",
			"pr(/b)?qr(x)=qr(y=?)&qe(parameter)&qe(name),     /b?x=y=?",
			"pr(/b)?qr(x)=qr(y=)&&qe(parameter)&qe(name),     /b?x=y=&",
			"pr(/b)?qr(x)=qr(y==)&qe(parameter)&qe(name),     /b?x=y==",
			"pr(/b)?qr(x)=qr(y=z)&qe(parameter)&qe(name),     /b?x=y=z",
			"pr(/b&)?qe(parameter)&qe(name),                  /b&",
			"pr(/b=)?qe(parameter)&qe(name),                  /b=" })
	void proxyAddsTwoQueriesWithoutValueAndCompletesQueries(String expected, String uri) {
		p = newProxy();
		p.withQuery("parameter");
		p.withQuery("name");
		assertEquals(expected, p.completeQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"pr(/b)?qe(parameter)=qe(name),                   /b",
			"pr(/b)?qe(parameter)=qe(name),                   /b?",
			"pr(/b)?qr(?)&qe(parameter)=qe(name),             /b??",
			"pr(/b)?&&qe(parameter)=qe(name),                 /b?&",
			"pr(/b)?=&qe(parameter)=qe(name),                 /b?=",
			"pr(/b)?qr(x)&qe(parameter)=qe(name),             /b?x",
			"pr(/b)?qr(x?)&qe(parameter)=qe(name),            /b?x?",
			"pr(/b)?qr(x)&&qe(parameter)=qe(name),            /b?x&",
			"pr(/b)?qr(x)&qr(?)&qe(parameter)=qe(name),       /b?x&?",
			"pr(/b)?qr(x)&&&qe(parameter)=qe(name),           /b?x&&",
			"pr(/b)?qr(x)&=&qe(parameter)=qe(name),           /b?x&=",
			"pr(/b)?qr(x)&qr(y)&qe(parameter)=qe(name),       /b?x&y",
			"pr(/b)?qr(x)&qr(y?)&qe(parameter)=qe(name),      /b?x&y?",
			"pr(/b)?qr(x)&qr(y)&&qe(parameter)=qe(name),      /b?x&y&",
			"pr(/b)?qr(x)&qr(y)&qr(?)&qe(parameter)=qe(name), /b?x&y&?",
			"pr(/b)?qr(x)&qr(y)&&&qe(parameter)=qe(name),     /b?x&y&&",
			"pr(/b)?qr(x)&qr(y)&=&qe(parameter)=qe(name),     /b?x&y&=",
			"pr(/b)?qr(x)&qr(y)&qr(z)&qe(parameter)=qe(name), /b?x&y&z",
			"pr(/b)?qr(x)&qr(y)=&qe(parameter)=qe(name),      /b?x&y=",
			"pr(/b)?qr(x)&qr(y)=qr(?)&qe(parameter)=qe(name), /b?x&y=?",
			"pr(/b)?qr(x)&qr(y)=&&qe(parameter)=qe(name),     /b?x&y=&",
			"pr(/b)?qr(x)&qr(y)=qr(=)&qe(parameter)=qe(name), /b?x&y==",
			"pr(/b)?qr(x)&qr(y)=qr(z)&qe(parameter)=qe(name), /b?x&y=z",
			"pr(/b)?qr(x)=&qe(parameter)=qe(name),            /b?x=",
			"pr(/b)?qr(x)=qr(?)&qe(parameter)=qe(name),       /b?x=?",
			"pr(/b)?qr(x)=&&qe(parameter)=qe(name),           /b?x=&",
			"pr(/b)?qr(x)=qr(=)&qe(parameter)=qe(name),       /b?x==",
			"pr(/b)?qr(x)=qr(y)&qe(parameter)=qe(name),       /b?x=y",
			"pr(/b)?qr(x)=qr(y?)&qe(parameter)=qe(name),      /b?x=y?",
			"pr(/b)?qr(x)=qr(y)&&qe(parameter)=qe(name),      /b?x=y&",
			"pr(/b)?qr(x)=qr(y)&qr(?)&qe(parameter)=qe(name), /b?x=y&?",
			"pr(/b)?qr(x)=qr(y)&&&qe(parameter)=qe(name),     /b?x=y&&",
			"pr(/b)?qr(x)=qr(y)&=&qe(parameter)=qe(name),     /b?x=y&=",
			"pr(/b)?qr(x)=qr(y)&qr(z)&qe(parameter)=qe(name), /b?x=y&z",
			"pr(/b)?qr(x)=qr(y=)&qe(parameter)=qe(name),      /b?x=y=",
			"pr(/b)?qr(x)=qr(y=?)&qe(parameter)=qe(name),     /b?x=y=?",
			"pr(/b)?qr(x)=qr(y=)&&qe(parameter)=qe(name),     /b?x=y=&",
			"pr(/b)?qr(x)=qr(y==)&qe(parameter)=qe(name),     /b?x=y==",
			"pr(/b)?qr(x)=qr(y=z)&qe(parameter)=qe(name),     /b?x=y=z",
			"pr(/b&)?qe(parameter)=qe(name),                  /b&",
			"pr(/b=)?qe(parameter)=qe(name),                  /b=" })
	void proxyAddsOneQueryAndCompletesQueries(String expected, String uri) {
		p = newProxy();
		p.withQuery("parameter", "name");
		assertEquals(expected, p.completeQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"pr(/b)?qe(parameter)&qe(name)&qe(0),                   /b",
			"pr(/b)?qe(parameter)&qe(name)&qe(0),                   /b?",
			"pr(/b)?qr(?)&qe(parameter)&qe(name)&qe(0),             /b??",
			"pr(/b)?&&qe(parameter)&qe(name)&qe(0),                 /b?&",
			"pr(/b)?=&qe(parameter)&qe(name)&qe(0),                 /b?=",
			"pr(/b)?qr(x)&qe(parameter)&qe(name)&qe(0),             /b?x",
			"pr(/b)?qr(x?)&qe(parameter)&qe(name)&qe(0),            /b?x?",
			"pr(/b)?qr(x)&&qe(parameter)&qe(name)&qe(0),            /b?x&",
			"pr(/b)?qr(x)&qr(?)&qe(parameter)&qe(name)&qe(0),       /b?x&?",
			"pr(/b)?qr(x)&&&qe(parameter)&qe(name)&qe(0),           /b?x&&",
			"pr(/b)?qr(x)&=&qe(parameter)&qe(name)&qe(0),           /b?x&=",
			"pr(/b)?qr(x)&qr(y)&qe(parameter)&qe(name)&qe(0),       /b?x&y",
			"pr(/b)?qr(x)&qr(y?)&qe(parameter)&qe(name)&qe(0),      /b?x&y?",
			"pr(/b)?qr(x)&qr(y)&&qe(parameter)&qe(name)&qe(0),      /b?x&y&",
			"pr(/b)?qr(x)&qr(y)&qr(?)&qe(parameter)&qe(name)&qe(0), /b?x&y&?",
			"pr(/b)?qr(x)&qr(y)&&&qe(parameter)&qe(name)&qe(0),     /b?x&y&&",
			"pr(/b)?qr(x)&qr(y)&=&qe(parameter)&qe(name)&qe(0),     /b?x&y&=",
			"pr(/b)?qr(x)&qr(y)&qr(z)&qe(parameter)&qe(name)&qe(0), /b?x&y&z",
			"pr(/b)?qr(x)&qr(y)=&qe(parameter)&qe(name)&qe(0),      /b?x&y=",
			"pr(/b)?qr(x)&qr(y)=qr(?)&qe(parameter)&qe(name)&qe(0), /b?x&y=?",
			"pr(/b)?qr(x)&qr(y)=&&qe(parameter)&qe(name)&qe(0),     /b?x&y=&",
			"pr(/b)?qr(x)&qr(y)=qr(=)&qe(parameter)&qe(name)&qe(0), /b?x&y==",
			"pr(/b)?qr(x)&qr(y)=qr(z)&qe(parameter)&qe(name)&qe(0), /b?x&y=z",
			"pr(/b)?qr(x)=&qe(parameter)&qe(name)&qe(0),            /b?x=",
			"pr(/b)?qr(x)=qr(?)&qe(parameter)&qe(name)&qe(0),       /b?x=?",
			"pr(/b)?qr(x)=&&qe(parameter)&qe(name)&qe(0),           /b?x=&",
			"pr(/b)?qr(x)=qr(=)&qe(parameter)&qe(name)&qe(0),       /b?x==",
			"pr(/b)?qr(x)=qr(y)&qe(parameter)&qe(name)&qe(0),       /b?x=y",
			"pr(/b)?qr(x)=qr(y?)&qe(parameter)&qe(name)&qe(0),      /b?x=y?",
			"pr(/b)?qr(x)=qr(y)&&qe(parameter)&qe(name)&qe(0),      /b?x=y&",
			"pr(/b)?qr(x)=qr(y)&qr(?)&qe(parameter)&qe(name)&qe(0), /b?x=y&?",
			"pr(/b)?qr(x)=qr(y)&&&qe(parameter)&qe(name)&qe(0),     /b?x=y&&",
			"pr(/b)?qr(x)=qr(y)&=&qe(parameter)&qe(name)&qe(0),     /b?x=y&=",
			"pr(/b)?qr(x)=qr(y)&qr(z)&qe(parameter)&qe(name)&qe(0), /b?x=y&z",
			"pr(/b)?qr(x)=qr(y=)&qe(parameter)&qe(name)&qe(0),      /b?x=y=",
			"pr(/b)?qr(x)=qr(y=?)&qe(parameter)&qe(name)&qe(0),     /b?x=y=?",
			"pr(/b)?qr(x)=qr(y=)&&qe(parameter)&qe(name)&qe(0),     /b?x=y=&",
			"pr(/b)?qr(x)=qr(y==)&qe(parameter)&qe(name)&qe(0),     /b?x=y==",
			"pr(/b)?qr(x)=qr(y=z)&qe(parameter)&qe(name)&qe(0),     /b?x=y=z",
			"pr(/b&)?qe(parameter)&qe(name)&qe(0),                  /b&",
			"pr(/b=)?qe(parameter)&qe(name)&qe(0),                  /b=" })
	void proxyAddsThreeQueriesWithoutValueAndCompletesQueries(String expected, String uri) {
		p = newProxy();
		p.withQuery("parameter");
		p.withQuery("name");
		p.withQuery("0");
		assertEquals(expected, p.completeQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"pr(/b)?qe(parameter)&qe(name)=qe(0),                   /b",
			"pr(/b)?qe(parameter)&qe(name)=qe(0),                   /b?",
			"pr(/b)?qr(?)&qe(parameter)&qe(name)=qe(0),             /b??",
			"pr(/b)?&&qe(parameter)&qe(name)=qe(0),                 /b?&",
			"pr(/b)?=&qe(parameter)&qe(name)=qe(0),                 /b?=",
			"pr(/b)?qr(x)&qe(parameter)&qe(name)=qe(0),             /b?x",
			"pr(/b)?qr(x?)&qe(parameter)&qe(name)=qe(0),            /b?x?",
			"pr(/b)?qr(x)&&qe(parameter)&qe(name)=qe(0),            /b?x&",
			"pr(/b)?qr(x)&qr(?)&qe(parameter)&qe(name)=qe(0),       /b?x&?",
			"pr(/b)?qr(x)&&&qe(parameter)&qe(name)=qe(0),           /b?x&&",
			"pr(/b)?qr(x)&=&qe(parameter)&qe(name)=qe(0),           /b?x&=",
			"pr(/b)?qr(x)&qr(y)&qe(parameter)&qe(name)=qe(0),       /b?x&y",
			"pr(/b)?qr(x)&qr(y?)&qe(parameter)&qe(name)=qe(0),      /b?x&y?",
			"pr(/b)?qr(x)&qr(y)&&qe(parameter)&qe(name)=qe(0),      /b?x&y&",
			"pr(/b)?qr(x)&qr(y)&qr(?)&qe(parameter)&qe(name)=qe(0), /b?x&y&?",
			"pr(/b)?qr(x)&qr(y)&&&qe(parameter)&qe(name)=qe(0),     /b?x&y&&",
			"pr(/b)?qr(x)&qr(y)&=&qe(parameter)&qe(name)=qe(0),     /b?x&y&=",
			"pr(/b)?qr(x)&qr(y)&qr(z)&qe(parameter)&qe(name)=qe(0), /b?x&y&z",
			"pr(/b)?qr(x)&qr(y)=&qe(parameter)&qe(name)=qe(0),      /b?x&y=",
			"pr(/b)?qr(x)&qr(y)=qr(?)&qe(parameter)&qe(name)=qe(0), /b?x&y=?",
			"pr(/b)?qr(x)&qr(y)=&&qe(parameter)&qe(name)=qe(0),     /b?x&y=&",
			"pr(/b)?qr(x)&qr(y)=qr(=)&qe(parameter)&qe(name)=qe(0), /b?x&y==",
			"pr(/b)?qr(x)&qr(y)=qr(z)&qe(parameter)&qe(name)=qe(0), /b?x&y=z",
			"pr(/b)?qr(x)=&qe(parameter)&qe(name)=qe(0),            /b?x=",
			"pr(/b)?qr(x)=qr(?)&qe(parameter)&qe(name)=qe(0),       /b?x=?",
			"pr(/b)?qr(x)=&&qe(parameter)&qe(name)=qe(0),           /b?x=&",
			"pr(/b)?qr(x)=qr(=)&qe(parameter)&qe(name)=qe(0),       /b?x==",
			"pr(/b)?qr(x)=qr(y)&qe(parameter)&qe(name)=qe(0),       /b?x=y",
			"pr(/b)?qr(x)=qr(y?)&qe(parameter)&qe(name)=qe(0),      /b?x=y?",
			"pr(/b)?qr(x)=qr(y)&&qe(parameter)&qe(name)=qe(0),      /b?x=y&",
			"pr(/b)?qr(x)=qr(y)&qr(?)&qe(parameter)&qe(name)=qe(0), /b?x=y&?",
			"pr(/b)?qr(x)=qr(y)&&&qe(parameter)&qe(name)=qe(0),     /b?x=y&&",
			"pr(/b)?qr(x)=qr(y)&=&qe(parameter)&qe(name)=qe(0),     /b?x=y&=",
			"pr(/b)?qr(x)=qr(y)&qr(z)&qe(parameter)&qe(name)=qe(0), /b?x=y&z",
			"pr(/b)?qr(x)=qr(y=)&qe(parameter)&qe(name)=qe(0),      /b?x=y=",
			"pr(/b)?qr(x)=qr(y=?)&qe(parameter)&qe(name)=qe(0),     /b?x=y=?",
			"pr(/b)?qr(x)=qr(y=)&&qe(parameter)&qe(name)=qe(0),     /b?x=y=&",
			"pr(/b)?qr(x)=qr(y==)&qe(parameter)&qe(name)=qe(0),     /b?x=y==",
			"pr(/b)?qr(x)=qr(y=z)&qe(parameter)&qe(name)=qe(0),     /b?x=y=z",
			"pr(/b&)?qe(parameter)&qe(name)=qe(0),                  /b&",
			"pr(/b=)?qe(parameter)&qe(name)=qe(0),                  /b=" })
	void proxyAddsOneQueryWithoutValueAndOneQueryAndCompletesQueries(String expected, String uri) {
		p = newProxy();
		p.withQuery("parameter");
		p.withQuery("name", 0);
		assertEquals(expected, p.completeQueries(uri));
	}

	@ParameterizedTest
	@CsvSource({
			"pr(/b)?qe(parameter)=qe(name)&qe(0),                   /b",
			"pr(/b)?qe(parameter)=qe(name)&qe(0),                   /b?",
			"pr(/b)?qr(?)&qe(parameter)=qe(name)&qe(0),             /b??",
			"pr(/b)?&&qe(parameter)=qe(name)&qe(0),                 /b?&",
			"pr(/b)?=&qe(parameter)=qe(name)&qe(0),                 /b?=",
			"pr(/b)?qr(x)&qe(parameter)=qe(name)&qe(0),             /b?x",
			"pr(/b)?qr(x?)&qe(parameter)=qe(name)&qe(0),            /b?x?",
			"pr(/b)?qr(x)&&qe(parameter)=qe(name)&qe(0),            /b?x&",
			"pr(/b)?qr(x)&qr(?)&qe(parameter)=qe(name)&qe(0),       /b?x&?",
			"pr(/b)?qr(x)&&&qe(parameter)=qe(name)&qe(0),           /b?x&&",
			"pr(/b)?qr(x)&=&qe(parameter)=qe(name)&qe(0),           /b?x&=",
			"pr(/b)?qr(x)&qr(y)&qe(parameter)=qe(name)&qe(0),       /b?x&y",
			"pr(/b)?qr(x)&qr(y?)&qe(parameter)=qe(name)&qe(0),      /b?x&y?",
			"pr(/b)?qr(x)&qr(y)&&qe(parameter)=qe(name)&qe(0),      /b?x&y&",
			"pr(/b)?qr(x)&qr(y)&qr(?)&qe(parameter)=qe(name)&qe(0), /b?x&y&?",
			"pr(/b)?qr(x)&qr(y)&&&qe(parameter)=qe(name)&qe(0),     /b?x&y&&",
			"pr(/b)?qr(x)&qr(y)&=&qe(parameter)=qe(name)&qe(0),     /b?x&y&=",
			"pr(/b)?qr(x)&qr(y)&qr(z)&qe(parameter)=qe(name)&qe(0), /b?x&y&z",
			"pr(/b)?qr(x)&qr(y)=&qe(parameter)=qe(name)&qe(0),      /b?x&y=",
			"pr(/b)?qr(x)&qr(y)=qr(?)&qe(parameter)=qe(name)&qe(0), /b?x&y=?",
			"pr(/b)?qr(x)&qr(y)=&&qe(parameter)=qe(name)&qe(0),     /b?x&y=&",
			"pr(/b)?qr(x)&qr(y)=qr(=)&qe(parameter)=qe(name)&qe(0), /b?x&y==",
			"pr(/b)?qr(x)&qr(y)=qr(z)&qe(parameter)=qe(name)&qe(0), /b?x&y=z",
			"pr(/b)?qr(x)=&qe(parameter)=qe(name)&qe(0),            /b?x=",
			"pr(/b)?qr(x)=qr(?)&qe(parameter)=qe(name)&qe(0),       /b?x=?",
			"pr(/b)?qr(x)=&&qe(parameter)=qe(name)&qe(0),           /b?x=&",
			"pr(/b)?qr(x)=qr(=)&qe(parameter)=qe(name)&qe(0),       /b?x==",
			"pr(/b)?qr(x)=qr(y)&qe(parameter)=qe(name)&qe(0),       /b?x=y",
			"pr(/b)?qr(x)=qr(y?)&qe(parameter)=qe(name)&qe(0),      /b?x=y?",
			"pr(/b)?qr(x)=qr(y)&&qe(parameter)=qe(name)&qe(0),      /b?x=y&",
			"pr(/b)?qr(x)=qr(y)&qr(?)&qe(parameter)=qe(name)&qe(0), /b?x=y&?",
			"pr(/b)?qr(x)=qr(y)&&&qe(parameter)=qe(name)&qe(0),     /b?x=y&&",
			"pr(/b)?qr(x)=qr(y)&=&qe(parameter)=qe(name)&qe(0),     /b?x=y&=",
			"pr(/b)?qr(x)=qr(y)&qr(z)&qe(parameter)=qe(name)&qe(0), /b?x=y&z",
			"pr(/b)?qr(x)=qr(y=)&qe(parameter)=qe(name)&qe(0),      /b?x=y=",
			"pr(/b)?qr(x)=qr(y=?)&qe(parameter)=qe(name)&qe(0),     /b?x=y=?",
			"pr(/b)?qr(x)=qr(y=)&&qe(parameter)=qe(name)&qe(0),     /b?x=y=&",
			"pr(/b)?qr(x)=qr(y==)&qe(parameter)=qe(name)&qe(0),     /b?x=y==",
			"pr(/b)?qr(x)=qr(y=z)&qe(parameter)=qe(name)&qe(0),     /b?x=y=z",
			"pr(/b&)?qe(parameter)=qe(name)&qe(0),                  /b&",
			"pr(/b=)?qe(parameter)=qe(name)&qe(0),                  /b=" })
	void proxyAddsOneQueryAndOneQueryWithoutValueAndCompletesQueries(String expected, String uri) {
		p = newProxy();
		p.withQuery("parameter", "name");
		p.withQuery("0");
		assertEquals(expected, p.completeQueries(uri));
	}

	@Test
	void proxyAddsHeaders() {
		p = newProxy();
		p.withHeader("x", false);
		p.withHeader("y", 0);
		p.withHeader("z", 2.2);
		HttpFields.Mutable fields = mock(HttpFields.Mutable.class);
		when(request.headers(any())).thenAnswer((invocation) -> {
			Consumer<HttpFields> consumer = invocation.getArgument(0);
			consumer.accept(fields);
			return null;
		});
		p.addHeaders(request);
		verify(fields).add("x", "false");
		verify(fields).add("y", "0");
		verify(fields).add("z", "2.2");
		verifyNoMoreInteractions(fields);
	}

	@Test
	void proxyConsumesNothingAndBuildsNothing() {
		p = newProxy();
		List<Task> tasks = p.consumeBodyAndBuildTasks(request);
		assertTrue(tasks.isEmpty());
	}

	@Test
	void proxyConsumesBodyAndBuildsOneTask() {
		p = spyNewProxy();
		p.withBody(mock(RestBody.class));
		List<OutputStreamRequestContent> contents = mockContents();
		List<Task> tasks = p.consumeBodyAndBuildTasks(request);
		verify(request).body(contents.get(0));
		assertNull(p.getBody());
		assertEquals(1, tasks.size());
	}

	@Test
	void proxyConsumesOnePartAndBuildsOneTask() {
		p = spyNewProxy();
		RestPart part = mockPart("name");
		List<Entry> headers = new ArrayList<>();
		headers.add(new Entry("x", "false"));
		headers.add(new Entry("y", "0"));
		headers.add(new Entry("z", "2.2"));
		when(part.getHeaders()).thenReturn(headers);
		HttpFields.Mutable fields = mock(HttpFields.Mutable.class);
		List<OutputStreamRequestContent> contents = mockContents();
		List<Task> tasks;
		MultiPartRequestContent content;
		try (MockedConstruction<MultiPartRequestContent> construction = mockConstruction(MultiPartRequestContent.class)) {
			try (MockedStatic<HttpFields> fieldsStatic = mockStatic(HttpFields.class)) {
				fieldsStatic.when(() -> HttpFields.build()).thenReturn(fields);
				tasks = p.consumeBodyAndBuildTasks(request);
			}
			content = construction.constructed().get(0);
		}
		verify(fields).add("x", "false");
		verify(fields).add("y", "0");
		verify(fields).add("z", "2.2");
		verifyNoMoreInteractions(fields);
		verify(content).addFieldPart("name", contents.get(0), fields);
		verify(content).close();
		verifyNoMoreInteractions(content);
		verify(request).body(content);
		assertTrue(p.getParts().isEmpty());
		assertEquals(1, tasks.size());
	}

	@Test
	void proxyConsumesTwoPartsAndBuildsTwoTasks() {
		p = spyNewProxy();
		mockPart("name0");
		mockPart("name1");
		HttpFields.Mutable fields0 = mock(HttpFields.Mutable.class);
		HttpFields.Mutable fields1 = mock(HttpFields.Mutable.class);
		Iterator<HttpFields.Mutable> iterator = List.of(fields0, fields1).iterator();
		List<OutputStreamRequestContent> contents = mockContents();
		List<Task> tasks;
		MultiPartRequestContent content;
		try (MockedConstruction<MultiPartRequestContent> construction = mockConstruction(MultiPartRequestContent.class)) {
			try (MockedStatic<HttpFields> fieldsStatic = mockStatic(HttpFields.class)) {
				fieldsStatic.when(() -> HttpFields.build()).thenAnswer((invocation) -> iterator.next());
				tasks = p.consumeBodyAndBuildTasks(request);
			}
			content = construction.constructed().get(0);
		}
		verify(content).addFieldPart("name0", contents.get(0), fields0);
		verify(content).addFieldPart("name1", contents.get(1), fields1);
		verify(content).close();
		verifyNoMoreInteractions(content);
		verify(request).body(content);
		assertTrue(p.getParts().isEmpty());
		assertEquals(2, tasks.size());
	}

	private RestPart mockPart(String name) {
		RestPart part = mock(RestPart.class);
		p.withPart(name, part);
		when(part.getName()).thenReturn(name);
		return part;
	}

	private List<OutputStreamRequestContent> mockContents() {
		List<OutputStreamRequestContent> contents = new ArrayList<>();
		doAnswer((invocation) -> {
			List<Task> tasks = invocation.getArgument(0);
			tasks.add(mock(Task.class));
			OutputStreamRequestContent content = mock(OutputStreamRequestContent.class);
			contents.add(content);
			return content;
		}).when(p).addTaskAndBuildContent(any(), any(RestBody.class));
		return contents;
	}

	@Test
	void proxyAddsTaskAndBuildsContent() {
		p = newProxy();
		assertAddsTaskAndBuildsContent("type/subtype;charset=UTF-8", false);
		verify(mediaCoder, times(0)).encode(any());
	}

	@Test
	void proxyAddsTaskAndBuildsContentInBase64() {
		p = newProxy();
		OutputStream stream = assertAddsTaskAndBuildsContent("type/subtype;charset=UTF-8;base64", true);
		verify(mediaCoder).encode(stream);
	}

	private OutputStream assertAddsTaskAndBuildsContent(String expected, boolean base64) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		OutputStreamRequestContent content = addTaskAndBuildContent(base64, stream);
		assertEquals(expected, content.getContentType());
		assertEquals(REGULAR_CONTENT, stream.toString(StandardCharsets.UTF_8));
		return stream;
	}

	@Test
	void proxyDoesNotAddTaskAndBuildContent() {
		p = newProxy();
		assertDoesNotAddTaskAndBuildContent(false);
	}

	@Test
	void proxyDoesNotAddTaskAndBuildContentInBase64() {
		p = newProxy();
		assertDoesNotAddTaskAndBuildContent(true);
	}

	void assertDoesNotAddTaskAndBuildContent(boolean base64) {
		p = newProxy();
		OutputStream stream = spy(OutputStream.nullOutputStream());
		Throwable cause = new IOException();
		assertDoesNotThrow(() -> {
			doThrow(cause).when(stream).close();
		});
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			addTaskAndBuildContent(base64, stream);
		});
		assertSame(cause, exception.getCause());
	}

	private OutputStreamRequestContent addTaskAndBuildContent(boolean base64, OutputStream stream) {
		RestBody body = mockBody(base64);
		when(body.getCharset()).thenReturn(StandardCharsets.UTF_8);
		Object actual = body.getActual();
		Serializer serializer = mock(Serializer.class);
		when(manager.isBinary(Object.class)).thenReturn(false);
		when(manager.getSerializerType(CONTENT_TYPE, actual, Object.class)).thenReturn(CONTENT_TYPE);
		when(manager.getSerializer(CONTENT_TYPE)).thenReturn(serializer);
		doAnswer((invocation) -> {
			Writer writer = invocation.getArgument(2);
			writer.write(REGULAR_CONTENT);
			return null;
		}).when(serializer).write(eq(actual), eq(Object.class), any(Writer.class));
		return mockContent(body, stream);
	}

	@Test
	void proxyAddsTaskAndBuildsBinaryContent() {
		p = newProxy();
		assertAddsTaskAndBuildsBinaryContent("type/subtype", false);
		verify(mediaCoder, times(0)).encode(any());
	}

	@Test
	void proxyAddsTaskAndBuildsBinaryContentInBase64() {
		p = newProxy();
		OutputStream stream = assertAddsTaskAndBuildsBinaryContent("type/subtype;base64", true);
		verify(mediaCoder).encode(stream);
	}

	private OutputStream assertAddsTaskAndBuildsBinaryContent(String expected, boolean base64) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		OutputStreamRequestContent content = addTaskAndBuildBinaryContent(base64, stream);
		assertEquals(expected, content.getContentType());
		assertEquals(REGULAR_CONTENT, stream.toString(StandardCharsets.UTF_8));
		return stream;
	}

	@Test
	void proxyDoesNotAddTaskAndBuildBinaryContent() {
		p = newProxy();
		assertDoesNotAddTaskAndBuildBinaryContent(false);
	}

	@Test
	void proxyDoesNotAddTaskAndBuildBinaryContentInBase64() {
		p = newProxy();
		assertDoesNotAddTaskAndBuildBinaryContent(true);
	}

	void assertDoesNotAddTaskAndBuildBinaryContent(boolean base64) {
		OutputStream stream = spy(OutputStream.nullOutputStream());
		Throwable cause = new IOException();
		assertDoesNotThrow(() -> {
			doThrow(cause).when(stream).close();
		});
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			addTaskAndBuildBinaryContent(base64, stream);
		});
		assertSame(cause, exception.getCause());
	}

	private OutputStreamRequestContent addTaskAndBuildBinaryContent(boolean base64, OutputStream stream) {
		RestBody body = mockBody(base64);
		Object actual = body.getActual();
		Assembler assembler = mock(Assembler.class);
		when(manager.isBinary(Object.class)).thenReturn(true);
		when(manager.getAssemblerType(CONTENT_TYPE, actual, Object.class)).thenReturn(CONTENT_TYPE);
		when(manager.getAssembler(CONTENT_TYPE)).thenReturn(assembler);
		doAnswer((invocation) -> {
			OutputStream output = invocation.getArgument(2);
			output.write(REGULAR_CONTENT.getBytes(StandardCharsets.UTF_8));
			return null;
		}).when(assembler).write(eq(actual), eq(Object.class), any(OutputStream.class));
		return mockContent(body, stream);
	}

	private RestBody mockBody(boolean base64) {
		Object actual = new Object();
		RestBody body = mock(RestBody.class);
		when(body.getActual()).thenReturn(actual);
		when(body.getType()).thenReturn(Object.class);
		when(body.getContentType()).thenReturn(CONTENT_TYPE);
		when(body.isBase64()).thenReturn(base64);
		return body;
	}

	private OutputStreamRequestContent mockContent(RestBody body, OutputStream stream) {
		MockInitializer<OutputStreamRequestContent> initializer = (mock, context) -> {
			String contentType = (String) context.arguments().get(0);
			when(mock.getContentType()).thenReturn(contentType);
			when(mock.getOutputStream()).thenReturn(stream);
		};
		OutputStreamRequestContent content;
		List<Task> tasks = new ArrayList<>();
		try (MockedConstruction<OutputStreamRequestContent> construction = mockConstruction(OutputStreamRequestContent.class, initializer)) {
			p.addTaskAndBuildContent(tasks, body);
			content = construction.constructed().get(0);
		}
		assertEquals(1, tasks.size());
		Task task = tasks.get(0);
		Consumer<OutputStream> consumer = task.consumer();
		OutputStream output = task.stream();
		consumer.accept(output);
		return content;
	}

	@Test
	void proxySends() {
		mockStart();
		p = newProxy();

		int status = 50;
		HttpFields fields = mock(HttpFields.class);
		Headers headers = mock(Headers.class);
		InputStream input = InputStream.nullInputStream();
		RestResponse restResponse = mock(RestResponse.class);
		when(response.getStatus()).thenReturn(status);
		when(response.getHeaders()).thenReturn(fields);
		when(fields.get("Content-Type")).thenReturn(CONTENT_TYPE);
		MockInitializer<InputStreamResponseListener> initializer = (mock, context) -> {
			when(mock.get(RestClient.TIMEOUT, TimeUnit.SECONDS)).thenReturn(response);
			when(mock.getInputStream()).thenReturn(input);
		};

		Consumer<OutputStream> consumer = (output) -> {
			assertDoesNotThrow(() -> {
				output.write(REGULAR_CONTENT.getBytes(StandardCharsets.UTF_8));
				output.close();
			});
		};
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		List<Task> tasks = List.of(new Task(consumer, output));

		InputStreamResponseListener listener;
		try (MockedConstruction<InputStreamResponseListener> construction = mockConstruction(InputStreamResponseListener.class, initializer)) {
			try (MockedStatic<Headers> headersStatic = mockStatic(Headers.class)) {
				headersStatic.when(() -> Headers.newInstance(fields)).thenReturn(headers);
				try (MockedStatic<RestResponse> responseStatic = mockStatic(RestResponse.class)) {
					responseStatic.when(() -> RestResponse.newInstance(manager, status, headers, CONTENT_TYPE, input)).thenReturn(restResponse);
					assertSame(restResponse, p.send(request, tasks));
				}
			}
			listener = construction.constructed().get(0);
		}
		verify(c).start();
		verify(request).send(listener);
		assertEquals(REGULAR_CONTENT, output.toString(StandardCharsets.UTF_8));
	}

	@Test
	void proxyDoesNotSendIfListenerThrowsExecutionException() {
		mockStart();
		p = newProxy();
		List<Task> tasks = List.of();
		Throwable cause = new Throwable();
		ExecutionException exception = new ExecutionException(cause);
		Throwable throwable = assertThrows(ClientException.class, () -> {
			try (MockedConstruction<InputStreamResponseListener> construction = mockListenerConstruction(exception)) {
				p.send(request, tasks);
			}
		});
		assertSame(cause, throwable.getCause());
	}

	@Test
	void proxyDoesNotSendIfListenerThrowsTimeoutException() {
		mockStart();
		p = newProxy();
		List<Task> tasks = List.of();
		TimeoutException exception = new TimeoutException();
		Throwable throwable = assertThrows(ClientException.class, () -> {
			try (MockedConstruction<InputStreamResponseListener> construction = mockListenerConstruction(exception)) {
				p.send(request, tasks);
			}
		});
		assertSame(exception, throwable.getCause());
	}

	@Test
	void proxyDoesNotSendIfListenerThrowsInterruptedException() {
		mockStart();
		p = newProxy();
		List<Task> tasks = List.of();
		InterruptedException exception = new InterruptedException();
		Throwable throwable = assertThrows(AssertionError.class, () -> {
			try (MockedConstruction<InputStreamResponseListener> construction = mockListenerConstruction(exception)) {
				p.send(request, tasks);
			}
		});
		assertSame(exception, throwable.getCause());
	}

	private MockedConstruction<InputStreamResponseListener> mockListenerConstruction(Exception exception) {
		MockInitializer<InputStreamResponseListener> initializer = (mock, context) -> {
			when(mock.get(RestClient.TIMEOUT, TimeUnit.SECONDS)).thenThrow(exception);
		};
		return mockConstruction(InputStreamResponseListener.class, initializer);
	}

	private void mockStart() {
		c = spy(c);
		doNothing().when(c).start();
	}

	private Proxy spyNewProxy() {
		return spy(newProxy());
	}

	private Proxy newProxy() {
		return c.new Proxy();
	}
}
