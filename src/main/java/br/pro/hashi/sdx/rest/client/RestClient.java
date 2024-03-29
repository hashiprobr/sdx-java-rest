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
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.MultiPartRequestContent;
import org.eclipse.jetty.client.util.OutputStreamRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.coding.PathCoder;
import br.pro.hashi.sdx.rest.coding.QueryCoder;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

/**
 * Sends REST requests.
 */
public final class RestClient {
	static final int TIMEOUT = 30;

	static RestClient newInstance(TransformManager manager, HttpClient jettyClient, Locale locale, Charset urlCharset, String urlPrefix) {
		QueryCoder queryCoder = QueryCoder.getInstance();
		PathCoder pathCoder = PathCoder.getInstance();
		MediaCoder mediaCoder = MediaCoder.getInstance();
		return new RestClient(queryCoder, pathCoder, mediaCoder, manager, jettyClient, locale, urlCharset, urlPrefix);
	}

	/**
	 * <p>
	 * Builds a dynamic HTTP/2 and HTTP/1.1 client with a default configuration to
	 * the specified URL prefix.
	 * </p>
	 *
	 * @param urlPrefix the URL prefix
	 * @return the client
	 * @throws NullPointerException     if the URL prefix is null
	 * @throws IllegalArgumentException if the URL prefix is invalid
	 */
	public static RestClient to(String urlPrefix) {
		RestClientBuilder builder = new RestClientBuilder();
		return builder.build(urlPrefix);
	}

	private final Logger logger;
	private final QueryCoder queryCoder;
	private final PathCoder pathCoder;
	private final MediaCoder mediaCoder;
	private final TransformManager manager;
	private final HttpClient jettyClient;
	private final Locale locale;
	private final Charset urlCharset;
	private final String urlPrefix;

	RestClient(QueryCoder queryCoder, PathCoder pathCoder, MediaCoder mediaCoder, TransformManager manager, HttpClient jettyClient, Locale locale, Charset urlCharset, String urlPrefix) {
		this.logger = LoggerFactory.getLogger(RestClient.class);
		this.queryCoder = queryCoder;
		this.pathCoder = pathCoder;
		this.mediaCoder = mediaCoder;
		this.manager = manager;
		this.jettyClient = jettyClient;
		this.locale = locale;
		this.urlCharset = urlCharset;
		this.urlPrefix = urlPrefix;
	}

	TransformManager getManager() {
		return manager;
	}

	Locale getLocale() {
		return locale;
	}

	Charset getUrlCharset() {
		return urlCharset;
	}

	String getUrlPrefix() {
		return urlPrefix;
	}

	/**
	 * <p>
	 * Obtains the Jetty HttpClient used internally by this client.
	 * </p>
	 * <p>
	 * Call this method if you want to override the builder configuration.
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
	 * @throws ClientException if the internal client cannot be started
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
	 * @throws ClientException if the internal client cannot be stopped
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
	 * @throws IllegalArgumentException if the header name is invalid or the header
	 *                                  value is invalid
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
	 * @throws IllegalArgumentException if the header name is invalid or the header
	 *                                  value is invalid
	 */
	public Proxy withHeader(String name, Object value) {
		return new Proxy().withHeader(name, value);
	}

	/**
	 * Alias for {@link #withBody(Object)}.
	 *
	 * @param body the body
	 * @return the proxy, for chaining
	 * @throws IllegalArgumentException if already added a part or the body is an
	 *                                  instance of {@code RestPart}
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
	 * @throws IllegalArgumentException if already added a part or the body is an
	 *                                  instance of {@code RestPart}
	 */
	public Proxy withBody(Object body) {
		return new Proxy().withBody(body);
	}

	/**
	 * Alias for {@link #withPart(String, Object)}.
	 *
	 * @param name the name
	 * @param part the part
	 * @return the proxy, for chaining
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if already added a body or the part is an
	 *                                  instance of {@code RestBody}
	 * @hidden
	 */
	public Proxy p(String name, Object part) {
		return withPart(name, part);
	}

	/**
	 * <p>
	 * Convenience method that instantiates a {@link RestClient.Proxy} and calls
	 * {@link RestClient.Proxy#withPart(String, Object)}.
	 * </p>
	 * <p>
	 * The alias {@link #p(String, Object)} is available for short chaining.
	 * </p>
	 *
	 * @param name the name
	 * @param part the part
	 * @return the proxy, for chaining
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if already added a body or the part is an
	 *                                  instance of {@code RestBody}
	 */
	public Proxy withPart(String name, Object part) {
		return new Proxy().withPart(name, part);
	}

	/**
	 * Alias for {@link #withTimeout(int)}.
	 *
	 * @param timeout the timeout, in seconds
	 * @return the proxy, for chaining
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
	 * @return the proxy, for chaining
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
	 * <p>
	 * Represents a request configuration.
	 * </p>
	 * <p>
	 * Sending a request consumes the body but preserves everything else for reuse.
	 * </p>
	 */
	public final class Proxy {
		private final CharsetEncoder encoder;
		private final List<Entry> queries;
		private final List<Entry> headers;
		private final List<RestPart> parts;
		private RestBody body;
		private int timeout;

		Proxy() {
			this.encoder = StandardCharsets.US_ASCII.newEncoder();
			this.queries = new ArrayList<>();
			this.headers = new ArrayList<>();
			this.parts = new ArrayList<>();
			this.body = null;
			this.timeout = TIMEOUT;
		}

		List<Entry> getQueries() {
			return queries;
		}

		List<Entry> getHeaders() {
			return headers;
		}

		List<RestPart> getParts() {
			return parts;
		}

		RestBody getBody() {
			return body;
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
		 * The name is encoded in the URL charset.
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
		 * The value is converted to {@code String} via {@code toString()}. The name and
		 * the value string are encoded in the URL charset.
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
			return queryCoder.encode(item, urlCharset);
		}

		/**
		 * Alias for {@link #withHeader(String, Object)}.
		 *
		 * @param name  the header name
		 * @param value the header value
		 * @return this proxy, for chaining
		 * @throws NullPointerException     if the header name is null or the header
		 *                                  value is null
		 * @throws IllegalArgumentException if the header name is invalid or the header
		 *                                  value is invalid
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
		 * The value is converted to {@code String} via {@code toString()}. The name and
		 * the value string are encoded in {@link StandardCharsets#US_ASCII}.
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
		 * @throws IllegalArgumentException if the header name is invalid or the header
		 *                                  value is invalid
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
		 * @throws IllegalArgumentException if already added a part or the body is an
		 *                                  instance of {@code RestPart}
		 * @hidden
		 */
		public Proxy b(Object body) {
			return withBody(body);
		}

		/**
		 * <p>
		 * Sets the body of the request.
		 * </p>
		 * <p>
		 * The alias {@link #b(Object)} is available for short chaining.
		 * </p>
		 *
		 * @param body the body
		 * @return this proxy, for chaining
		 * @throws IllegalArgumentException if already added a part or the body is an
		 *                                  instance of {@code RestPart}
		 */
		public Proxy withBody(Object body) {
			if (parts.size() > 0) {
				throw new IllegalArgumentException("Cannot set body if already added a part");
			}
			RestBody restBody;
			if (body instanceof RestBody) {
				if (body instanceof RestPart) {
					throw new IllegalArgumentException("Body cannot be an instance of RestPart");
				} else {
					restBody = (RestBody) body;
				}
			} else {
				restBody = RestBody.of(body);
			}
			this.body = restBody;
			return this;
		}

		/**
		 * Alias for {@link #withPart(String, Object)}.
		 *
		 * @param name the name
		 * @param part the part
		 * @return this proxy, for chaining
		 * @throws NullPointerException     if the name is null
		 * @throws IllegalArgumentException if already added a body or the part is an
		 *                                  instance of {@code RestBody}
		 * @hidden
		 */
		public Proxy p(String name, Object part) {
			return withPart(name, part);
		}

		/**
		 * <p>
		 * Adds a multipart body part to the request.
		 * </p>
		 * <p>
		 * The alias {@link #p(String, Object)} is available for short chaining.
		 * </p>
		 *
		 * @param name the name
		 * @param part the part
		 * @return this proxy, for chaining
		 * @throws NullPointerException     if the name is null
		 * @throws IllegalArgumentException if already added a body or the part is an
		 *                                  instance of {@code RestBody}
		 */
		public Proxy withPart(String name, Object part) {
			if (name == null) {
				throw new NullPointerException("Name cannot be null");
			}
			if (body != null) {
				throw new IllegalArgumentException("Cannot add part if already added a body");
			}
			RestPart restPart;
			if (part instanceof RestPart) {
				restPart = (RestPart) part;
			} else {
				if (part instanceof RestBody) {
					throw new IllegalArgumentException("Part cannot be an instance of RestBody");
				} else {
					restPart = RestPart.of(part);
				}
			}
			restPart.setName(name);
			parts.add(restPart);
			return this;
		}

		/**
		 * Alias for {@link #withTimeout(int)}.
		 *
		 * @param timeout the timeout, in seconds
		 * @return this proxy, for chaining
		 * @hidden
		 */
		public Proxy t(int timeout) {
			return withTimeout(timeout);
		}

		/**
		 * <p>
		 * Sets the timeout of the request. Default is {@value RestClient#TIMEOUT}.
		 * </p>
		 * <p>
		 * The alias {@link #t(int)} is available for short chaining.
		 * </p>
		 *
		 * @param timeout the timeout, in seconds
		 * @return this proxy, for chaining
		 */
		public Proxy withTimeout(int timeout) {
			this.timeout = timeout;
			return this;
		}

		/**
		 * Sends a GET request to the specified URI.
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
		 * Sends a POST request without body to the specified URI.
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
		 * Sends a POST request to the specified URI.
		 *
		 * @param uri  the URI
		 * @param body the body
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public RestResponse post(String uri, Object body) {
			return withBody(body).post(uri);
		}

		/**
		 * Sends a PUT request without body to the specified URI.
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
		 * Sends a PUT request to the specified URI.
		 *
		 * @param uri  the URI
		 * @param body the body
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public RestResponse put(String uri, Object body) {
			return withBody(body).put(uri);
		}

		/**
		 * Sends a PATCH request without body to the specified URI.
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
		 * Sends a PATCH request to the specified URI.
		 *
		 * @param uri  the URI
		 * @param body the body
		 * @return the response
		 * @throws NullPointerException     if the URI is null
		 * @throws IllegalArgumentException if the URI is invalid
		 */
		public RestResponse patch(String uri, Object body) {
			return withBody(body).patch(uri);
		}

		/**
		 * Sends a DELETE request to the specified URI.
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
		 * Sends the specified method to the specified URI.
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
			String url = "%s%s".formatted(urlPrefix, completeQueries(uri));
			Request request = jettyClient.newRequest(url).method(method);
			addHeaders(request);
			List<Task> tasks = consumeBodyAndBuildTasks(request);
			return send(request, tasks);
		}

		String completeQueries(String uri) {
			StringJoiner joiner = new StringJoiner("&");

			int index = uri.indexOf('?');
			if (index == -1) {
				uri = stripAndRecode(uri);
			} else {
				String prefix = uri.substring(0, index);
				String suffix = uri.substring(index + 1);
				uri = stripAndRecode(prefix);

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

			if (!queries.isEmpty()) {
				if (joiner.length() == 0) {
					joiner = new StringJoiner("&");
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
			}

			if (joiner.length() > 0) {
				uri = "%s?%s".formatted(uri, joiner.toString());
			}
			return uri;
		}

		private String stripAndRecode(String uri) {
			uri = pathCoder.stripEndingSlashes(uri);
			return pathCoder.recode(uri, urlCharset);
		}

		private String recode(String item) {
			return queryCoder.recode(item, urlCharset);
		}

		void addHeaders(Request request) {
			request.headers((fields) -> {
				for (Entry entry : headers) {
					fields.add(entry.name(), entry.valueString());
				}
			});
		}

		List<Task> consumeBodyAndBuildTasks(Request request) {
			List<Task> tasks = new ArrayList<>();
			if (body != null) {
				request.body(addTaskAndBuildContent(tasks, body));
				body = null;
			} else {
				if (!parts.isEmpty()) {
					try (MultiPartRequestContent content = new MultiPartRequestContent()) {
						for (RestPart part : parts) {
							HttpFields.Mutable fields = HttpFields.build();
							for (Entry entry : part.getHeaders()) {
								fields.add(entry.name(), entry.valueString());
							}
							content.addFieldPart(part.getName(), addTaskAndBuildContent(tasks, part), fields);
						}
						request.body(content);
					}
					parts.clear();
				}
			}
			return tasks;
		}

		OutputStreamRequestContent addTaskAndBuildContent(List<Task> tasks, RestBody body) {
			Object actual = body.getActual();
			Type type = body.getType();
			String contentType = body.getContentType();
			boolean base64 = body.isBase64();

			Consumer<OutputStream> consumer;
			if (manager.isBinary(type)) {
				contentType = manager.getAssemblerType(contentType, actual, type);
				Assembler assembler = manager.getAssembler(contentType);
				consumer = (output) -> {
					assembler.write(actual, type, output);
					try {
						output.close();
					} catch (IOException exception) {
						throw new UncheckedIOException(exception);
					}
				};
			} else {
				contentType = manager.getSerializerType(contentType, actual, type);
				Serializer serializer = manager.getSerializer(contentType);
				Charset charset = body.getCharset();
				consumer = (output) -> {
					OutputStreamWriter writer = new OutputStreamWriter(output, charset);
					serializer.write(actual, type, writer);
					try {
						writer.close();
					} catch (IOException exception) {
						throw new UncheckedIOException(exception);
					}
				};
				contentType = "%s;charset=%s".formatted(contentType, charset.name());
			}
			if (base64) {
				contentType = "%s;base64".formatted(contentType);
			}

			OutputStreamRequestContent content = new OutputStreamRequestContent(contentType);
			OutputStream stream = content.getOutputStream();
			if (base64) {
				stream = mediaCoder.encode(stream);
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
				OutputStream output = task.stream();
				consumer.accept(output);
			}

			Response response;
			try {
				response = listener.get(timeout, TimeUnit.SECONDS);
			} catch (ExecutionException exception) {
				throw new ClientException(exception.getCause());
			} catch (TimeoutException exception) {
				throw new ClientException(exception);
			} catch (InterruptedException exception) {
				throw new AssertionError(exception);
			}

			int status = response.getStatus();
			HttpFields fields = response.getHeaders();
			Headers headers = Headers.newInstance(fields);
			String contentType = fields.get("Content-Type");
			InputStream input = listener.getInputStream();
			return RestResponse.newInstance(manager, status, headers, contentType, input);
		}

		record Entry(String name, String valueString) {
		}

		record Task(Consumer<OutputStream> consumer, OutputStream stream) {
		}
	}
}
