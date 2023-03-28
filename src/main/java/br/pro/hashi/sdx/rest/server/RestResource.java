package br.pro.hashi.sdx.rest.server;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.coding.Media;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Base class for representing REST resources.
 */
public abstract class RestResource {
	private final String base;
	private final boolean nullBase;
	private int status;
	private boolean nullable;
	private String contentType;
	private Charset charset;
	private boolean base64;
	private HttpServletResponse response;
	private CharsetEncoder encoder;

	/**
	 * Maps each part name to its headers. If the request is not multipart, this map
	 * is empty.
	 */
	protected Map<String, List<Fields>> partHeaders;

	/**
	 * The request headers.
	 */
	protected Fields headers;

	/**
	 * The query parameters.
	 */
	protected Fields queries;

	/**
	 * Constructs a new resource with a default base.
	 */
	protected RestResource() {
		this.base = null;
		this.nullBase = true;
		init();
	}

	/**
	 * Constructs a new resource with a specified base.
	 * 
	 * @param base the base
	 */
	protected RestResource(String base) {
		this.base = base;
		this.nullBase = false;
		init();
	}

	private void init() {
		this.status = -1;
		this.nullable = false;
		this.contentType = null;
		this.charset = Coding.CHARSET;
		this.base64 = false;
	}

	String getBase() {
		return base;
	}

	boolean isNullBase() {
		return nullBase;
	}

	int getStatus() {
		return status;
	}

	boolean isNullable() {
		return nullable;
	}

	String getContentType() {
		return contentType;
	}

	Charset getCharset() {
		return charset;
	}

	boolean isBase64() {
		return base64;
	}

	void setFields(Map<String, List<Fields>> partHeaders, Fields headers, Fields queries, CharsetEncoder encoder, HttpServletResponse response) {
		this.partHeaders = partHeaders;
		this.headers = headers;
		this.queries = queries;
		this.encoder = encoder;
		this.response = response;
	}

	/**
	 * Override this method to indicate which extensions are not accepted by this
	 * resource. Default is {@code null}.
	 * 
	 * @implNote The implementation should return {@code null} or a static field to
	 *           maximize efficiency.
	 * 
	 * @return a set with the not accepted extensions
	 */
	protected Set<String> notAcceptableExtensions() {
		return null;
	}

	/**
	 * Wrap the response body with this method to indicate that it should be
	 * serialized even if it is null.
	 * 
	 * @param <T>  the type of the response body
	 * @param body the response body
	 * @return the parameter, for wrapping
	 */
	protected final <T> T nullable(T body) {
		nullable = true;
		return body;
	}

	/**
	 * Alias for {@link #asContentType(String)}.
	 * 
	 * @param contentType the content type
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 * @hidden
	 */
	protected final Void as(String contentType) {
		return asContentType(contentType);
	}

	/**
	 * <p>
	 * Sets the content type for the response body. Parameters are ignored.
	 * </p>
	 * <p>
	 * The alias {@link #as(String)} is available.
	 * </p>
	 * 
	 * @param contentType the content type
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	protected final Void asContentType(String contentType) {
		if (contentType == null) {
			throw new NullPointerException("Content type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Content type cannot be blank");
		}
		this.contentType = contentType;
		return null;
	}

	/**
	 * Alias for {@link #inCharset(Charset)}.
	 * 
	 * @param charset the charset
	 * @throws NullPointerException if the charset is null
	 * @hidden
	 */
	protected final Void in(Charset charset) {
		return inCharset(charset);
	}

	/**
	 * <p>
	 * Sets the charset for the response body. It is ignored if the type is
	 * considered binary.
	 * </p>
	 * <p>
	 * The alias {@link #in(Charset)} is available.
	 * </p>
	 * 
	 * @param charset the charset
	 * @throws NullPointerException if the charset is null
	 */
	protected final Void inCharset(Charset charset) {
		if (charset == null) {
			throw new NullPointerException("Charset cannot be null");
		}
		this.charset = charset;
		return null;
	}

	/**
	 * Encodes the response body in Base64.
	 */
	protected final Void inBase64() {
		this.base64 = true;
		return null;
	}

	/**
	 * Alias for {@link #withHeader(String, Object)}.
	 * 
	 * @param name  the header name
	 * @param value the header value
	 * @throws NullPointerException     if the header name is null or the header
	 *                                  value is null
	 * @throws IllegalArgumentException if the header name is invalid or the header
	 *                                  value is invalid
	 * @hidden
	 */
	protected final Void h(String name, Object value) {
		return withHeader(name, value);
	}

	/**
	 * <p>
	 * Adds a header to the response.
	 * </p>
	 * <p>
	 * The value is converted to {@code String} via {@code toString()} and encoded
	 * in the {@link StandardCharsets#US_ASCII} charset.
	 * </p>
	 * <p>
	 * The alias {@link #h(String, Object)} is available.
	 * </p>
	 * 
	 * @param name  the header name
	 * @param value the header value
	 * @throws NullPointerException     if the header name is null or the header
	 *                                  value is null
	 * @throws IllegalArgumentException if the header name is invalid or the header
	 *                                  value is invalid
	 */
	protected final Void withHeader(String name, Object value) {
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
		response.addHeader(name, valueString);
		return null;
	}

	/**
	 * <p>
	 * Wrap the response body with this method to set the status.
	 * </p>
	 * <p>
	 * The convenience parameter {@code args} can be used to call the other methods.
	 * This ensures that the response and its details are syntactically related and
	 * improves autocomplete accuracy.
	 * </p>
	 * 
	 * @param <T>    the type of the response body
	 * @param status the response status
	 * @param body   the response body
	 * @param args   convenience parameters to call the other methods
	 * @throws IllegalArgumentException if the status is invalid
	 * @return the second parameter, for wrapping
	 */
	protected <T> T response(int status, T body, Void... args) {
		if (status < 100 || status > 399) {
			throw new IllegalArgumentException("Status must be between 100 and 399");
		}
		this.status = status;
		return body;
	}

	/**
	 * <p>
	 * Wrap the response body with this method to construct an exception.
	 * </p>
	 * <p>
	 * The convenience parameter {@code args} can be used to call the other methods.
	 * This ensures that the response and its details are syntactically related and
	 * improves autocomplete accuracy.
	 * </p>
	 * 
	 * @param <T>    the type of the response body
	 * @param status the response status
	 * @param body   the response body
	 * @param args   convenience parameters to call the other methods
	 * @throws IllegalArgumentException if the status is invalid
	 * @return the second parameter, for wrapping
	 */
	protected <T> RestException error(int status, T body, Void... args) {
		return new RestException(status, body);
	}
}
