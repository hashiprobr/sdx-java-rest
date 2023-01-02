package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.base.BuilderTest;

class RESTClientBuilderTest extends BuilderTest {
	private RESTClientBuilder b;

	@BeforeEach
	void setUp() {
		mockConstructions();
		b = new RESTClientBuilder();
		getTransformer();
		setBuilder(b);
	}

	@AfterEach
	void tearDown() {
		closeConstructions();
	}

	@Test
	void initializesWithURLCharsetUTF8() {
		testInitializesWithURLCharsetUTF8();
	}

	@Test
	void initializesWithoutNullBody() {
		testInitializesWithoutNullBody();
	}

	@Test
	void initializesWithoutRedirection() {
		testInitializesWithoutRedirection();
	}

	@Test
	void initializesWithCompression() {
		testInitializesWithCompression();
	}

	@Test
	void initializesWithoutFactory() {
		assertNull(b.getFactory());
	}

	@Test
	void addsBinary() {
		testAddsBinary();
	}

	@Test
	void putsAssembler() {
		testPutsAssembler();
	}

	@Test
	void removesAssembler() {
		testRemovesAssembler();
	}

	@Test
	void putsDisassembler() {
		testPutsDisassembler();
	}

	@Test
	void removesDisassembler() {
		testRemovesDisassembler();
	}

	@Test
	void putsSerializer() {
		testPutsSerializer();
	}

	@Test
	void putsGsonSerializer() {
		testPutsGsonSerializer();
	}

	@Test
	void putsUncheckedSerializer() {
		testPutsUncheckedSerializer();
	}

	@Test
	void doesNotPutUncheckedSerializerIfNull() {
		testDoesNotPutUncheckedSerializerIfNull();
	}

	@Test
	void doesNotPutUncheckedSerializerIfBlank() {
		testDoesNotPutUncheckedSerializerIfBlank();
	}

	@Test
	void removesSerializer() {
		testRemovesSerializer();
	}

	@Test
	void putsDeserializer() {
		testPutsDeserializer();
	}

	@Test
	void putsGsonDeserializer() {
		testPutsGsonDeserializer();
	}

	@Test
	void putsSafeDeserializer() {
		testPutsSafeDeserializer();
	}

	@Test
	void doesNotPutSafeDeserializerIfNull() {
		testDoesNotPutSafeDeserializerIfNull();
	}

	@Test
	void doesNotPutSafeDeserializerIfBlank() {
		testDoesNotPutSafeDeserializerIfBlank();
	}

	@Test
	void removesDeserializer() {
		testRemovesDeserializer();
	}

	@Test
	void setsURLCharset() {
		testSetsURLCharset();
	}

	@Test
	void doesNotSetURLCharset() {
		testDoesNotSetURLCharset();
	}

	@Test
	void setsNullBody() {
		testSetsNullBody();
	}

	@Test
	void setsRedirection() {
		testSetsRedirection();
	}

	@Test
	void setsCompression() {
		testSetsCompression();
	}

	@Test
	void setsTrustStore() {
		try (MockedConstruction<SslContextFactory.Client> construction = mockConstruction(SslContextFactory.Client.class)) {
			String path = "path";
			String password = "password";
			b.withTrustStore(path, password);
			SslContextFactory.Client factory = construction.constructed().get(0);
			verify(factory).setTrustStorePath(path);
			verify(factory).setTrustStorePassword(password);
			assertEquals(factory, b.getFactory());
		}
	}

	@Test
	void doesNotSetTrustStoreIfPathIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withTrustStore(null, "password");
		});
		assertNull(b.getFactory());
	}

	@Test
	void doesNotSetTrustStoreIfPathIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withTrustStore("", "password");
		});
		assertNull(b.getFactory());
	}

	@Test
	void doesNotSetTrustStoreIfPasswordIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withTrustStore("path", null);
		});
		assertNull(b.getFactory());
	}

	@Test
	void doesNotSetTrustStoreIfPasswordIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withTrustStore("path", "");
		});
		assertNull(b.getFactory());
	}

	@Test
	void builds() {
		RESTClient client = b.build("http://a");
		assertSame(b.getTransformer(), client.getTransformer());
		assertSame(b.getURLCharset(), client.getURLCharset());
		assertNull(client.getNone());
		HttpClient jettyClient = client.getJettyClient();
		assertInstanceOf(HttpClientTransportDynamic.class, jettyClient.getTransport());
		assertInstanceOf(HttpCookieStore.Empty.class, jettyClient.getCookieStore());
		assertFalse(jettyClient.isFollowRedirects());
		Set<ContentDecoder.Factory> factories = jettyClient.getContentDecoderFactories();
		int size = 0;
		for (ContentDecoder.Factory factory : factories) {
			assertInstanceOf(GZIPContentDecoder.Factory.class, factory);
			size++;
		}
		assertEquals(1, size);
		assertNull(jettyClient.getSslContextFactory());
		assertEquals("http://a", client.getURLPrefix());
	}

	@Test
	void buildsWithNullBody() {
		b.withNullBody();
		RESTClient client = b.build("http://a");
		assertEquals("", client.getNone());
	}

	@Test
	void buildsWithRedirection() {
		b.withRedirection();
		RESTClient client = b.build("http://a");
		assertTrue(client.getJettyClient().isFollowRedirects());
	}

	@Test
	void buildsWithoutCompression() {
		b.withoutCompression();
		RESTClient client = b.build("http://a");
		assertTrue(client.getJettyClient().getContentDecoderFactories().isEmpty());
	}

	@Test
	void buildsWithTrustStore() {
		b.withTrustStore("path", "password");
		RESTClient client = b.build("http://a");
		SslContextFactory.Client factory = client.getJettyClient().getSslContextFactory();
		assertNotNull(factory);
		assertSame(b.getFactory(), factory);
	}

	@Test
	void buildsWithHTTPS() {
		RESTClient client = b.build("https://a");
		assertEquals("https://a", client.getURLPrefix());
	}

	@Test
	void buildsWithReserved() {
		RESTClient client = b.build("http://a %20+%2B%%2F");
		assertEquals("http://a %20+%2B%%2F", client.getURLPrefix());
	}

	@Test
	void buildsWithWhitespaces() {
		RESTClient client = b.build(" \t\nhttp://a \t\n");
		assertEquals("http://a", client.getURLPrefix());
	}

	@Test
	void buildsWithItems() {
		RESTClient client = b.build("http://a/0/1/2");
		assertEquals("http://a/0/1/2", client.getURLPrefix());
	}

	@Test
	void buildsWithSlashes() {
		RESTClient client = b.build("http://a///");
		assertEquals("http://a", client.getURLPrefix());
	}

	@Test
	void buildsWithHTTPSAndReservedAndWhitespacesAndItemsAndSlashes() {
		RESTClient client = b.build(" \t\nhttps://a %20+%2B%%2F/0/ %20+%2B/1/%25%2F/2/// \t\n");
		assertEquals("https://a %20+%2B%%2F/0/%20%20%2B%2B/1/%25%2F/2", client.getURLPrefix());
	}

	@Test
	void buildsWithHTTP1() {
		RESTClient client = b.build1("http://a");
		HttpClient jettyClient = client.getJettyClient();
		assertInstanceOf(HttpClientTransportOverHTTP.class, jettyClient.getTransport());
		assertNull(jettyClient.getSslContextFactory());
	}

	@Test
	void buildsWithHTTPS1() {
		b.withTrustStore("path", "password");
		RESTClient client = b.build1("http://a");
		SslContextFactory.Client factory = client.getJettyClient().getSslContextFactory();
		assertNotNull(factory);
		assertSame(b.getFactory(), factory);
	}

	@Test
	void buildsWithHTTP2() {
		RESTClient client = b.build2("http://a");
		HttpClient jettyClient = client.getJettyClient();
		assertNull(jettyClient.getSslContextFactory());
		assertInstanceOf(HttpClientTransportOverHTTP2.class, jettyClient.getTransport());
	}

	@Test
	void buildsWithHTTPS2() {
		b.withTrustStore("path", "password");
		RESTClient client = b.build2("http://a");
		SslContextFactory.Client factory = client.getJettyClient().getSslContextFactory();
		assertNotNull(factory);
		assertSame(b.getFactory(), factory);
	}

	@Test
	void buildsWithHTTPS3() {
		b.withTrustStore("path", "password");
		RESTClient client = b.build3("http://a");
		HttpClient jettyClient = client.getJettyClient();
		assertInstanceOf(HttpClientTransportOverHTTP3.class, jettyClient.getTransport());
		SslContextFactory.Client factory = jettyClient.getSslContextFactory();
		assertNotNull(factory);
		assertSame(b.getFactory(), factory);
	}

	@Test
	void doesNotBuildIfURLPrefixIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build(null);
		});
	}

	@Test
	void doesNotBuildIfURLPrefixDoesNotStartCorrectly() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build("file://a");
		});
	}

	@Test
	void doesNotBuildIfURLPrefixIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build("http:// \t\n");
		});
	}

	@Test
	void doesNotBuildIfURLPrefixAuthorityIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build("http:///a");
		});
	}

	@Test
	void doesNotBuildWithHTTP3() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build3("http://a");
		});
	}
}
