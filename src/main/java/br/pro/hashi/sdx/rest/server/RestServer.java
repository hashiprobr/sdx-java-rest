package br.pro.hashi.sdx.rest.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;

import br.pro.hashi.sdx.rest.server.exception.ServerException;

/**
 * Receives REST requests.
 */
public final class RestServer {
	/**
	 * Builds a server with a default configuration from the resources in the
	 * specified package.
	 * 
	 * @param packageName the package name
	 * @return the server
	 * @throws NullPointerException if the package name is null
	 */
	public static RestServer from(String packageName) {
		RestServerBuilder builder = new RestServerBuilder();
		return builder.build(packageName);
	}

	private final Logger logger;
	private final Server jettyServer;
	private final String scheme;
	private final int clearPort;
	private final int mainPort;
	private final int altPort;
	private String publicAddress;
	private String publicUrl;
	private String publicUrl3;
	private String privateAddress;
	private String privateUrl;
	private String privateUrl3;

	RestServer(Server jettyServer, String scheme, int clearPort, int mainPort, int altPort) {
		this.logger = LoggerFactory.getLogger(RestServer.class);
		this.jettyServer = jettyServer;
		this.scheme = scheme;
		this.clearPort = clearPort;
		this.mainPort = mainPort;
		this.altPort = altPort;
		this.publicAddress = null;
		this.publicUrl = null;
		this.publicUrl3 = null;
		this.privateAddress = null;
		this.privateUrl = null;
		this.privateUrl3 = null;
	}

	String getScheme() {
		return scheme;
	}

	int getClearPort() {
		return clearPort;
	}

	int getMainPort() {
		return mainPort;
	}

	int getAltPort() {
		return altPort;
	}

	/**
	 * <p>
	 * Obtains the Jetty Server used internally by this server.
	 * </p>
	 * <p>
	 * Call this method if you want to override the builder configuration.
	 * </p>
	 * 
	 * @return the internal server
	 */
	public Server getJettyServer() {
		return jettyServer;
	}

	/**
	 * Obtains the public IP of this server or {@code null} if the address is not
	 * available.
	 * 
	 * @return the address
	 */
	public String getPublicAddress() {
		return publicAddress;
	}

	/**
	 * Obtains the public URL of this server or {@code null} if the address is not
	 * available.
	 * 
	 * @return the URL
	 */
	public String getPublicUrl() {
		return publicUrl;
	}

	/**
	 * Obtains the public HTTP/3 URL of this server or {@code null} if the address
	 * is not available.
	 * 
	 * @return the URL
	 */
	public String getPublicUrl3() {
		return publicUrl3;
	}

	/**
	 * Obtains the local IP of this server for private usage.
	 * 
	 * @return the address
	 */
	public String getPrivateAddress() {
		return privateAddress;
	}

	/**
	 * Obtains the local URL of this server for private usage.
	 * 
	 * @return the URL
	 */
	public String getPrivateUrl() {
		return privateUrl;
	}

	/**
	 * Obtains the local HTTP/3 URL of this server for private usage or {@code null}
	 * if the address is not available.
	 * 
	 * @return the URL
	 */
	public String getPrivateUrl3() {
		return privateUrl3;
	}

	/**
	 * Starts this server.
	 * 
	 * @throws ServerException if the internal client cannot be started
	 */
	public void start() {
		start(false);
	}

	/**
	 * Starts this server behind a ngrok proxy.
	 * 
	 * @throws ServerException if the internal client cannot be started
	 */
	public void startWithTunnel() {
		start(true);
	}

	private void start(boolean useTunnel) {
		if (jettyServer.isRunning()) {
			return;
		}
		logger.info("Starting REST server...");
		try {
			jettyServer.start();
		} catch (Exception exception) {
			throw new ServerException(exception);
		}
		InetAddress inet;
		String authority;
		if (useTunnel) {
			int index = scheme.length() + 3;
			NgrokClient client = new NgrokClient.Builder()
					.build();
			CreateTunnel create = new CreateTunnel.Builder()
					.withBindTls(index == 8)
					.withAddr(clearPort)
					.build();
			String tunnelUrl = client.connect(create).getPublicUrl();
			authority = tunnelUrl.substring(index);
			try {
				inet = InetAddress.getByName(authority);
				publicAddress = inet.getHostAddress();
			} catch (UnknownHostException warning) {
				logger.warn("Could not get IP of %s".formatted(authority), warning);
			}
			publicUrl = tunnelUrl;
		} else {
			try {
				inet = InetAddress.getLocalHost();
				authority = inet.getHostName();
				publicAddress = inet.getHostAddress();
				publicUrl = formatUrl(authority, mainPort);
				if (altPort != -1) {
					publicUrl3 = formatUrl(authority, altPort);
				}
			} catch (UnknownHostException warning) {
				logger.warn("Could not get local address", warning);
			}
		}
		inet = InetAddress.getLoopbackAddress();
		authority = inet.getHostName();
		privateAddress = inet.getHostAddress();
		privateUrl = formatUrl(authority, mainPort);
		if (altPort != -1) {
			privateUrl3 = formatUrl(authority, altPort);
		}
		logger.info("REST server started");
	}

	private String formatUrl(String authority, int port) {
		if (scheme.endsWith("s")) {
			if (port == 443) {
				return "https://%s".formatted(authority);
			}
		} else {
			if (port == 80) {
				return "http://%s".formatted(authority);
			}
		}
		return "%s://%s:%d".formatted(scheme, authority, port);
	}

	/**
	 * Stops this server.
	 * 
	 * @throws ServerException if the internal client cannot be stopped
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
		publicAddress = null;
		publicUrl = null;
		publicUrl3 = null;
		privateAddress = null;
		privateUrl = null;
		privateUrl3 = null;
		logger.info("REST server stopped");
	}
}
