package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.eclipse.jetty.client.ContentDecoder;
import org.eclipse.jetty.client.GZIPContentDecoder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.http3.client.http.HttpClientTransportOverHTTP3;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.BuilderTest;

class RestClientBuilderTest extends BuilderTest {
	private RestClientBuilder b;

	@Test
	void initializesWithoutFactory() {
		assertNull(b.getFactory());
	}

	@Test
	void setsTrustStore() {
		String path = "path";
		String password = "password";
		try (MockedConstruction<SslContextFactory.Client> construction = mockConstruction(SslContextFactory.Client.class)) {
			b.withTrustStore(path, password);
			SslContextFactory.Client factory = construction.constructed().get(0);
			verify(factory).setTrustStorePath(path);
			verify(factory).setTrustStorePassword(password);
			assertEquals(factory, b.getFactory());
		}
	}

	@Test
	void doesNotSetTrustStoreWithNullPath() {
		assertThrows(NullPointerException.class, () -> {
			b.withTrustStore(null, "password");
		});
	}

	@Test
	void doesNotSetTrustStoreWithEmptyPath() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withTrustStore("", "password");
		});
	}

	@Test
	void doesNotSetTrustStoreWithNullPassword() {
		assertThrows(NullPointerException.class, () -> {
			b.withTrustStore("path", null);
		});
	}

	@Test
	void doesNotSetTrustStoreWithEmptyPassword() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withTrustStore("path", "");
		});
	}

	@Test
	void builds() {
		RestClient client = b.build("http://a");
		assertSame(b.getManager(), client.getManager());
		assertEquals(StandardCharsets.UTF_8, client.getUrlCharset());
		assertEquals("http://a", client.getUrlPrefix());
		HttpClient jettyClient = client.getJettyClient();
		assertInstanceOf(HttpCookieStore.Empty.class, jettyClient.getCookieStore());
		assertTrue(jettyClient.isFollowRedirects());
		Set<ContentDecoder.Factory> factories = jettyClient.getContentDecoderFactories();
		assertEquals(1, factories.size());
		assertInstanceOf(GZIPContentDecoder.Factory.class, factories.iterator().next());
		assertNull(jettyClient.getSslContextFactory());
		assertInstanceOf(HttpClientTransportDynamic.class, jettyClient.getTransport());
	}

	@Test
	void buildsWithUrlCharset() {
		b.withUrlCharset(StandardCharsets.ISO_8859_1);
		RestClient client = b.build("http://a");
		assertEquals(StandardCharsets.ISO_8859_1, client.getUrlCharset());
	}

	@Test
	void buildsWithoutRedirection() {
		b.withoutRedirection();
		RestClient client = b.build("http://a");
		assertFalse(client.getJettyClient().isFollowRedirects());
	}

	@Test
	void buildsWithoutCompression() {
		b.withoutCompression();
		RestClient client = b.build("http://a");
		assertTrue(client.getJettyClient().getContentDecoderFactories().isEmpty());
	}

	@Test
	void buildsWithTrustStore() {
		b.withTrustStore("path", "password");
		RestClient client = b.build("http://a");
		assertSame(b.getFactory(), client.getJettyClient().getSslContextFactory());
	}

	@Test
	void buildsWithHttps() {
		RestClient client = b.build("https://a");
		assertEquals("https://a", client.getUrlPrefix());
	}

	@Test
	void buildsWithReserved() {
		RestClient client = b.build("http://a%20 %2B+%25%%2F/");
		assertEquals("http://a%20 %2B+%25%%2F", client.getUrlPrefix());
	}

	@Test
	void buildsWithWhitespaces() {
		RestClient client = b.build(" \t\nhttp://a \t\n");
		assertEquals("http://a", client.getUrlPrefix());
	}

	@Test
	void buildsWithItems() {
		RestClient client = b.build("http://a/0/1/2");
		assertEquals("http://a/0/1/2", client.getUrlPrefix());
	}

	@Test
	void buildsWithSlashes() {
		RestClient client = b.build("http://a///");
		assertEquals("http://a", client.getUrlPrefix());
	}

	@Test
	void buildsWithHttpsAndReservedAndWhitespacesAndItemsAndSlashes() {
		RestClient client = b.build(" \t\nhttps://a%20 %2B+%25%%2F/0/%20 %2B+/1/%25%2F/2/// \t\n");
		assertEquals("https://a%20 %2B+%25%%2F/0/%20%20%2B%2B/1/%25%2F/2", client.getUrlPrefix());
	}

	@Test
	void buildsWithHttp1() {
		RestClient client = b.build1("http://a");
		HttpClient jettyClient = client.getJettyClient();
		assertNull(jettyClient.getSslContextFactory());
		assertInstanceOf(HttpClientTransportOverHTTP.class, jettyClient.getTransport());
	}

	@Test
	void buildsWithHttps1() {
		b.withTrustStore("path", "password");
		RestClient client = b.build1("https://a");
		HttpClient jettyClient = client.getJettyClient();
		assertSame(b.getFactory(), jettyClient.getSslContextFactory());
		assertInstanceOf(HttpClientTransportOverHTTP.class, jettyClient.getTransport());
	}

	@Test
	void buildsWithHttp2() {
		RestClient client = b.build2("http://a");
		HttpClient jettyClient = client.getJettyClient();
		assertNull(jettyClient.getSslContextFactory());
		assertInstanceOf(HttpClientTransportOverHTTP2.class, jettyClient.getTransport());
	}

	@Test
	void buildsWithHttps2() {
		b.withTrustStore("path", "password");
		RestClient client = b.build2("https://a");
		HttpClient jettyClient = client.getJettyClient();
		assertSame(b.getFactory(), jettyClient.getSslContextFactory());
		assertInstanceOf(HttpClientTransportOverHTTP2.class, jettyClient.getTransport());
	}

	@Test
	void buildsWithHttp3() {
		RestClient client = b.build3("http://a");
		HttpClient jettyClient = client.getJettyClient();
		assertNull(jettyClient.getSslContextFactory());
		assertInstanceOf(HttpClientTransportOverHTTP3.class, jettyClient.getTransport());
	}

	@Test
	void buildsWithHttps3() {
		b.withTrustStore("path", "password");
		RestClient client = b.build3("https://a");
		HttpClient jettyClient = client.getJettyClient();
		assertSame(b.getFactory(), jettyClient.getSslContextFactory());
		assertInstanceOf(HttpClientTransportOverHTTP3.class, jettyClient.getTransport());
	}

	@Test
	void doesNotBuildIfUrlPrefixIsNull() {
		assertThrows(NullPointerException.class, () -> {
			b.build(null);
		});
	}

	@Test
	void doesNotBuildIfUrlPrefixDoesNotStartCorrectly() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build("file://a");
		});
	}

	@Test
	void doesNotBuildIfUrlPrefixIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build("http:// \t\n");
		});
	}

	@Test
	void doesNotBuildIfUrlPrefixAuthorityIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build("http:///a");
		});
	}

	@Override
	protected Builder<?> newInstance() {
		b = new RestClientBuilder();
		return b;
	}
}
