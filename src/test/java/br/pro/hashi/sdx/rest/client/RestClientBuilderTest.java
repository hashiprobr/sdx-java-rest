package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
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
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.BuilderTest;
import br.pro.hashi.sdx.rest.constant.Defaults;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

class RestClientBuilderTest extends BuilderTest {
	private static final String URL_PREFIX = " \t\nhttp://a/b \t\n";

	private MockedStatic<RestClient> clientStatic;
	private RestClientBuilder b;

	@Test
	void initializesWithoutFactory() {
		assertNull(b.getFactory());
	}

	@Test
	void setsTrustStore() {
		String path = "path";
		String password = "password";
		SslContextFactory.Client factory;
		try (MockedConstruction<SslContextFactory.Client> construction = mockConstruction(SslContextFactory.Client.class)) {
			assertSame(b, b.withTrustStore(path, password));
			factory = construction.constructed().get(0);
		}
		verify(factory).setTrustStorePath(path);
		verify(factory).setTrustStorePassword(password);
		assertEquals(factory, b.getFactory());
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
		RestClient client = b.build(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithoutFactory(jettyClient);
		assertWithoutStatic(jettyClient);
	}

	@Test
	void buildsWithLocale() {
		b.withLocale(Locale.ROOT);
		RestClient client = b.build(URL_PREFIX);
		assertEquals(Locale.ROOT, client.getLocale());
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithoutFactory(jettyClient);
		assertWithoutStatic(jettyClient);
	}

	@Test
	void buildsWithUrlCharset() {
		b.withUrlCharset(StandardCharsets.ISO_8859_1);
		RestClient client = b.build(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertEquals(StandardCharsets.ISO_8859_1, client.getUrlCharset());
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithoutFactory(jettyClient);
		assertWithoutStatic(jettyClient);
	}

	@Test
	void buildsWithoutRedirection() {
		b.withoutRedirection();
		RestClient client = b.build(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertFalse(jettyClient.isFollowRedirects());
		assertWithCompression(jettyClient);
		assertWithoutFactory(jettyClient);
		assertWithoutStatic(jettyClient);
	}

	@Test
	void buildsWithoutCompression() {
		b.withoutCompression();
		RestClient client = b.build(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertTrue(jettyClient.getContentDecoderFactories().isEmpty());
		assertWithoutFactory(jettyClient);
		assertWithoutStatic(jettyClient);
	}

	@Test
	void buildsWithTrustStore() {
		b.withTrustStore("path", "password");
		RestClient client = b.build(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithFactory(jettyClient);
		assertWithoutStatic(jettyClient);
	}

	@Test
	void buildsWithHttpsProtocol() {
		RestClient client = b.build(" \t\nhttps://a/b \t\n");
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertEquals("https://a/pr(b)", client.getUrlPrefix());
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithoutFactory(jettyClient);
		assertWithoutStatic(jettyClient);
	}

	@Test
	void buildsWithoutSuffix() {
		RestClient client = b.build(" \t\nhttp://a \t\n");
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertEquals("http://a", client.getUrlPrefix());
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithoutFactory(jettyClient);
		assertWithoutStatic(jettyClient);
	}

	@Test
	void buildsWithHttp1() {
		RestClient client = b.buildWithHttp1(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithoutFactory(jettyClient);
		assertWithStaticHttp1(jettyClient);
	}

	@Test
	void buildsWithTrustStoreAndHttp1() {
		b.withTrustStore("path", "password");
		RestClient client = b.buildWithHttp1(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithFactory(jettyClient);
		assertWithStaticHttp1(jettyClient);
	}

	@Test
	void buildsWithHttp2() {
		RestClient client = b.buildWithHttp2(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithoutFactory(jettyClient);
		assertWithStaticHttp2(jettyClient);
	}

	@Test
	void buildsWithTrustStoreAndHttp2() {
		b.withTrustStore("path", "password");
		RestClient client = b.buildWithHttp2(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithFactory(jettyClient);
		assertWithStaticHttp2(jettyClient);
	}

	@Test
	void buildsWithHttp3() {
		RestClient client = b.buildWithHttp3(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithoutFactory(jettyClient);
		assertWithStaticHttp3(jettyClient);
	}

	@Test
	void buildsWithTrustStoreAndHttp3() {
		b.withTrustStore("path", "password");
		RestClient client = b.buildWithHttp3(URL_PREFIX);
		assertWithDefaultLocale(client);
		assertWithDefaultUrlCharset(client);
		assertWithDefaultUrlPrefix(client);
		HttpClient jettyClient = getJettyClient(client);
		assertWithRedirection(jettyClient);
		assertWithCompression(jettyClient);
		assertWithFactory(jettyClient);
		assertWithStaticHttp3(jettyClient);
	}

	private void assertWithDefaultLocale(RestClient client) {
		assertEquals(Defaults.LOCALE, client.getLocale());
	}

	private void assertWithDefaultUrlCharset(RestClient client) {
		assertEquals(StandardCharsets.UTF_8, client.getUrlCharset());
	}

	private void assertWithDefaultUrlPrefix(RestClient client) {
		assertEquals("http://a/pr(b)", client.getUrlPrefix());
	}

	private HttpClient getJettyClient(RestClient client) {
		assertSame(managerCopy, client.getManager());
		HttpClient jettyClient = client.getJettyClient();
		assertInstanceOf(HttpCookieStore.Empty.class, jettyClient.getCookieStore());
		return jettyClient;
	}

	private void assertWithRedirection(HttpClient jettyClient) {
		assertTrue(jettyClient.isFollowRedirects());
	}

	private void assertWithCompression(HttpClient jettyClient) {
		Set<ContentDecoder.Factory> factories = jettyClient.getContentDecoderFactories();
		assertEquals(1, factories.size());
		assertInstanceOf(GZIPContentDecoder.Factory.class, factories.iterator().next());
	}

	private void assertWithoutFactory(HttpClient jettyClient) {
		assertNull(jettyClient.getSslContextFactory());
	}

	private void assertWithFactory(HttpClient jettyClient) {
		assertSame(b.getFactory(), jettyClient.getSslContextFactory());
	}

	private void assertWithoutStatic(HttpClient jettyClient) {
		assertInstanceOf(HttpClientTransportDynamic.class, jettyClient.getTransport());
	}

	private void assertWithStaticHttp1(HttpClient jettyClient) {
		assertInstanceOf(HttpClientTransportOverHTTP.class, jettyClient.getTransport());
	}

	private void assertWithStaticHttp2(HttpClient jettyClient) {
		assertInstanceOf(HttpClientTransportOverHTTP2.class, jettyClient.getTransport());
	}

	private void assertWithStaticHttp3(HttpClient jettyClient) {
		assertInstanceOf(HttpClientTransportOverHTTP3.class, jettyClient.getTransport());
	}

	@Test
	void doesNotBuildWithIfUrlPrefixIsNull() {
		assertThrows(NullPointerException.class, () -> {
			b.build(null);
		});
	}

	@Test
	void doesNotBuildIfUrlPrefixDoesNotStartWithHttpProtocol() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build("file://a/b");
		});
	}

	@Test
	void doesNotBuildIfUrlPrefixPathIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build("http:// \t\n");
		});
	}

	@Test
	void doesNotBuildIfUrlPrefixAuthorityIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.build("http:///a/b");
		});
	}

	@Override
	protected Builder<?> newInstance() {
		clientStatic = mockStatic(RestClient.class);
		clientStatic.when(() -> RestClient.newInstance(any(TransformManager.class), any(HttpClient.class), any(Locale.class), any(Charset.class), any(String.class))).thenAnswer((invocation) -> {
			TransformManager manager = invocation.getArgument(0);
			HttpClient jettyClient = invocation.getArgument(1);
			Locale locale = invocation.getArgument(2);
			Charset urlCharset = invocation.getArgument(3);
			String urlPrefix = invocation.getArgument(4);
			RestClient client = mock(RestClient.class);
			when(client.getManager()).thenReturn(manager);
			when(client.getLocale()).thenReturn(locale);
			when(client.getUrlCharset()).thenReturn(urlCharset);
			when(client.getUrlPrefix()).thenReturn(urlPrefix);
			when(client.getJettyClient()).thenReturn(jettyClient);
			return client;
		});
		b = new RestClientBuilder();
		return b;
	}

	@Override
	protected void close() {
		clientStatic.close();
	}
}
