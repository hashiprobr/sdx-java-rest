package br.pro.hashi.sdx.rest.server;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.coding.Percent;
import br.pro.hashi.sdx.rest.reflection.Reflection;
import br.pro.hashi.sdx.rest.server.exception.ResourceException;
import br.pro.hashi.sdx.rest.server.tree.Tree;

/**
 * Configures and builds objects of type {@link RestServer}.
 */
public non-sealed class RestServerBuilder extends Builder<RestServerBuilder> {
	private final Logger logger;
	private SslContextFactory.Server factory;
	private UriCompliance uriCompliance;

	/**
	 * Constructs a new builder.
	 */
	public RestServerBuilder() {
		this.logger = LoggerFactory.getLogger(RestServerBuilder.class);
		this.factory = null;
		this.uriCompliance = UriCompliance.RFC3986_UNAMBIGUOUS;
	}

	SslContextFactory.Server getFactory() {
		return factory;
	}

	UriCompliance getUriCompliance() {
		return uriCompliance;
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
	 * Sets the URI compliance for endpoint bases.
	 * 
	 * @param uriCompliance the URI compliance
	 * @return this builder, for chaining
	 * @throws NullPointerException if the compliance is null
	 */
	public final RestServerBuilder withUriCompliance(UriCompliance uriCompliance) {
		if (uriCompliance == null) {
			throw new NullPointerException("URI compliance cannot be null");
		}
		this.uriCompliance = uriCompliance;
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
		RestHandler handler = new RestHandler(constructors, tree);
		Server server = new Server();
		server.setHandler(handler);
		return new RestServer(server);
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
		String message = UriCompliance.checkUriCompliance(uriCompliance, uri);
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
