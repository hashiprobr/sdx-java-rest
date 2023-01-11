package br.pro.hashi.sdx.rest.client;

import org.eclipse.jetty.client.GZIPContentDecoder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.http.HttpClientConnectionFactory;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.ClientConnectionFactoryOverHTTP2;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.http3.client.HTTP3Client;
import org.eclipse.jetty.http3.client.http.HttpClientTransportOverHTTP3;
import org.eclipse.jetty.io.ClientConnectionFactory;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.coding.Percent;

/**
 * Configures and builds objects of type {@link RestClient}.
 */
public non-sealed class RestClientBuilder extends Builder<RestClientBuilder> {
	private SslContextFactory.Client factory;

	/**
	 * Constructs a new builder.
	 */
	public RestClientBuilder() {
		this.factory = null;
	}

	private String encode(String urlPrefix) {
		if (urlPrefix == null) {
			throw new IllegalArgumentException("URL prefix cannot be null");
		}
		urlPrefix = urlPrefix.strip();
		String schema = "http://";
		if (!urlPrefix.startsWith(schema)) {
			schema = "https://";
			if (!urlPrefix.startsWith(schema)) {
				throw new IllegalArgumentException("URL prefix must start with http:// or https://");
			}
		}
		String path = urlPrefix.substring(schema.length());
		if (path.isEmpty()) {
			throw new IllegalArgumentException("URL prefix path cannot be blank");
		}
		path = Percent.stripEndingSlashes(path);
		int index = path.indexOf("/");
		if (index != -1) {
			if (index == 0) {
				throw new IllegalArgumentException("URL prefix authority cannot be empty");
			}
			String[] items = Percent.splitAndEncode(path.substring(index + 1), urlCharset);
			path = "%s/%s".formatted(path.substring(0, index), String.join("/", items));
		}
		return "%s%s".formatted(schema, path);
	}

	private RestClient build(HttpClient client, String urlPrefix) {
		client.setCookieStore(new HttpCookieStore.Empty());
		if (compression) {
			client.getContentDecoderFactories().add(new GZIPContentDecoder.Factory());
		}
		client.setFollowRedirects(redirection);
		return new RestClient(facade, client, urlCharset, none, urlPrefix);
	}

	SslContextFactory.Client getFactory() {
		return factory;
	}

	/**
	 * @hidden
	 */
	@Override
	protected RestClientBuilder self() {
		return this;
	}

	/**
	 * Sets a keytool truststore that should be used to enable HTTPS support.
	 * 
	 * @param path     the TrustStore path.
	 * @param password the TrustStore password.
	 * @return this builder, for chaining.
	 */
	public RestClientBuilder withTrustStore(String path, String password) {
		if (path == null) {
			throw new IllegalArgumentException("TrustStore path cannot be null");
		}
		if (path.isEmpty()) {
			throw new IllegalArgumentException("TrustStore path cannot be empty");
		}
		if (password == null) {
			throw new IllegalArgumentException("TrustStore password cannot be null");
		}
		if (password.isEmpty()) {
			throw new IllegalArgumentException("TrustStore password cannot be empty");
		}
		this.factory = new SslContextFactory.Client();
		this.factory.setTrustStorePath(path);
		this.factory.setTrustStorePassword(password);
		return this;
	}

	/**
	 * <p>
	 * Builds a dynamic HTTP/2 and HTTP/1.1 {@link RestClient} with the current
	 * configuration.
	 * </p>
	 * <p>
	 * This method receives an URL prefix that should be used in all requests.
	 * </p>
	 * 
	 * @param urlPrefix the URL prefix.
	 * @return the {@link RestClient}.
	 */
	public RestClient build(String urlPrefix) {
		urlPrefix = encode(urlPrefix);
		ClientConnector connector = new ClientConnector();
		if (factory != null) {
			connector.setSslContextFactory(factory);
		}
		HTTP2Client client2 = new HTTP2Client(connector);
		ClientConnectionFactoryOverHTTP2.HTTP2 http2 = new ClientConnectionFactoryOverHTTP2.HTTP2(client2);
		ClientConnectionFactory.Info http1 = HttpClientConnectionFactory.HTTP11;
		HttpClientTransport transport = new HttpClientTransportDynamic(connector, http2, http1);
		HttpClient client = new HttpClient(transport);
		return build(client, urlPrefix);
	}

	/**
	 * <p>
	 * Builds a static HTTP/1.1 {@link RestClient} with the current configuration.
	 * </p>
	 * <p>
	 * This method receives an URL prefix that should be used in all requests.
	 * </p>
	 * 
	 * @param urlPrefix the URL prefix.
	 * @return the {@link RestClient}.
	 */
	public RestClient build1(String urlPrefix) {
		urlPrefix = encode(urlPrefix);
		HttpClient client1;
		if (factory == null) {
			client1 = new HttpClient();
		} else {
			ClientConnector connector = new ClientConnector();
			connector.setSslContextFactory(factory);
			HttpClientTransport transport = new HttpClientTransportOverHTTP(connector);
			client1 = new HttpClient(transport);
		}
		return build(client1, urlPrefix);
	}

	/**
	 * <p>
	 * Builds a static HTTP/2 {@link RestClient} with the current configuration.
	 * </p>
	 * <p>
	 * This method receives an URL prefix that should be used in all requests.
	 * </p>
	 * 
	 * @param urlPrefix the URL prefix.
	 * @return the {@link RestClient}.
	 */
	public RestClient build2(String urlPrefix) {
		urlPrefix = encode(urlPrefix);
		HTTP2Client client2;
		if (factory == null) {
			client2 = new HTTP2Client();
		} else {
			ClientConnector connector = new ClientConnector();
			connector.setSslContextFactory(factory);
			client2 = new HTTP2Client(connector);
		}
		HttpClientTransport transport = new HttpClientTransportOverHTTP2(client2);
		HttpClient client = new HttpClient(transport);
		return build(client, urlPrefix);
	}

	/**
	 * <p>
	 * Builds a static HTTP/3 {@link RestClient} with the current configuration.
	 * </p>
	 * <p>
	 * This method receives an URL prefix that should be used in all requests.
	 * </p>
	 * 
	 * @param urlPrefix the URL prefix.
	 * @return the {@link RestClient}.
	 */
	public RestClient build3(String urlPrefix) {
		urlPrefix = encode(urlPrefix);
		if (factory == null) {
			throw new IllegalArgumentException("HTTP/3 client must have a keytool truststore");
		}
		HTTP3Client client3 = new HTTP3Client();
		ClientConnector connector = client3.getClientConnector();
		connector.setSslContextFactory(factory);
		HttpClientTransport transport = new HttpClientTransportOverHTTP3(client3);
		HttpClient client = new HttpClient(transport);
		return build(client, urlPrefix);
	}
}
