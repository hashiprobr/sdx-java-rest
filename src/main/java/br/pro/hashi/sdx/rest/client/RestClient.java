package br.pro.hashi.sdx.rest.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Request.Content;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.MultiPartRequestContent;
import org.eclipse.jetty.client.util.OutputStreamRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.coding.Percent;
import br.pro.hashi.sdx.rest.coding.Query;
import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.server.exception.ServerException;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
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
	private final Cache cache;
	private final Facade facade;
	private final HttpClient jettyClient;
	private final Charset urlCharset;
	private final Locale locale;
	private final String none;
	private final String urlPrefix;

	RestClient(Cache cache, Facade facade, HttpClient jettyClient, Charset urlCharset, Locale locale, String none, String urlPrefix) {
		this.logger = LoggerFactory.getLogger(RestClient.class);
		this.cache = cache;
		this.facade = facade;
		this.jettyClient = jettyClient;
		this.urlCharset = urlCharset;
		this.locale = locale;
		this.none = none;
		this.urlPrefix = urlPrefix;
	}

	Cache getCache() {
		return cache;
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
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#get(String)}.
	 * 
	 * @param uri the URI
	 * @return the response
	 * @throws NullPointerException     if the URI is null
	 * @throws IllegalArgumentException if the URI is invalid
	 */
	public RestResponse get(String uri) {
		return new Proxy().get(uri);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#post(String)}.
	 * 
	 * @param uri the URI
	 * @return the response
	 * @throws NullPointerException     if the URI is null
	 * @throws IllegalArgumentException if the URI is invalid
	 * @hidden
	 */
	public RestResponse post(String uri) {
		return new Proxy().post(uri);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#post(String, Object)}.
	 * 
	 * @param uri  the URI
	 * @param body the body
	 * @return the response
	 * @throws NullPointerException     if the URI is null
	 * @throws IllegalArgumentException if the URI is invalid
	 */
	public RestResponse post(String uri, Object body) {
		return new Proxy().post(uri, body);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@code RestClient.Proxy.post(String, T, Hint<T>)}.
	 * 
	 * @param <T>  the type of the object
	 * @param uri  the URI
	 * @param body the body
	 * @param hint the type hint
	 * @return the response
	 * @throws NullPointerException     if the URI is null or the type hint is null
	 * @throws IllegalArgumentException if the URI is invalid
	 */
	public <T> RestResponse post(String uri, T body, Hint<T> hint) {
		return new Proxy().post(uri, body, hint);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#put(String)}.
	 * 
	 * @param uri the URI
	 * @return the response
	 * @throws NullPointerException     if the URI is null
	 * @throws IllegalArgumentException if the URI is invalid
	 * @hidden
	 */
	public RestResponse put(String uri) {
		return new Proxy().put(uri);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#put(String, Object)}.
	 * 
	 * @param uri  the URI
	 * @param body the body
	 * @return the response
	 * @throws NullPointerException     if the URI is null
	 * @throws IllegalArgumentException if the URI is invalid
	 */
	public RestResponse put(String uri, Object body) {
		return new Proxy().put(uri, body);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@code RestClient.Proxy.put(String, T, Hint<T>)}.
	 * 
	 * @param <T>  the type of the object
	 * @param uri  the URI
	 * @param body the body
	 * @param hint the type hint
	 * @return the response
	 * @throws NullPointerException     if the URI is null or the type hint is null
	 * @throws IllegalArgumentException if the URI is invalid
	 */
	public <T> RestResponse put(String uri, T body, Hint<T> hint) {
		return new Proxy().put(uri, body, hint);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#patch(String)}.
	 * 
	 * @param uri the URI
	 * @return the response
	 * @throws NullPointerException     if the URI is null
	 * @throws IllegalArgumentException if the URI is invalid
	 * @hidden
	 */
	public RestResponse patch(String uri) {
		return new Proxy().patch(uri);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#patch(String, Object)}.
	 * 
	 * @param uri  the URI
	 * @param body the body
	 * @return the response
	 * @throws NullPointerException     if the URI is null
	 * @throws IllegalArgumentException if the URI is invalid
	 */
	public RestResponse patch(String uri, Object body) {
		return new Proxy().patch(uri, body);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@code RestClient.Proxy.patch(String, T, Hint<T>)}.
	 * 
	 * @param <T>  the type of the object
	 * @param uri  the URI
	 * @param body the body
	 * @param hint the type hint
	 * @return the response
	 * @throws NullPointerException     if the URI is null or the type hint is null
	 * @throws IllegalArgumentException if the URI is invalid
	 */
	public <T> RestResponse patch(String uri, T body, Hint<T> hint) {
		return new Proxy().patch(uri, body, hint);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#delete(String)}.
	 * 
	 * @param uri the URI
	 * @return the response
	 * @throws NullPointerException     if the URI is null
	 * @throws IllegalArgumentException if the URI is invalid
	 */
	public RestResponse delete(String uri) {
		return new Proxy().delete(uri);
	}

	/**
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#request(String, String)}.
	 * 
	 * @param method the method
	 * @param uri    the URI
	 * @return the response
	 * @throws NullPointerException     if the method is null or the URI is null
	 * @throws IllegalArgumentException if the method is blank or the URI is invalid
	 */
	public RestResponse request(String method, String uri) {
		return new Proxy().request(method, uri);
	}

	/**
	 * Represents a request configuration.
	 */
	public final class Proxy {
		private final CharsetEncoder encoder;
		private final List<Entry> queries;
		private final List<Entry> headers;
		private final List<RestBody> bodies;
		private int timeout;

		Proxy() {
			this.encoder = StandardCharsets.US_ASCII.newEncoder();
			this.queries = new ArrayList<>();
			this.headers = new ArrayList<>();
			this.bodies = new ArrayList<>();
			this.timeout = 0;
		}

		RestClient getEnclosing() {
			return RestClient.this;
		}

		List<Entry> getQueries() {
			return queries;
		}

		List<Entry> getHeaders() {
			return headers;
		}

		List<RestBody> getBodies() {
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
			queries.add(new Entry(encode(name), null));
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
			String valueString = value.toString();
			if (valueString == null) {
				throw new NullPointerException("Query value string cannot be null");
			}
			queries.add(new Entry(encode(name), encode(valueString)));
			return this;
		}

		private String encode(String item) {
			return Query.encode(item, urlCharset);
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
			if (!encoder.canEncode(name)) {
				throw new IllegalArgumentException("Header name must be in US-ASCII");
			}
			if (value == null) {
				throw new NullPointerException("Header value cannot be null");
			}
			String valueString = value.toString();
			if (valueString == null) {
				throw new NullPointerException("Header value string cannot be null");
			}
			if (!encoder.canEncode(valueString)) {
				throw new IllegalArgumentException("Header value string must be in US-ASCII");
			}
			headers.add(new Entry(name, valueString));
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
			if (body instanceof RestBody) {
				bodies.add((RestBody) body);
			} else {
				bodies.add(new RestBody(body));
			}
			return this;
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
			if (body instanceof RestBody) {
				bodies.add((RestBody) body);
			} else {
				bodies.add(new RestBody(body, hint));
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

		/**
		 * Sends a GET request to a specified URI.
		 * 
		 * @param uri the URI
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public RestResponse get(String uri) {
			return doRequest("GET", uri);
		}

		/**
		 * Sends a POST request without body to a specified URI.
		 * 
		 * @param uri the URI
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 * @hidden
		 */
		public RestResponse post(String uri) {
			return doRequest("POST", uri);
		}

		/**
		 * <p>
		 * Sends a POST request to a specified URI.
		 * </p>
		 * <p>
		 * This method calls {@code body.getClass()} to obtain the body type. Since
		 * {@code body.getClass()} loses generic information due to type erasure, do not
		 * call it if the type is generic. Call {@code post(String, T, Hint<T>)}
		 * instead.
		 * </p>
		 * 
		 * @param uri  the URI
		 * @param body the body
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public RestResponse post(String uri, Object body) {
			return withBody(body).doRequest("POST", uri);
		}

		/**
		 * <p>
		 * Sends a POST request with hinted type to a specified URI.
		 * </p>
		 * <p>
		 * Call this method if the body type is generic.
		 * </p>
		 * 
		 * @param <T>  the type of the object
		 * @param uri  the URI
		 * @param body the body
		 * @param hint the type hint
		 * @return the response
		 * @throws NullPointerException     if the URI is null or the type hint is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public <T> RestResponse post(String uri, T body, Hint<T> hint) {
			return withBody(body, hint).doRequest("POST", uri);
		}

		/**
		 * Sends a PUT request without body to a specified URI.
		 * 
		 * @param uri the URI
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 * @hidden
		 */
		public RestResponse put(String uri) {
			return doRequest("PUT", uri);
		}

		/**
		 * <p>
		 * Sends a PUT request to a specified URI.
		 * </p>
		 * <p>
		 * This method calls {@code body.getClass()} to obtain the body type. Since
		 * {@code body.getClass()} loses generic information due to type erasure, do not
		 * call it if the type is generic. Call {@code put(String, T, Hint<T>)} instead.
		 * </p>
		 * 
		 * @param uri  the URI
		 * @param body the body
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public RestResponse put(String uri, Object body) {
			return withBody(body).doRequest("PUT", uri);
		}

		/**
		 * <p>
		 * Sends a PUT request with hinted type to a specified URI.
		 * </p>
		 * <p>
		 * Call this method if the body type is generic.
		 * </p>
		 * 
		 * @param <T>  the type of the object
		 * @param uri  the URI
		 * @param body the body
		 * @param hint the type hint
		 * @return the response
		 * @throws NullPointerException     if the URI is null or the type hint is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public <T> RestResponse put(String uri, T body, Hint<T> hint) {
			return withBody(body, hint).doRequest("PUT", uri);
		}

		/**
		 * Sends a PATCH request without body to a specified URI.
		 * 
		 * @param uri the URI
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 * @hidden
		 */
		public RestResponse patch(String uri) {
			return doRequest("PATCH", uri);
		}

		/**
		 * <p>
		 * Sends a PATCH request to a specified URI.
		 * </p>
		 * <p>
		 * This method calls {@code body.getClass()} to obtain the body type. Since
		 * {@code body.getClass()} loses generic information due to type erasure, do not
		 * call it if the type is generic. Call {@code patch(String, T, Hint<T>)}
		 * instead.
		 * </p>
		 * 
		 * @param uri  the URI
		 * @param body the body
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public RestResponse patch(String uri, Object body) {
			return withBody(body).doRequest("PATCH", uri);
		}

		/**
		 * <p>
		 * Sends a PATCH request with hinted type to a specified URI.
		 * </p>
		 * <p>
		 * Call this method if the body type is generic.
		 * </p>
		 * 
		 * @param <T>  the type of the object
		 * @param uri  the URI
		 * @param body the body
		 * @param hint the type hint
		 * @return the response
		 * @throws NullPointerException     if the URI is null or the type hint is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public <T> RestResponse patch(String uri, T body, Hint<T> hint) {
			return withBody(body, hint).doRequest("PATCH", uri);
		}

		/**
		 * Sends a DELETE request to a specified URI.
		 * 
		 * @param uri the URI
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public RestResponse delete(String uri) {
			return doRequest("DELETE", uri);
		}

		/**
		 * Sends a specified method to a specified URI.
		 * 
		 * @param method the method
		 * @param uri    the URI
		 * @return the response
		 * @throws NullPointerException     if the method is null or the URI is null
		 * @throws IllegalArgumentException if the method is blank or the URI is invalid
		 */
		public RestResponse request(String method, String uri) {
			if (method == null) {
				throw new NullPointerException("Method cannot be null");
			}
			method = method.strip();
			if (method.isEmpty()) {
				throw new IllegalArgumentException("Method cannot be blank");
			}
			return doRequest(method.toUpperCase(locale), uri);
		}

		RestResponse doRequest(String method, String uri) {
			if (uri == null) {
				throw new NullPointerException("URI cannot be null");
			}
			uri = uri.strip();
			if (uri.isEmpty()) {
				throw new IllegalArgumentException("URI cannot be blank");
			}
			if (!uri.startsWith("/")) {
				throw new IllegalArgumentException("URI must start with /");
			}
			String url = "%s%s".formatted(urlPrefix, withQueries(uri));
			Request request = jettyClient.newRequest(url).method(method);
			addHeaders(request);
			List<Task> tasks = addBodiesAndGetTasks(request);
			return send(request, tasks);
		}

		String withQueries(String uri) {
			StringJoiner joiner = new StringJoiner("&");

			String[] items;
			int index = uri.indexOf('?');
			if (index == -1) {
				items = splitAndEncode(uri);
			} else {
				String prefix = uri.substring(0, index);
				String suffix = uri.substring(index + 1);
				items = splitAndEncode(prefix);

				for (String item : suffix.split("&", -1)) {
					index = item.indexOf('=');
					if (index == -1) {
						joiner.add(recode(item));
					} else {
						prefix = item.substring(0, index);
						suffix = item.substring(index + 1);
						joiner.add("%s=%s".formatted(recode(prefix), recode(suffix)));
					}
				}
			}

			for (Entry entry : queries) {
				String name = entry.name();
				String value = entry.valueString();
				if (value == null) {
					joiner.add(name);
				} else {
					joiner.add("%s=%s".formatted(name, value));
				}
			}

			uri = String.join("/", items);
			if (joiner.length() > 0) {
				uri = "%s?%s".formatted(uri, joiner.toString());
			}
			return uri;
		}

		private String[] splitAndEncode(String uri) {
			uri = Percent.stripEndingSlashes(uri);
			return Percent.splitAndEncode(uri, urlCharset);
		}

		private String recode(String item) {
			return Query.recode(item, urlCharset);
		}

		void addHeaders(Request request) {
			request.headers((fields) -> {
				for (Entry entry : headers) {
					fields.add(entry.name(), entry.valueString());
				}
			});
		}

		List<Task> addBodiesAndGetTasks(Request request) {
			List<Task> tasks = new ArrayList<>();
			if (!bodies.isEmpty()) {
				if (bodies.size() == 1) {
					RestBody body = bodies.get(0);
					request.body(addTaskAndGetContent(tasks, body));
				} else {
					try (MultiPartRequestContent content = new MultiPartRequestContent()) {
						for (RestBody body : bodies) {
							content.addFieldPart(body.getName(), addTaskAndGetContent(tasks, body), null);
						}
						request.body(content);
					}
				}
			}
			return tasks;
		}

		Content addTaskAndGetContent(List<Task> tasks, RestBody body) {
			Object actual = body.getActual();
			Type type = body.getType();
			String contentType = body.getContentType();
			Charset charset = body.getCharset();
			boolean base64 = body.isBase64();

			Consumer<OutputStream> consumer;
			if (facade.isBinary(type)) {
				contentType = facade.cleanForAssembling(contentType, actual);
				Assembler assembler = facade.getAssembler(contentType);
				consumer = (stream) -> {
					assembler.write(actual, type, stream);
				};
			} else {
				contentType = facade.cleanForSerializing(contentType, actual);
				Serializer serializer = facade.getSerializer(contentType);
				consumer = (stream) -> {
					OutputStreamWriter writer = new OutputStreamWriter(stream, charset);
					serializer.write(actual, type, writer);
				};
				contentType = "%s;charset=%s".formatted(contentType, charset.name());
			}

			if (base64) {
				contentType = "%s;base64".formatted(contentType);
			}
			OutputStreamRequestContent content = new OutputStreamRequestContent(contentType);
			OutputStream stream = content.getOutputStream();
			if (base64) {
				stream = Media.encode(stream);
			}

			tasks.add(new Task(consumer, stream));
			return content;
		}

		RestResponse send(Request request, List<Task> tasks) {
			start();

			InputStreamResponseListener listener = new InputStreamResponseListener();
			request.send(listener);

			for (Task task : tasks) {
				Consumer<OutputStream> consumer = task.consumer();
				try (OutputStream stream = task.stream()) {
					consumer.accept(stream);
				} catch (IOException exception) {
					throw new UncheckedIOException(exception);
				}
			}

			Response response;
			try {
				response = listener.get(timeout, TimeUnit.SECONDS);
			} catch (ExecutionException exception) {
				throw new ServerException(exception.getCause());
			} catch (TimeoutException exception) {
				throw new ServerException(exception);
			} catch (InterruptedException exception) {
				throw new AssertionError(exception);
			}

			int status = response.getStatus();
			HttpFields fields = response.getHeaders();
			Headers headers = new Headers(cache, fields);
			String contentType = fields.get("Content-Type");
			InputStream stream = listener.getInputStream();
			return new RestResponse(facade, status, headers, contentType, stream);
		}

		record Entry(String name, String valueString) {
		}

		record Task(Consumer<OutputStream> consumer, OutputStream stream) {
		}
	}
}
