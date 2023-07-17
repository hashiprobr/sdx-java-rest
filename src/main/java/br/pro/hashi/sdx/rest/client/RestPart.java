package br.pro.hashi.sdx.rest.client;

import java.lang.reflect.Type;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.client.RestClient.Proxy.Entry;
import br.pro.hashi.sdx.rest.coding.MediaCoder;

/**
 * Wraps a multipart request body part.
 */
public class RestPart extends RestBody {
	/**
	 * <p>
	 * Gets a wrapped part.
	 * </p>
	 * <p>
	 * This method calls {@code actual.getClass()} to obtain the part type, so do
	 * not call it if the part is null. Also, since {@code actual.getClass()} loses
	 * generic information due to type erasure, do not call it if the type is
	 * generic. In both cases, call {@code of(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param actual the actual part
	 * @return the wrapped part
	 * @throws NullPointerException     if the part is null
	 * @throws IllegalArgumentException if the part type is generic
	 */
	public static RestPart of(Object actual) {
		if (actual == null) {
			throw new NullPointerException("Part cannot be null");
		}
		Class<?> type = actual.getClass();
		if (type.getTypeParameters().length > 0) {
			throw new IllegalArgumentException("Part type cannot be generic");
		}
		return of(actual, type);
	}

	/**
	 * <p>
	 * Gets a wrapped part with hinted type.
	 * </p>
	 * <p>
	 * Call this method if the part is null or the type is generic.
	 * </p>
	 * 
	 * @param <T>    the part type
	 * @param actual the actual part
	 * @param hint   a {@link Hint} representing {@code T}
	 * @return the wrapped part
	 * @throws NullPointerException if the hint is null
	 */
	public static <T> RestPart of(T actual, Hint<T> hint) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		return of(actual, hint.getType());
	}

	private static RestPart of(Object actual, Type type) {
		MediaCoder coder = MediaCoder.getInstance();
		return new RestPart(coder, actual, type);
	}

	private final CharsetEncoder encoder;
	private final List<Entry> headers;
	private String name;

	RestPart(MediaCoder coder, Object actual, Type type) {
		super(coder, actual, type);
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
	 * @return this part, for chaining
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
	 * Adds a header to this part.
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
	 * @return this part, for chaining
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
