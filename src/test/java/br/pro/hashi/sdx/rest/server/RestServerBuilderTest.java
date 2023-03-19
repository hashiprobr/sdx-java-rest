package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnector;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConfiguration.Customizer;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.reflections.Reflections;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.BuilderTest;
import br.pro.hashi.sdx.rest.server.exception.ResourceException;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithBlank;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithNull;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithoutCompliance;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithoutDecoding;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithoutSlash;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import jakarta.servlet.MultipartConfigElement;

class RestServerBuilderTest extends BuilderTest {
	private static final String VALID_PACKAGE = "br.pro.hashi.sdx.rest.server.mock.valid";
	private static final String INVALID_PACKAGE = "br.pro.hashi.sdx.rest.server.mock.invalid";

	private RestServerBuilder b;

	@Override
	protected Builder<?> newInstance() {
		b = new RestServerBuilder();
		return b;
	}

	@Test
	void initializesWithoutGateway() {
		assertTrue(b.getGatewayTypes().isEmpty());
	}

	@Test
	void initializesWithConcreteFormatter() {
		assertEquals(ConcreteFormatter.class, b.getFormatter().getClass());
	}

	@Test
	void initializesWithoutFactory() {
		assertNull(b.getFactory());
	}

	@Test
	void initializesWithDefaultConfig() {
		assertEquals("", b.getElement().getLocation());
	}

	@Test
	void initializesWithUnambiguousCompliance() {
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, b.getCompliance());
	}

	@Test
	void initializesWithDefaultClearPort() {
		assertEquals(8080, b.getClearPort());
	}

	@Test
	void initializesWithDefaultSecurePort() {
		assertEquals(8443, b.getSecurePort());
	}

	@Test
	void initializesWithDefaultPort3() {
		assertEquals(8843, b.getPort3());
	}

	@Test
	void initializesWithoutHttp3() {
		assertFalse(b.isHttp3());
	}

	@Test
	void initializesWithHttp2() {
		assertTrue(b.isHttp2());
	}

	@Test
	void initializesWithCors() {
		assertTrue(b.isCors());
	}

	@Test
	void initializesWithLogging() {
		assertTrue(b.isLogging());
	}

	@Test
	void addsGateway() {
		assertSame(b, b.withGateway(RuntimeException.class));
		assertEquals(Set.of(RuntimeException.class), b.getGatewayTypes());
	}

	@Test
	void doesNotAddGateway() {
		assertThrows(NullPointerException.class, () -> {
			b.withGateway(null);
		});
		assertTrue(b.getGatewayTypes().isEmpty());
	}

	@Test
	void setsErrorFormatter() {
		ErrorFormatter formatter = new ErrorFormatter() {};
		assertSame(b, b.withErrorFormatter(formatter));
		assertNotEquals(ConcreteFormatter.class, b.getFormatter().getClass());
	}

	@Test
	void doesNotSetErrorFormatter() {
		assertThrows(NullPointerException.class, () -> {
			b.withErrorFormatter(null);
		});
		assertEquals(ConcreteFormatter.class, b.getFormatter().getClass());
	}

	@Test
	void setsKeyStore() {
		try (MockedConstruction<SslContextFactory.Server> construction = mockConstruction(SslContextFactory.Server.class)) {
			String path = "path";
			String password = "password";
			b.withKeyStore(path, password);
			SslContextFactory.Server factory = construction.constructed().get(0);
			verify(factory).setKeyStorePath(path);
			verify(factory).setKeyStorePassword(password);
			assertEquals(factory, b.getFactory());
		}
	}

	@Test
	void doesNotSetKeyStoreIfPathIsNull() {
		assertThrows(NullPointerException.class, () -> {
			b.withKeyStore(null, "password");
		});
		assertNull(b.getFactory());
	}

	@Test
	void doesNotSetKeyStoreIfPathIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withKeyStore("", "password");
		});
		assertNull(b.getFactory());
	}

	@Test
	void doesNotSetKeyStoreIfPasswordIsNull() {
		assertThrows(NullPointerException.class, () -> {
			b.withKeyStore("path", null);
		});
		assertNull(b.getFactory());
	}

	@Test
	void doesNotSetKeyStoreIfPasswordIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withKeyStore("path", "");
		});
		assertNull(b.getFactory());
	}

	@Test
	void setsMultipartConfig() {
		assertSame(b, b.withMultipartConfig(new MultipartConfigElement("location")));
		assertEquals("location", b.getElement().getLocation());
	}

	@Test
	void doesNotSetMultipartConfig() {
		assertThrows(NullPointerException.class, () -> {
			b.withMultipartConfig(null);
		});
		assertEquals("", b.getElement().getLocation());
	}

	@Test
	void setsUriCompliance() {
		assertSame(b, b.withUriCompliance(UriCompliance.UNSAFE));
		assertEquals(UriCompliance.UNSAFE, b.getCompliance());
	}

	@Test
	void doesNotSetUriCompliance() {
		assertThrows(NullPointerException.class, () -> {
			b.withUriCompliance(null);
		});
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, b.getCompliance());
	}

	@Test
	void setsClearPort() {
		assertSame(b, b.withClearPort(80));
		assertEquals(80, b.getClearPort());
	}

	@Test
	void setsSecurePort() {
		assertSame(b, b.withSecurePort(443));
		assertEquals(443, b.getSecurePort());
	}

	@Test
	void setsPort3() {
		assertSame(b, b.withPort3(843));
		assertEquals(843, b.getPort3());
	}

	@Test
	void setsHttp3() {
		assertSame(b, b.withHttp3());
		assertTrue(b.isHttp3());
	}

	@Test
	void setsHttp2() {
		assertSame(b, b.withoutHttp2());
		assertFalse(b.isHttp2());
	}

	@Test
	void setsCors() {
		assertSame(b, b.withoutCors());
		assertFalse(b.isCors());
	}

	@Test
	void setsLogging() {
		assertSame(b, b.withoutLogging());
		assertFalse(b.isLogging());
	}

	@Test
	void builds() {
		RestServer server = b.build(VALID_PACKAGE);
		assertEquals("http", server.getScheme());
		assertEquals(8080, server.getClearPort());
		assertEquals(8080, server.getMainPort());
		assertEquals(-1, server.getAltPort());
		Server jettyServer = server.getJettyServer();
		Connector[] connectors = jettyServer.getConnectors();
		assertEquals(1, connectors.length);
		ServerConnector connector = (ServerConnector) connectors[0];
		assertEquals(8080, connector.getPort());
		Iterator<ConnectionFactory> iterator = connector.getConnectionFactories().iterator();
		HttpConnectionFactory h11 = (HttpConnectionFactory) iterator.next();
		HttpConfiguration configuration = h11.getHttpConfiguration();
		assertEquals(HttpCompliance.RFC7230, configuration.getHttpCompliance());
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, configuration.getUriCompliance());
		HTTP2CServerConnectionFactory h2c = (HTTP2CServerConnectionFactory) iterator.next();
		assertSame(configuration, h2c.getHttpConfiguration());
		assertFalse(iterator.hasNext());
		GzipHandler gzipHandler = (GzipHandler) jettyServer.getHandler();
		Handler handler = (Handler) gzipHandler.getHandler();
		assertSame(b.getCache(), handler.getCache());
		assertSame(b.getFacade(), handler.getFacade());
		assertEquals(StandardCharsets.UTF_8, handler.getUrlCharset());
		assertSame(b.getGatewayTypes(), handler.getGatewayTypes());
		assertSame(b.getFormatter(), handler.getFormatter());
		assertSame(b.getElement(), handler.getElement());
		assertTrue(handler.isCors());
		assertTrue(handler.isLogging());
		Tree tree = handler.getTree();
		List<String> itemList = new ArrayList<>();
		assertNotNull(tree.getNodeAndAddItems(new String[] { "b" }, itemList));
		assertNotNull(tree.getNodeAndAddItems(new String[] { "c" }, itemList));
		assertNotNull(tree.getNodeAndAddItems(new String[] { "b", "c" }, itemList));
		assertNotNull(tree.getNodeAndAddItems(new String[] { "c", "d" }, itemList));
	}

	@Test
	void buildsWithUrlCharset() {
		b.withUrlCharset(StandardCharsets.ISO_8859_1);
		RestServer server = b.build(VALID_PACKAGE);
		GzipHandler gzipHandler = (GzipHandler) server.getJettyServer().getHandler();
		Handler handler = (Handler) gzipHandler.getHandler();
		assertEquals(StandardCharsets.ISO_8859_1, handler.getUrlCharset());
	}

	@Test
	void buildsWithRedirection() {
		b.withRedirection();
		RestServer server = b.build(VALID_PACKAGE);
		SecuredRedirectHandler redirectHandler = (SecuredRedirectHandler) server.getJettyServer().getHandler();
		GzipHandler gzipHandler = (GzipHandler) redirectHandler.getHandler();
		assertInstanceOf(Handler.class, gzipHandler.getHandler());
	}

	@Test
	void buildsWithoutCompression() {
		b.withoutCompression();
		RestServer server = b.build(VALID_PACKAGE);
		assertInstanceOf(Handler.class, server.getJettyServer().getHandler());
	}

	@Test
	void buildsWithRedirectionWithoutCompression() {
		b.withRedirection();
		b.withoutCompression();
		RestServer server = b.build(VALID_PACKAGE);
		SecuredRedirectHandler redirectHandler = (SecuredRedirectHandler) server.getJettyServer().getHandler();
		assertInstanceOf(Handler.class, redirectHandler.getHandler());
	}

	@Test
	void buildsWithUriCompliance() {
		b.withUriCompliance(UriCompliance.UNSAFE);
		RestServer server = b.build(VALID_PACKAGE);
		Connector[] connectors = server.getJettyServer().getConnectors();
		ServerConnector connector = (ServerConnector) connectors[0];
		HttpConnectionFactory h11 = (HttpConnectionFactory) connector.getConnectionFactories().iterator().next();
		assertEquals(UriCompliance.UNSAFE, h11.getHttpConfiguration().getUriCompliance());
	}

	@Test
	void buildsWithoutCors() {
		b.withoutCors();
		RestServer server = b.build(VALID_PACKAGE);
		GzipHandler gzipHandler = (GzipHandler) server.getJettyServer().getHandler();
		Handler handler = (Handler) gzipHandler.getHandler();
		assertFalse(handler.isCors());
	}

	@Test
	void buildsWithoutLogging() {
		b.withoutLogging();
		RestServer server = b.build(VALID_PACKAGE);
		GzipHandler gzipHandler = (GzipHandler) server.getJettyServer().getHandler();
		Handler handler = (Handler) gzipHandler.getHandler();
		assertFalse(handler.isLogging());
	}

	@Test
	void buildsWithHttps() {
		b.withKeyStore("path", "password");
		RestServer server = b.build(VALID_PACKAGE);
		assertEquals("https", server.getScheme());
		assertEquals(8080, server.getClearPort());
		assertEquals(8443, server.getMainPort());
		assertEquals(-1, server.getAltPort());
		Connector[] connectors = server.getJettyServer().getConnectors();
		assertEquals(2, connectors.length);
		ServerConnector connector = (ServerConnector) connectors[0];
		assertEquals(8080, connector.getPort());
		Iterator<ConnectionFactory> iterator = connector.getConnectionFactories().iterator();
		HttpConnectionFactory h11 = (HttpConnectionFactory) iterator.next();
		HttpConfiguration configuration = h11.getHttpConfiguration();
		assertEquals(HttpCompliance.RFC7230, configuration.getHttpCompliance());
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, configuration.getUriCompliance());
		List<Customizer> customizers = configuration.getCustomizers();
		assertEquals(1, customizers.size());
		assertInstanceOf(SecureRequestCustomizer.class, customizers.get(0));
		assertEquals("https", configuration.getSecureScheme());
		assertEquals(8443, configuration.getSecurePort());
		HTTP2CServerConnectionFactory h2c = (HTTP2CServerConnectionFactory) iterator.next();
		assertSame(configuration, h2c.getHttpConfiguration());
		assertFalse(iterator.hasNext());
		connector = (ServerConnector) connectors[1];
		assertEquals(8443, connector.getPort());
		iterator = connector.getConnectionFactories().iterator();
		SslConnectionFactory tls = (SslConnectionFactory) iterator.next();
		assertSame(b.getFactory(), tls.getSslContextFactory());
		assertEquals("alpn", tls.getNextProtocol());
		ALPNServerConnectionFactory alpn = (ALPNServerConnectionFactory) iterator.next();
		assertEquals("HTTP/1.1", alpn.getDefaultProtocol());
		HTTP2ServerConnectionFactory h2 = (HTTP2ServerConnectionFactory) iterator.next();
		assertSame(configuration, h2.getHttpConfiguration());
		assertSame(h11, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	void buildsWithHttps3() {
		b.withKeyStore("path", "password");
		b.withHttp3();
		RestServer server = b.build(VALID_PACKAGE);
		assertEquals("https", server.getScheme());
		assertEquals(8080, server.getClearPort());
		assertEquals(8443, server.getMainPort());
		assertEquals(8843, server.getAltPort());
		Connector[] connectors = server.getJettyServer().getConnectors();
		assertEquals(3, connectors.length);
		ServerConnector connector = (ServerConnector) connectors[0];
		assertEquals(8080, connector.getPort());
		Iterator<ConnectionFactory> iterator = connector.getConnectionFactories().iterator();
		HttpConnectionFactory h11 = (HttpConnectionFactory) iterator.next();
		HttpConfiguration configuration = h11.getHttpConfiguration();
		assertEquals(HttpCompliance.RFC7230, configuration.getHttpCompliance());
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, configuration.getUriCompliance());
		List<Customizer> customizers = configuration.getCustomizers();
		assertEquals(2, customizers.size());
		assertInstanceOf(SecureRequestCustomizer.class, customizers.get(0));
		assertEquals("https", configuration.getSecureScheme());
		assertEquals(8443, configuration.getSecurePort());
		HTTP2CServerConnectionFactory h2c = (HTTP2CServerConnectionFactory) iterator.next();
		assertSame(configuration, h2c.getHttpConfiguration());
		assertFalse(iterator.hasNext());
		connector = (ServerConnector) connectors[1];
		assertEquals(8443, connector.getPort());
		iterator = connector.getConnectionFactories().iterator();
		SslConnectionFactory tls = (SslConnectionFactory) iterator.next();
		assertSame(b.getFactory(), tls.getSslContextFactory());
		assertEquals("alpn", tls.getNextProtocol());
		ALPNServerConnectionFactory alpn = (ALPNServerConnectionFactory) iterator.next();
		assertEquals("HTTP/1.1", alpn.getDefaultProtocol());
		HTTP2ServerConnectionFactory h2 = (HTTP2ServerConnectionFactory) iterator.next();
		assertSame(configuration, h2.getHttpConfiguration());
		assertSame(h11, iterator.next());
		assertFalse(iterator.hasNext());
		HTTP3ServerConnector connector3 = (HTTP3ServerConnector) connectors[2];
		assertEquals(8843, connector3.getPort());
		assertSame(b.getFactory(), connector3.getBean(SslContextFactory.Server.class));
		iterator = connector3.getConnectionFactories().iterator();
		HTTP3ServerConnectionFactory h3 = (HTTP3ServerConnectionFactory) iterator.next();
		assertSame(configuration, h3.getHttpConfiguration());
		assertFalse(iterator.hasNext());
	}

	@Test
	void buildsWithClearPort() {
		b.withClearPort(80);
		RestServer server = b.build(VALID_PACKAGE);
		assertEquals("http", server.getScheme());
		assertEquals(80, server.getClearPort());
		assertEquals(80, server.getMainPort());
		assertEquals(-1, server.getAltPort());
		Connector[] connectors = server.getJettyServer().getConnectors();
		assertEquals(1, connectors.length);
		ServerConnector connector = (ServerConnector) connectors[0];
		assertEquals(80, connector.getPort());
	}

	@Test
	void buildsWithSecurePort() {
		b.withKeyStore("path", "password");
		b.withSecurePort(443);
		RestServer server = b.build(VALID_PACKAGE);
		assertEquals("https", server.getScheme());
		assertEquals(8080, server.getClearPort());
		assertEquals(443, server.getMainPort());
		assertEquals(-1, server.getAltPort());
		Connector[] connectors = server.getJettyServer().getConnectors();
		assertEquals(2, connectors.length);
		ServerConnector connector = (ServerConnector) connectors[0];
		assertEquals(8080, connector.getPort());
		HttpConnectionFactory h11 = (HttpConnectionFactory) connector.getConnectionFactories().iterator().next();
		HttpConfiguration configuration = h11.getHttpConfiguration();
		assertEquals("https", configuration.getSecureScheme());
		assertEquals(443, configuration.getSecurePort());
		connector = (ServerConnector) connectors[1];
		assertEquals(443, connector.getPort());
	}

	@Test
	void buildsWithPort3() {
		b.withKeyStore("path", "password");
		b.withHttp3();
		b.withPort3(843);
		RestServer server = b.build(VALID_PACKAGE);
		assertEquals("https", server.getScheme());
		assertEquals(8080, server.getClearPort());
		assertEquals(8443, server.getMainPort());
		assertEquals(843, server.getAltPort());
		Connector[] connectors = server.getJettyServer().getConnectors();
		assertEquals(3, connectors.length);
		ServerConnector connector = (ServerConnector) connectors[0];
		assertEquals(8080, connector.getPort());
		HttpConnectionFactory h11 = (HttpConnectionFactory) connector.getConnectionFactories().iterator().next();
		HttpConfiguration configuration = h11.getHttpConfiguration();
		assertEquals("https", configuration.getSecureScheme());
		assertEquals(8443, configuration.getSecurePort());
		connector = (ServerConnector) connectors[1];
		assertEquals(8443, connector.getPort());
		HTTP3ServerConnector connector3 = (HTTP3ServerConnector) connectors[2];
		assertEquals(843, connector3.getPort());
	}

	@Test
	void buildsWithoutHttp2() {
		b.withoutHttp2();
		RestServer server = b.build(VALID_PACKAGE);
		assertEquals("http", server.getScheme());
		assertEquals(8080, server.getClearPort());
		assertEquals(8080, server.getMainPort());
		assertEquals(-1, server.getAltPort());
		Connector[] connectors = server.getJettyServer().getConnectors();
		assertEquals(1, connectors.length);
		ServerConnector connector = (ServerConnector) connectors[0];
		assertEquals(8080, connector.getPort());
		Iterator<ConnectionFactory> iterator = connector.getConnectionFactories().iterator();
		HttpConnectionFactory h11 = (HttpConnectionFactory) iterator.next();
		HttpConfiguration configuration = h11.getHttpConfiguration();
		assertEquals(HttpCompliance.RFC7230, configuration.getHttpCompliance());
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, configuration.getUriCompliance());
		assertFalse(iterator.hasNext());
	}

	@Test
	void buildsWithHttpsWithoutHttp2() {
		b.withKeyStore("path", "password");
		b.withoutHttp2();
		RestServer server = b.build(VALID_PACKAGE);
		assertEquals("https", server.getScheme());
		assertEquals(8080, server.getClearPort());
		assertEquals(8443, server.getMainPort());
		assertEquals(-1, server.getAltPort());
		Connector[] connectors = server.getJettyServer().getConnectors();
		assertEquals(2, connectors.length);
		ServerConnector connector = (ServerConnector) connectors[0];
		assertEquals(8080, connector.getPort());
		Iterator<ConnectionFactory> iterator = connector.getConnectionFactories().iterator();
		HttpConnectionFactory h11 = (HttpConnectionFactory) iterator.next();
		HttpConfiguration configuration = h11.getHttpConfiguration();
		assertEquals(HttpCompliance.RFC7230, configuration.getHttpCompliance());
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, configuration.getUriCompliance());
		List<Customizer> customizers = configuration.getCustomizers();
		assertEquals(1, customizers.size());
		assertInstanceOf(SecureRequestCustomizer.class, customizers.get(0));
		assertEquals("https", configuration.getSecureScheme());
		assertEquals(8443, configuration.getSecurePort());
		assertFalse(iterator.hasNext());
		connector = (ServerConnector) connectors[1];
		assertEquals(8443, connector.getPort());
		iterator = connector.getConnectionFactories().iterator();
		SslConnectionFactory tls = (SslConnectionFactory) iterator.next();
		assertSame(b.getFactory(), tls.getSslContextFactory());
		assertEquals("HTTP/1.1", tls.getNextProtocol());
		assertSame(h11, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	void buildsWithHttp3WithoutHttp2() {
		b.withKeyStore("path", "password");
		b.withHttp3();
		b.withoutHttp2();
		RestServer server = b.build(VALID_PACKAGE);
		assertEquals("https", server.getScheme());
		assertEquals(8080, server.getClearPort());
		assertEquals(8443, server.getMainPort());
		assertEquals(8843, server.getAltPort());
		Connector[] connectors = server.getJettyServer().getConnectors();
		assertEquals(3, connectors.length);
		ServerConnector connector = (ServerConnector) connectors[0];
		assertEquals(8080, connector.getPort());
		Iterator<ConnectionFactory> iterator = connector.getConnectionFactories().iterator();
		HttpConnectionFactory h11 = (HttpConnectionFactory) iterator.next();
		HttpConfiguration configuration = h11.getHttpConfiguration();
		assertEquals(HttpCompliance.RFC7230, configuration.getHttpCompliance());
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, configuration.getUriCompliance());
		List<Customizer> customizers = configuration.getCustomizers();
		assertEquals(2, customizers.size());
		assertInstanceOf(SecureRequestCustomizer.class, customizers.get(0));
		assertEquals("https", configuration.getSecureScheme());
		assertEquals(8443, configuration.getSecurePort());
		assertFalse(iterator.hasNext());
		connector = (ServerConnector) connectors[1];
		assertEquals(8443, connector.getPort());
		iterator = connector.getConnectionFactories().iterator();
		SslConnectionFactory tls = (SslConnectionFactory) iterator.next();
		assertSame(b.getFactory(), tls.getSslContextFactory());
		assertEquals("HTTP/1.1", tls.getNextProtocol());
		assertSame(h11, iterator.next());
		assertFalse(iterator.hasNext());
		HTTP3ServerConnector connector3 = (HTTP3ServerConnector) connectors[2];
		assertEquals(8843, connector3.getPort());
		assertSame(b.getFactory(), connector3.getBean(SslContextFactory.Server.class));
		iterator = connector3.getConnectionFactories().iterator();
		HTTP3ServerConnectionFactory h3 = (HTTP3ServerConnectionFactory) iterator.next();
		assertSame(configuration, h3.getHttpConfiguration());
		assertFalse(iterator.hasNext());
	}

	@Test
	void doesNotBuildIfBaseIsNull() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithNull.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build(INVALID_PACKAGE);
			});
		}
	}

	@Test
	void doesNotBuildIfBaseIsBlank() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithBlank.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build(INVALID_PACKAGE);
			});
		}
	}

	@Test
	void doesNotBuildIfBaseDoesNotStartWithSlash() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithoutSlash.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build(INVALID_PACKAGE);
			});
		}
	}

	@Test
	void doesNotBuildIfBaseDoesNotHaveDecoding() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithoutDecoding.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build(INVALID_PACKAGE);
			});
		}
	}

	@Test
	void doesNotBuildIfBaseDoesNotHaveCompliance() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithoutCompliance.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build(INVALID_PACKAGE);
			});
		}
	}

	private MockedConstruction<Reflections> mockReflectionsConstruction(Class<? extends RestResource> type) {
		return mockConstruction(Reflections.class, (mock, context) -> {
			when(mock.getSubTypesOf(RestResource.class)).thenReturn(Set.of(type));
		});
	}
}
