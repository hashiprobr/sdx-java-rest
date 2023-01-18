package br.pro.hashi.sdx.rest.client;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
		private Proxy() {
		}

		/**
		 * Alias for {@link #withQuery(String)}.
		 * 
		 * @param name the query name
		 * @return this proxy, for chaining
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
		 */
		public Proxy withQuery(String name) {
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
		 */
		public Proxy withQuery(String name, Object value) {
			return this;
		}

		/**
		 * Alias for {@link #withHeader(String, Object)}.
		 * 
		 * @param name  the header name
		 * @param value the header value
		 * @return this proxy, for chaining
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
		 */
		public Proxy withHeader(String name, Object value) {
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
			return this;
		}
	}
}
