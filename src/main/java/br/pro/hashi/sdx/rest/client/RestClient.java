package br.pro.hashi.sdx.rest.client;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

/**
 * Main object for sending REST requests.
 */
public final class RestClient {
	/**
	 * Instantiates a REST client using a specified URL prefix.
	 * 
	 * @param urlPrefix the URL prefix
	 * @return the client
	 */
	public static RestClient to(String urlPrefix) {
		return builder().build(urlPrefix);
	}

	/**
	 * Convenience method for instantiating a REST client builder.
	 * 
	 * @return the client builder
	 */
	public static RestClientBuilder builder() {
		return new RestClientBuilder();
	}

	private final Facade facade;
	private final HttpClient jettyClient;
	private final Charset urlCharset;
	private final String none;
	private final String urlPrefix;

	RestClient(Facade facade, HttpClient jettyClient, Charset urlCharset, String none, String urlPrefix) {
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
	 * Obtains the Jetty HttpClient used by this client.
	 * </p>
	 * <p>
	 * Call this method if you want to override the default configuration.
	 * </p>
	 * 
	 * @return the Jetty client
	 */
	public HttpClient getJettyClient() {
		return jettyClient;
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
	 * Convenience method to instantiate a {@link RestClient.Proxy} and call
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
	 * Convenience method to instantiate a {@link RestClient.Proxy} and call
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
	 * Convenience method to instantiate a {@link RestClient.Proxy} and call
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
	 * Convenience method to instantiate a {@link RestClient.Proxy} and call
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
	 * Represents a request configuration.
	 */
	public final class Proxy {
		private List<Entry> queries;
		private List<Entry> headers;
		private List<Body> bodies;

		private Proxy() {
			this.queries = new ArrayList<>();
			this.headers = new ArrayList<>();
			this.bodies = new ArrayList<>();
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
		 * Add a query without value to the request.
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
		 * Add a query to the request.
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
		 * Add a header to the request.
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
		 * Add a body to the request.
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

		private record Entry(String name, Object value) {
		}
	}
}
