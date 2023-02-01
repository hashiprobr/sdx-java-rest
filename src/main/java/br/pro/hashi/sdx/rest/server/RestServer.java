package br.pro.hashi.sdx.rest.server;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.pro.hashi.sdx.rest.server.exception.ServerException;

/**
 * Main object for receiving REST requests.
 */
public final class RestServer {
	/**
	 * Instantiates a default REST server using the resources of a specified
	 * package.
	 * 
	 * @param packageName the package name
	 * @return the server
	 */
	public static RestServer from(String packageName) {
		return builder().build(packageName);
	}

	/**
	 * Convenience method that instantiates a REST server builder.
	 * 
	 * @return the server builder
	 */
	public static RestServerBuilder builder() {
		return new RestServerBuilder();
	}

	private final Logger logger;
	private final Server jettyServer;

	RestServer(Server jettyServer) {
		this.logger = LoggerFactory.getLogger(RestServer.class);
		this.jettyServer = jettyServer;
	}

	/**
	 * <p>
	 * Obtains the Jetty Server used internally by this server.
	 * </p>
	 * <p>
	 * Call this method if you want to override the default configuration.
	 * </p>
	 * 
	 * @return the internal server
	 */
	public Server getJettyServer() {
		return jettyServer;
	}

	/**
	 * Starts this server.
	 * 
	 * @throws ServerException if the Jetty Server cannot be started
	 */
	public void start() {
		if (jettyServer.isRunning()) {
			return;
		}
		logger.info("Starting REST server...");
		try {
			jettyServer.start();
		} catch (Exception exception) {
			throw new ServerException(exception);
		}
		logger.info("REST server started");
	}

	/**
	 * Stops this server.
	 * 
	 * @throws ServerException if the Jetty Server cannot be stopped
	 */
	public void stop() {
		if (!jettyServer.isRunning()) {
			return;
		}
		logger.info("Stopping REST server...");
		try {
			jettyServer.stop();
		} catch (Exception exception) {
			throw new ServerException(exception);
		}
		logger.info("REST server stopped");
	}
}
