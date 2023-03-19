package br.pro.hashi.sdx.rest.server;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.coding.Percent;
import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.reflection.Reflection;
import br.pro.hashi.sdx.rest.server.exception.ResourceException;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import br.pro.hashi.sdx.rest.transform.facade.Facade;
import jakarta.servlet.MultipartConfigElement;

/**
 * Configures and builds objects of type {@link RestServer}.
 */
public non-sealed class RestServerBuilder extends Builder<RestServerBuilder> {
	private final Logger logger;
	private final Set<Class<? extends RuntimeException>> gatewayTypes;
	private ErrorFormatter formatter;
	private SslContextFactory.Server factory;
	private MultipartConfigElement element;
	private UriCompliance compliance;
	private int clearPort;
	private int securePort;
	private int port3;
	private boolean http3;
	private boolean http2;
	private boolean cors;
	private boolean logging;

	/**
	 * Constructs a new builder.
	 */
	public RestServerBuilder() {
		this.logger = LoggerFactory.getLogger(RestServerBuilder.class);
		this.gatewayTypes = new HashSet<>();
		this.formatter = new ConcreteFormatter();
		this.factory = null;
		this.element = new MultipartConfigElement("");
		this.compliance = UriCompliance.RFC3986_UNAMBIGUOUS;
		this.clearPort = 8080;
		this.securePort = 8443;
		this.port3 = 8843;
		this.http3 = false;
		this.http2 = true;
		this.cors = true;
		this.logging = true;
	}

	Cache getCache() {
		return cache;
	}

	Facade getFacade() {
		return facade;
	}

	Set<Class<? extends RuntimeException>> getGatewayTypes() {
		return gatewayTypes;
	}

	ErrorFormatter getFormatter() {
		return formatter;
	}

	SslContextFactory.Server getFactory() {
		return factory;
	}

	MultipartConfigElement getElement() {
		return element;
	}

	UriCompliance getCompliance() {
		return compliance;
	}

	int getClearPort() {
		return clearPort;
	}

	int getSecurePort() {
		return securePort;
	}

	int getPort3() {
		return port3;
	}

	boolean isHttp3() {
		return http3;
	}

	boolean isHttp2() {
		return http2;
	}

	boolean isCors() {
		return cors;
	}

	boolean isLogging() {
		return logging;
	}

	/**
	 * Adds an exception type that should be considered gateway.
	 * 
	 * @param type the exception type
	 * @return this builder, for chaining
	 * @throws NullPointerException if the type is null
	 */
	public final RestServerBuilder withGateway(Class<? extends RuntimeException> type) {
		if (type == null) {
			throw new NullPointerException("Exception type cannot be null");
		}
		this.gatewayTypes.add(type);
		return self();
	}

	/**
	 * Sets a formatter that should be used for error messages.
	 * 
	 * @param formatter the error formatter
	 * @return this builder, for chaining
	 * @throws NullPointerException if the formatter is null
	 */
	public final RestServerBuilder withErrorFormatter(ErrorFormatter formatter) {
		if (formatter == null) {
			throw new NullPointerException("Error formatter cannot be null");
		}
		this.formatter = formatter;
		return self();
	}

	/**
	 * Sets a keytool keystore that should be used to enable HTTPS support.
	 * 
	 * @param path     the KeyStore path
	 * @param password the KeyStore password
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the path is null or the password is null
	 * @throws IllegalArgumentException if the path is empty or the password is
	 *                                  empty
	 */
	public final RestServerBuilder withKeyStore(String path, String password) {
		if (path == null) {
			throw new NullPointerException("KeyStore path cannot be null");
		}
		if (path.isEmpty()) {
			throw new IllegalArgumentException("KeyStore path cannot be empty");
		}
		if (password == null) {
			throw new NullPointerException("KeyStore password cannot be null");
		}
		if (password.isEmpty()) {
			throw new IllegalArgumentException("KeyStore password cannot be empty");
		}
		this.factory = new SslContextFactory.Server();
		this.factory.setKeyStorePath(path);
		this.factory.setKeyStorePassword(password);
		return self();
	}

	/**
	 * Sets a configuration that should be used for multipart requests.
	 * 
	 * @param element the multipart configuration
	 * @return this builder, for chaining
	 * @throws NullPointerException if the configuration is null
	 */
	public final RestServerBuilder withMultipartConfig(MultipartConfigElement element) {
		if (element == null) {
			throw new NullPointerException("Multipart configuration cannot be null");
		}
		this.element = element;
		return self();
	}

	/**
	 * Sets the URI compliance for endpoint bases.
	 * 
	 * @param compliance the URI compliance
	 * @return this builder, for chaining
	 * @throws NullPointerException if the compliance is null
	 */
	public final RestServerBuilder withUriCompliance(UriCompliance compliance) {
		if (compliance == null) {
			throw new NullPointerException("URI compliance cannot be null");
		}
		this.compliance = compliance;
		return self();
	}

	/**
	 * Sets the port for clear-text HTTP/1.1 and HTTP/2.
	 * 
	 * @param clearPort the port
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withClearPort(int clearPort) {
		this.clearPort = clearPort;
		return self();
	}

	/**
	 * Sets the port for encrypted HTTP/1.1 and HTTP/2.
	 * 
	 * @param securePort the port
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withSecurePort(int securePort) {
		this.securePort = securePort;
		return self();
	}

	/**
	 * Sets the port for encrypted HTTP/3.
	 * 
	 * @param port3 the port
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withPort3(int port3) {
		this.port3 = port3;
		return self();
	}

	/**
	 * Enables HTTP/3 support.
	 * 
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withHttp3() {
		this.http3 = true;
		return self();
	}

	/**
	 * Disables HTTP/2 support.
	 * 
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withoutHttp2() {
		this.http2 = false;
		return self();
	}

	/**
	 * Disables CORS.
	 * 
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withoutCors() {
		this.cors = false;
		return self();
	}

	/**
	 * Disables logging.
	 * 
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withoutLogging() {
		this.logging = false;
		return self();
	}

	/**
	 * Builds a server using the resources of a specified package.
	 * 
	 * @param packageName the package name
	 * @return the REST server
	 */
	public final RestServer build(String packageName) {
		Map<Class<? extends RestResource>, Constructor<? extends RestResource>> constructors = new HashMap<>();
		Map<Class<? extends RestResource>, String[]> itemMap = new HashMap<>();
		for (Class<? extends RestResource> type : Reflection.getConcreteSubTypes(packageName, RestResource.class)) {
			String typeName = type.getName();
			Constructor<? extends RestResource> constructor = Reflection.getNoArgsConstructor(type, typeName);
			String[] items = getItems(constructor, typeName);
			constructors.put(type, constructor);
			itemMap.put(type, items);
			logger.info("Constructed %s".formatted(typeName));
		}

		Tree tree = new Tree(cache, locale);
		for (Class<? extends RestResource> type : itemMap.keySet()) {
			String typeName = type.getName();
			tree.putNodesAndEndpoints(type, typeName, itemMap);
			logger.info("Registered %s".formatted(typeName));
		}

		Server server = new Server();

		AbstractHandler handler = new Handler(cache, facade, tree, formatter, constructors, element, gatewayTypes, urlCharset, cors, logging);
		if (compression) {
			GzipHandler gzipHandler = new GzipHandler();
			gzipHandler.setHandler(handler);
			handler = gzipHandler;
		}
		if (redirection) {
			SecuredRedirectHandler redirectHandler = new SecuredRedirectHandler();
			redirectHandler.setHandler(handler);
			handler = redirectHandler;
		}
		server.setHandler(handler);

		ConcreteHandler errorHandler = new ConcreteHandler();
		server.setErrorHandler(errorHandler);

		String scheme;
		int mainPort;
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setHttpCompliance(HttpCompliance.RFC7230);
		configuration.setUriCompliance(compliance);
		if (factory == null) {
			scheme = "http";
			mainPort = clearPort;
		} else {
			scheme = "https";
			mainPort = securePort;
			configuration.addCustomizer(new SecureRequestCustomizer());
			configuration.setSecureScheme(scheme);
			configuration.setSecurePort(mainPort);
		}

		HttpConnectionFactory h11 = new HttpConnectionFactory(configuration);
		ServerConnector connector;
		if (http2) {
			HTTP2CServerConnectionFactory h2c = new HTTP2CServerConnectionFactory(configuration);
			connector = new ServerConnector(server, h11, h2c);
		} else {
			connector = new ServerConnector(server, h11);
		}
		connector.setPort(clearPort);
		server.addConnector(connector);

		int altPort;
		if (factory == null) {
			altPort = -1;
		} else {
			SslConnectionFactory tls;
			if (http2) {
				HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(configuration);
				ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
				alpn.setDefaultProtocol(h11.getProtocol());
				tls = new SslConnectionFactory(factory, alpn.getProtocol());
				connector = new ServerConnector(server, tls, alpn, h2, h11);
			} else {
				tls = new SslConnectionFactory(factory, h11.getProtocol());
				connector = new ServerConnector(server, tls, h11);
			}
			connector.setPort(securePort);
			server.addConnector(connector);
			if (http3) {
				HTTP3ServerConnectionFactory h3 = new HTTP3ServerConnectionFactory(configuration);
				HTTP3ServerConnector connector3 = new HTTP3ServerConnector(server, factory, h3);
				connector3.setPort(port3);
				server.addConnector(connector3);
				altPort = port3;
			} else {
				altPort = -1;
			}
		}

		return new RestServer(server, scheme, clearPort, mainPort, altPort);
	}

	private String[] getItems(Constructor<? extends RestResource> constructor, String typeName) {
		RestResource resource = Reflection.newNoArgsInstance(constructor);
		String base = resource.getBase();
		if (base == null) {
			throw new ResourceException(typeName, "Base cannot be null");
		}
		base = base.strip();
		if (base.isEmpty()) {
			throw new ResourceException(typeName, "Base cannot be blank");
		}
		if (!base.startsWith("/")) {
			throw new ResourceException(typeName, "Base must start with /");
		}
		base = Percent.stripEndingSlashes(base);
		String urlSuffix;
		try {
			urlSuffix = Percent.recode(base, urlCharset);
		} catch (IllegalArgumentException exception) {
			throw new ResourceException(typeName, exception.getMessage());
		}
		HttpURI uri = HttpURI.from("http://a%s".formatted(urlSuffix));
		String message = UriCompliance.checkUriCompliance(compliance, uri);
		if (message != null) {
			throw new ResourceException(typeName, message);
		}
		return Percent.splitAndDecode(base, urlCharset);
	}

	/**
	 * @hidden
	 */
	@Override
	protected final RestServerBuilder self() {
		return this;
	}
}
