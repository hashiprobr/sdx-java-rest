package br.pro.hashi.sdx.rest.client;

import java.lang.reflect.Type;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.client.RestClient.Proxy.Entry;

/**
 * Wrapper for customizing a multipart request body part via a fluent interface.
 */
public class RestPart extends RestBody {
	private final CharsetEncoder encoder;
	private final List<Entry> headers;
	private String name;

	/**
	 * <p>
	 * Constructs a wrapped part.
	 * </p>
	 * <p>
	 * This constructor calls {@code actual.getClass()} to obtain the part type.
	 * Since {@code actual.getClass()} loses generic information due to type
	 * erasure, do not call it if the type is generic. Call
	 * {@code RestPart(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param actual the actual part
	 */
	public RestPart(Object actual) {
		this(actual, actual == null ? Object.class : actual.getClass());
	}

	/**
	 * <p>
	 * Constructs a wrapped part with hinted type.
	 * </p>
	 * <p>
	 * Call this constructor if the part type is generic.
	 * </p>
	 * 
	 * @param <T>    the type of the actual part
	 * @param actual the actual part
	 * @param hint   the type hint
	 * @throws NullPointerException if the type hint is null
	 */
	public <T> RestPart(T actual, Hint<T> hint) {
		this(actual, getType(hint));
	}

	private RestPart(Object actual, Type type) {
		super(actual, type);
		this.encoder = StandardCharsets.US_ASCII.newEncoder();
		this.headers = new ArrayList<>();
		this.name = null;
	}

	List<Entry> getHeaders() {
		return headers;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
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
	public final RestPart h(String name, Object value) {
		return withHeader(name, value);
	}

	/**
	 * <p>
	 * Adds a header to the part.
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
	 * @throws IllegalArgumentException if the header name is invalid or the header
	 *                                  value is invalid
	 */
	public final RestPart withHeader(String name, Object value) {
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
}
