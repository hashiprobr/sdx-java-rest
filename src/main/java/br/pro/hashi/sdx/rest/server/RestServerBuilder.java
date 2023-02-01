package br.pro.hashi.sdx.rest.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import br.pro.hashi.sdx.rest.Builder;

/**
 * Configures and builds objects of type {@link RestServer}.
 */
public non-sealed class RestServerBuilder extends Builder<RestServerBuilder> {
	private SslContextFactory.Server factory;

	/**
	 * Constructs a new builder.
	 */
	public RestServerBuilder() {
		this.factory = null;
	}

	SslContextFactory.Server getFactory() {
		return factory;
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
	 * Builds a server using the resources of a specified package.
	 * 
	 * @param packageName the package name
	 * @return the REST server
	 */
	public final RestServer build(String packageName) {
		Server server = new Server();
		return new RestServer(server);
	}

	/**
	 * @hidden
	 */
	@Override
	protected final RestServerBuilder self() {
		return this;
	}
}
