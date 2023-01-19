package br.pro.hashi.sdx.rest.client;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

/**
 * Main object for sending REST requests.
 */
public final class RestClient {
	/**
	 * Instantiates a default REST client using a specified URL prefix.
	 * 
	 * @param urlPrefix the URL prefix
	 * @return the client
	 */
	public static RestClient to(String urlPrefix) {
		return builder().build(urlPrefix);
	}

	/**
	 * Convenience method that instantiates a REST client builder.
	 * 
	 * @return the client builder
	 */
	public static RestClientBuilder builder() {
		return new RestClientBuilder();
	}

	private final Logger logger;
	private final Facade facade;
	private final HttpClient jettyClient;
	private final Charset urlCharset;
	private final String none;
	private final String urlPrefix;

	RestClient(Facade facade, HttpClient jettyClient, Charset urlCharset, String none, String urlPrefix) {
		this.logger = LoggerFactory.getLogger(RestClient.class);
		this.facade = facade;
		this.jettyClient = jettyClient;
		this.urlCharset = urlCharset;
		this.none = none;
		this.urlPrefix = urlPrefix;
	}

	Facade getFacade() {
		return facade;
	}

	Charset getUrlCharset() {
		return urlCharset;
	}

	String getNone() {
		return none;
	}

	String getUrlPrefix() {
		return urlPrefix;
	}

	/**
	 * <p>
	 * Obtains the Jetty HttpClient used internally by this client.
	 * </p>
	 * <p>
	 * Call this method if you want to override the default configuration.
	 * </p>
	 * 
	 * @return the internal client
	 */
	public HttpClient getJettyClient() {
		return jettyClient;
	}

	/**
	 * Starts this client.
	 * 
	 * @throws ClientException if the Jetty HttpClient cannot be started
	 */
	public void start() {
		if (jettyClient.isRunning()) {
			return;
		}
		logger.info("Starting REST client...");
		try {
			jettyClient.start();
		} catch (Exception exception) {
			throw new ClientException(exception);
		}
		logger.info("REST client started");
	}

	/**
	 * Stops this client.
	 * 
	 * @throws ClientException if the Jetty HttpClient cannot be stopped
	 */
	public void stop() {
		if (!jettyClient.isRunning()) {
			return;
		}
		logger.info("Stopping REST client...");
		try {
			jettyClient.stop();
		} catch (Exception exception) {
			throw new ClientException(exception);
		}
		logger.info("REST client stopped");
	}

	/**
	 * Alias for {@link #withQuery(String)}.
	 * 
	 * @param name the query name
	 * @return the proxy, for chaining
	 * @throws NullPointerException if the query name is null
	 * @hidden
	 */
	public Proxy q(String name) {
		return withQuery(name);
	}

	/**
	 * Alias for {@link #withQuery(String, Object)}.
	 * 
	 * @param name  the query name
	 * @param value the query value
	 * @return the proxy, for chaining
	 * @throws NullPointerException if the query name is null or the query value is
	 *                              null
	 * @hidden
	 */
	public Proxy q(String name, Object value) {
		return withQuery(name, value);
	}

	/**
	 * <p>
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#withQuery(String)}.
	 * </p>
	 * <p>
	 * The alias {@link #q(String)} is available for short chaining.
	 * </p>
	 * 
	 * @param name the query name
	 * @return the proxy, for chaining
	 * @throws NullPointerException if the query name is null
	 */
	public Proxy withQuery(String name) {
		return new Proxy().withQuery(name);
	}

	/**
	 * <p>
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#withQuery(String, Object)}.
	 * </p>
	 * <p>
	 * The alias {@link #q(String, Object)} is available for short chaining.
	 * </p>
	 * 
	 * @param name  the query name
	 * @param value the query value
	 * @return the proxy, for chaining
	 * @throws NullPointerException if the query name is null or the query value is
	 *                              null
	 */
	public Proxy withQuery(String name, Object value) {
		return new Proxy().withQuery(name, value);
	}

	/**
	 * Alias for {@link #withHeader(String, Object)}.
	 * 
	 * @param name  the header name
	 * @param value the header value
	 * @return the proxy, for chaining
	 * @throws NullPointerException     if the header name is null or the header
	 *                                  value is null
	 * @throws IllegalArgumentException if the header name is blank
	 * @hidden
	 */
	public Proxy h(String name, Object value) {
		return withHeader(name, value);
	}

	/**
	 * <p>
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#withHeader(String, Object)}.
	 * </p>
	 * <p>
	 * The alias {@link #h(String, Object)} is available for short chaining.
	 * </p>
	 * 
	 * @param name  the header name
	 * @param value the header value
	 * @return the proxy, for chaining
	 * @throws NullPointerException     if the header name is null or the header
	 *                                  value is null
	 * @throws IllegalArgumentException if the header name is blank
	 */
	public Proxy withHeader(String name, Object value) {
		return new Proxy().withHeader(name, value);
	}

	/**
	 * Alias for {@link #withBody(Object)}.
	 * 
	 * @param body the body
	 * @return the proxy, for chaining
	 * @hidden
	 */
	public Proxy b(Object body) {
		return withBody(body);
	}

	/**
	 * <p>
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#withBody(Object)}.
	 * </p>
	 * <p>
	 * The alias {@link #b(Object)} is available for short chaining.
	 * </p>
	 * 
	 * @param body the body
	 * @return the proxy, for chaining
	 */
	public Proxy withBody(Object body) {
		return new Proxy().withBody(body);
	}

	/**
	 * Alias for {@code withBody(T, Hint<T>)}.
	 * 
	 * @param <T>  the type of the body
	 * @param body the body
	 * @param hint the type hint
	 * @return the proxy, for chaining
	 * @throws NullPointerException if the type hint is null
	 * @hidden
	 */
	public <T> Proxy b(T body, Hint<T> hint) {
		return withBody(body, hint);
	}

	/**
	 * <p>
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@code RestClient.Proxy.withBody(T, Hint<T>)}.
	 * </p>
	 * <p>
	 * The alias {@code b(T, Hint<T>)} is available for short chaining.
	 * </p>
	 * 
	 * @param <T>  the type of the body
	 * @param body the body
	 * @param hint the type hint
	 * @return the proxy, for chaining
	 * @throws NullPointerException if the type hint is null
	 */
	public <T> Proxy withBody(T body, Hint<T> hint) {
		return new Proxy().withBody(body, hint);
	}

	/**
	 * Alias for {@link #withTimeout(int)}.
	 * 
	 * @param timeout the timeout, in seconds
	 * @return this proxy, for chaining
	 * @throws IllegalArgumentException if the timeout is not positive
	 * @hidden
	 */
	public Proxy t(int timeout) {
		return withTimeout(timeout);
	}

	/**
	 * <p>
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#withTimeout(int)}.
	 * </p>
	 * <p>
	 * The alias {@link #t(int)} is available for short chaining.
	 * </p>
	 * 
	 * @param timeout the timeout, in seconds
	 * @return this proxy, for chaining
	 * @throws IllegalArgumentException if the timeout is not positive
	 */
	public Proxy withTimeout(int timeout) {
		return new Proxy().withTimeout(timeout);
	}

	/**
	 * Represents a request configuration.
	 */
	public final class Proxy {
		private final List<Entry> queries;
		private final List<Entry> headers;
		private final List<Body> bodies;
		private int timeout;

		Proxy() {
			this.queries = new ArrayList<>();
			this.headers = new ArrayList<>();
			this.bodies = new ArrayList<>();
			this.timeout = 0;
		}

		List<Entry> getQueries() {
			return queries;
		}

		List<Entry> getHeaders() {
			return headers;
		}

		List<Body> getBodies() {
			return bodies;
		}

		int getTimeout() {
			return timeout;
		}

		/**
		 * Alias for {@link #withQuery(String)}.
		 * 
		 * @param name the query name
		 * @return this proxy, for chaining
		 * @throws NullPointerException if the query name is null
		 * @hidden
		 */
		public Proxy q(String name) {
			return withQuery(name);
		}

		/**
		 * Alias for {@link #withQuery(String, Object)}.
		 * 
		 * @param name  the query name
		 * @param value the query value
		 * @return this proxy, for chaining
		 * @throws NullPointerException if the query name is null or the query value is
		 *                              null
		 * @hidden
		 */
		public Proxy q(String name, Object value) {
			return withQuery(name, value);
		}

		/**
		 * <p>
		 * Adds a query without value to the request.
		 * </p>
		 * <p>
		 * The value is converted to {@code String} via {@code toString()} and encoded
		 * in the URL charset.
		 * </p>
		 * <p>
		 * The alias {@link #q(String)} is available for short chaining.
		 * </p>
		 * 
		 * @param name the query name
		 * @return this proxy, for chaining
		 * @throws NullPointerException if the query name is null
		 */
		public Proxy withQuery(String name) {
			if (name == null) {
				throw new NullPointerException("Query name cannot be null");
			}
			queries.add(new Entry(name, null));
			return this;
		}

		/**
		 * <p>
		 * Adds a query to the request.
		 * </p>
		 * <p>
		 * The value is converted to {@code String} via {@code toString()} and encoded
		 * in the URL charset.
		 * </p>
		 * <p>
		 * The alias {@link #q(String, Object)} is available for short chaining.
		 * </p>
		 * 
		 * @param name  the query name
		 * @param value the query value
		 * @return this proxy, for chaining
		 * @throws NullPointerException if the query name is null or the query value is
		 *                              null
		 */
		public Proxy withQuery(String name, Object value) {
			if (name == null) {
				throw new NullPointerException("Query name cannot be null");
			}
			if (value == null) {
				throw new NullPointerException("Query value cannot be null");
			}
			queries.add(new Entry(name, value));
			return this;
		}

		/**
		 * Alias for {@link #withHeader(String, Object)}.
		 * 
		 * @param name  the header name
		 * @param value the header value
		 * @return this proxy, for chaining
		 * @throws NullPointerException     if the header name is null or the header
		 *                                  value is null
		 * @throws IllegalArgumentException if the header name is blank
		 * @hidden
		 */
		public Proxy h(String name, Object value) {
			return withHeader(name, value);
		}

		/**
		 * <p>
		 * Adds a header to the request.
		 * </p>
		 * <p>
		 * The value is converted to {@code String} via {@code toString()} and encoded
		 * in the {@link StandardCharsets#US_ASCII} charset.
		 * </p>
		 * <p>
		 * The alias {@link #h(String, Object)} is available for short chaining.
		 * </p>
		 * 
		 * @param name  the header name
		 * @param value the header value
		 * @return this proxy, for chaining
		 * @throws NullPointerException     if the header name is null or the header
		 *                                  value is null
		 * @throws IllegalArgumentException if the header name is blank
		 */
		public Proxy withHeader(String name, Object value) {
			if (name == null) {
				throw new NullPointerException("Header name cannot be null");
			}
			name = name.strip();
			if (name.isEmpty()) {
				throw new IllegalArgumentException("Header name cannot be blank");
			}
			if (value == null) {
				throw new NullPointerException("Header value cannot be null");
			}
			headers.add(new Entry(name, value));
			return this;
		}

		/**
		 * Alias for {@link #withBody(Object)}.
		 * 
		 * @param body the body
		 * @return this proxy, for chaining
		 * @hidden
		 */
		public Proxy b(Object body) {
			return withBody(body);
		}

		/**
		 * <p>
		 * Adds a body to the request.
		 * </p>
		 * <p>
		 * This method calls {@code body.getClass()} to obtain the body type. Since
		 * {@code body.getClass()} loses generic information due to type erasure, do not
		 * call it if the type is generic. Call {@code withBody(T, Hint<T>)} instead.
		 * </p>
		 * <p>
		 * The alias {@link #b(Object)} is available for short chaining.
		 * </p>
		 * 
		 * @param body the body
		 * @return this proxy, for chaining
		 */
		public Proxy withBody(Object body) {
			if (body instanceof Body) {
				bodies.add((Body) body);
			} else {
				bodies.add(new Body(body));
			}
			return this;
		}

		/**
		 * Alias for {@code withBody(T, Hint<T>)}.
		 * 
		 * @param <T>  the type of the body
		 * @param body the body
		 * @param hint the type hint
		 * @return this proxy, for chaining
		 * @throws NullPointerException if the type hint is null
		 * @hidden
		 */
		public <T> Proxy b(T body, Hint<T> hint) {
			return withBody(body, hint);
		}

		/**
		 * <p>
		 * Adds a body with hinted type to the request.
		 * </p>
		 * <p>
		 * Call this method if the body type is generic.
		 * </p>
		 * <p>
		 * The alias {@code b(T, Hint<T>)} is available for short chaining.
		 * </p>
		 * 
		 * @param <T>  the type of the body
		 * @param body the body
		 * @param hint the type hint
		 * @return this proxy, for chaining
		 * @throws NullPointerException if the type hint is null
		 */
		public <T> Proxy withBody(T body, Hint<T> hint) {
			if (body instanceof Body) {
				bodies.add((Body) body);
			} else {
				bodies.add(new Body(body, hint));
			}
			return this;
		}

		/**
		 * Alias for {@link #withTimeout(int)}.
		 * 
		 * @param timeout the timeout, in seconds
		 * @return this proxy, for chaining
		 * @throws IllegalArgumentException if the timeout is not positive
		 * @hidden
		 */
		public Proxy t(int timeout) {
			return withTimeout(timeout);
		}

		/**
		 * <p>
		 * Sets the timeout of the request.
		 * </p>
		 * <p>
		 * The alias {@link #t(int)} is available for short chaining.
		 * </p>
		 * 
		 * @param timeout the timeout, in seconds
		 * @return this proxy, for chaining
		 * @throws IllegalArgumentException if the timeout is not positive
		 */
		public Proxy withTimeout(int timeout) {
			if (timeout < 1) {
				throw new IllegalArgumentException("Timeout must be positive");
			}
			this.timeout = timeout;
			return this;
		}
	}

	record Entry(String name, Object value) {
	}
}
