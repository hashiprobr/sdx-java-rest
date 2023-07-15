package br.pro.hashi.sdx.rest.server;

import java.lang.invoke.MethodHandle;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.jetty.server.MultiPartFormDataCompliance;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.server.handler.ThreadLimitHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.constant.Defaults;
import br.pro.hashi.sdx.rest.reflection.Reflector;
import br.pro.hashi.sdx.rest.server.exception.ResourceException;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import jakarta.servlet.MultipartConfigElement;

/**
 * Builds REST servers.
 */
public non-sealed class RestServerBuilder extends Builder<RestServerBuilder> {
	private static final Pattern BASE_PATTERN = Pattern.compile("\\p{javaLowerCase}\\p{javaUpperCase}");

	private final Logger logger;
	private final Reflector reflector;
	private final MediaCoder mediaCoder;
	private final Set<Class<? extends RuntimeException>> gatewayTypes;
	private ErrorFormatter formatter;
	private String contentType;
	private Charset charset;
	private boolean base64;
	private SslContextFactory.Server factory;
	private ThreadPool requestPool;
	private MultipartConfigElement element;
	private UriCompliance compliance;
	private long maxBodySize;
	private int clearPort;
	private int securePort;
	private int port3;
	private boolean http3;
	private boolean http2;
	private boolean http1;
	private boolean cors;

	/**
	 * Constructs a new builder.
	 */
	public RestServerBuilder() {
		this.logger = LoggerFactory.getLogger(RestServerBuilder.class);
		this.reflector = Reflector.getInstance();
		this.mediaCoder = MediaCoder.getInstance();
		this.gatewayTypes = new HashSet<>();
		this.formatter = new ConcreteFormatter();
		this.contentType = null;
		this.charset = Defaults.CHARSET;
		this.base64 = false;
		this.factory = null;
		this.requestPool = null;
		this.element = new MultipartConfigElement("", 0, 2000000, 200000);
		this.compliance = UriCompliance.RFC3986_UNAMBIGUOUS;
		this.maxBodySize = 200000;
		this.clearPort = 8080;
		this.securePort = 8443;
		this.port3 = 8843;
		this.http3 = false;
		this.http2 = true;
		this.http1 = true;
		this.cors = true;
	}

	Set<Class<? extends RuntimeException>> getGatewayTypes() {
		return gatewayTypes;
	}

	ErrorFormatter getFormatter() {
		return formatter;
	}

	String getContentType() {
		return contentType;
	}

	Charset getCharset() {
		return charset;
	}

	boolean isBase64() {
		return base64;
	}

	SslContextFactory.Server getFactory() {
		return factory;
	}

	ThreadPool getRequestPool() {
		return requestPool;
	}

	MultipartConfigElement getElement() {
		return element;
	}

	UriCompliance getCompliance() {
		return compliance;
	}

	long getMaxBodySize() {
		return maxBodySize;
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

	boolean isHttp1() {
		return http1;
	}

	boolean isCors() {
		return cors;
	}

	/**
	 * <p>
	 * Associates an extension to a content type.
	 * </p>
	 * <p>
	 * The only extension associated to a content type by default is {@code txt}.
	 * </p>
	 * 
	 * @param extension   the extension
	 * @param contentType the content type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the extension is null or the content type
	 *                                  is null
	 * @throws IllegalArgumentException if the extension is blank or the content
	 *                                  type is invalid
	 */
	public final RestServerBuilder withExtension(String extension, String contentType) {
		managerBase.putExtensionType(extension, contentType);
		return self();
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
	 * @throws NullPointerException     if the formatter is null
	 * @throws IllegalArgumentException if the formatter is binary
	 */
	public final RestServerBuilder withErrorFormatter(ErrorFormatter formatter) {
		if (formatter == null) {
			throw new NullPointerException("Error formatter cannot be null");
		}
		if (managerBase.isBinary(formatter.getReturnType())) {
			throw new IllegalArgumentException("Error formatter cannot be binary");
		}
		this.formatter = formatter;
		return self();
	}

	/**
	 * Sets the content type for error bodies. Parameters are ignored.
	 * 
	 * @param contentType the content type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final RestServerBuilder withErrorContentType(String contentType) {
		if (contentType == null) {
			throw new NullPointerException("Content type cannot be null");
		}
		contentType = mediaCoder.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Content type cannot be blank");
		}
		this.contentType = contentType;
		return self();
	}

	/**
	 * Sets the charset for error bodies.
	 * 
	 * @param charset the charset
	 * @return this builder, for chaining
	 * @throws NullPointerException if the charset is null
	 */
	public final RestServerBuilder withErrorCharset(Charset charset) {
		if (charset == null) {
			throw new NullPointerException("Charset cannot be null");
		}
		this.charset = charset;
		return self();
	}

	/**
	 * Encodes the error bodies in Base64 by default.
	 * 
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withErrorInBase64() {
		this.base64 = true;
		return self();
	}

	/**
	 * Sets the keytool KeyStore that should be used for HTTPS support.
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
		SslContextFactory.Server factory = new SslContextFactory.Server();
		factory.setKeyStorePath(path);
		factory.setKeyStorePassword(password);
		this.factory = factory;
		return self();
	}

	/**
	 * Sets a primary thread pool that should be used for requests.
	 * 
	 * @param requestPool the thread pool
	 * @return this builder, for chaining
	 * @throws NullPointerException if the pool is null
	 */
	public final RestServerBuilder withRequestPool(ThreadPool requestPool) {
		if (requestPool == null) {
			throw new NullPointerException("Request pool cannot be null");
		}
		this.requestPool = requestPool;
		return self();
	}

	/**
	 * Sets a configuration that should be used for multipart requests.
	 * 
	 * @param location          the directory location where files will be stored
	 * @param maxFileSize       the maximum size allowed for uploaded files
	 * @param maxRequestSize    the maximum size allowed for
	 *                          {@code multipart/form-data} requests
	 * @param fileSizeThreshold the size threshold after which files will be written
	 *                          to disk
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withMultipartConfig(String location, long maxFileSize, long maxRequestSize, int fileSizeThreshold) {
		this.element = new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
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
	 * Sets the maximum body size allowed in non-multipart requests. Default is
	 * {@code 200000}.
	 * 
	 * @param maxBodySize the limit
	 * @return this builder, for chaining
	 */
	public final RestServerBuilder withMaxBodySize(long maxBodySize) {
		this.maxBodySize = maxBodySize;
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
	 * @throws IllegalArgumentException if HTTP/1.1 is also disabled
	 */
	public final RestServerBuilder withoutHttp2() {
		if (!http1) {
			throw new IllegalArgumentException("Either HTTP/1.1 or HTTP/2 needs to be enabled");
		}
		this.http2 = false;
		return self();
	}

	/**
	 * Disables HTTP/1.1 support.
	 * 
	 * @return this builder, for chaining
	 * @throws IllegalArgumentException if HTTP/2 is also disabled
	 */
	public final RestServerBuilder withoutHttp1() {
		if (!http2) {
			throw new IllegalArgumentException("Either HTTP/2 or HTTP/1.1 needs to be enabled");
		}
		this.http1 = false;
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
	 * Builds a server with the current configuration from the resources in the
	 * specified package.
	 * 
	 * @param packageName the package name
	 * @return the server
	 */
	public final RestServer build(String packageName) {
		Map<Class<? extends RestResource>, MethodHandle> handles = new HashMap<>();
		Map<Class<? extends RestResource>, String[]> itemMap = new HashMap<>();
		for (Class<? extends RestResource> type : reflector.getConcreteSubTypes(packageName, RestResource.class)) {
			String typeName = type.getName();
			MethodHandle handle = reflector.getCreator(type, typeName);
			String[] items = getItems(handle, typeName);
			handles.put(type, handle);
			itemMap.put(type, items);
			logger.info("Constructed %s".formatted(typeName));
		}

		Tree tree = Tree.newInstance(locale, maxBodySize);
		for (Class<? extends RestResource> type : itemMap.keySet()) {
			String typeName = type.getName();
			tree.putNodesAndEndpoints(type, typeName, itemMap);
			logger.info("Registered %s".formatted(typeName));
		}

		Server server;
		if (requestPool == null) {
			server = new Server();
		} else {
			server = new Server(requestPool);
		}

		ConcreteHandler errorHandler = new ConcreteHandler(managerBase, formatter, contentType, charset, base64);
		server.setErrorHandler(errorHandler);

		AbstractHandler handler = new Handler(managerBase, tree, formatter, handles, element, gatewayTypes, urlCharset, cors);
		if (compression) {
			GzipHandler gzipHandler = new GzipHandler();
			gzipHandler.setHandler(handler);
			handler = gzipHandler;
		}
		if (redirection && factory != null) {
			SecuredRedirectHandler redirectHandler = new SecuredRedirectHandler();
			redirectHandler.setHandler(handler);
			handler = redirectHandler;
		}
		ThreadLimitHandler limitHandler = new ThreadLimitHandler();
		limitHandler.setHandler(handler);
		server.setHandler(limitHandler);

		String scheme;
		int mainPort;
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setSendDateHeader(false);
		configuration.setSendServerVersion(false);
		configuration.setSendXPoweredBy(false);
		configuration.setHttpCompliance(HttpCompliance.RFC7230);
		configuration.setMultiPartFormDataCompliance(MultiPartFormDataCompliance.RFC7578);
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

		HttpConnectionFactory h11;
		if (http1) {
			h11 = new HttpConnectionFactory(configuration);
		} else {
			h11 = null;
		}

		ServerConnector connector;
		if (http2) {
			HTTP2CServerConnectionFactory h2c = new HTTP2CServerConnectionFactory(configuration);
			if (h11 == null) {
				connector = new ServerConnector(server, h2c);
			} else {
				connector = new ServerConnector(server, h11, h2c);
			}
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
				if (h11 == null) {
					tls = new SslConnectionFactory(factory, h2.getProtocol());
					connector = new ServerConnector(server, tls, h2);
				} else {
					ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
					alpn.setDefaultProtocol(h11.getProtocol());
					tls = new SslConnectionFactory(factory, alpn.getProtocol());
					connector = new ServerConnector(server, tls, alpn, h2, h11);
				}
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

	private String[] getItems(MethodHandle handle, String typeName) {
		RestResource resource = reflector.invokeCreator(handle);
		String base = resource.getBase();
		if (base == null) {
			if (!resource.isNullBase()) {
				throw new ResourceException(typeName, "Base cannot be null");
			}
			StringJoiner joiner = new StringJoiner("-", "/", "");
			int start = typeName.lastIndexOf('.') + 1;
			Matcher matcher = BASE_PATTERN.matcher(typeName);
			while (matcher.find(start)) {
				int end = matcher.end() - 1;
				joiner.add(typeName.substring(start, end).toLowerCase(locale));
				start = end;
			}
			joiner.add(typeName.substring(start).toLowerCase(locale));
			base = joiner.toString();
		} else {
			base = base.strip();
			if (base.isEmpty()) {
				throw new ResourceException(typeName, "Base cannot be blank");
			}
			if (!base.startsWith("/")) {
				throw new ResourceException(typeName, "Base must start with /");
			}
		}
		base = pathCoder.stripEndingSlashes(base);
		String urlSuffix;
		try {
			urlSuffix = pathCoder.recode(base, urlCharset);
		} catch (IllegalArgumentException error) {
			String message = "Base could not be decoded";
			logger.error(message, error);
			throw new ResourceException(typeName, message);
		}
		HttpURI uri = HttpURI.from("http://a%s".formatted(urlSuffix));
		String message = UriCompliance.checkUriCompliance(compliance, uri);
		if (message != null) {
			throw new ResourceException(typeName, message);
		}
		return pathCoder.splitAndDecode(base, urlCharset);
	}

	/**
	 * Internal member.
	 * 
	 * @return this builder, for chaining
	 * @hidden
	 */
	@Override
	protected final RestServerBuilder self() {
		return this;
	}
}
