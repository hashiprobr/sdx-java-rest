package br.pro.hashi.sdx.rest.client;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Hint;

/**
 * Wrapper for customizing a request body via a fluent interface.
 */
public class RestBody {
	static Type getType(Hint<?> hint) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		return hint.getType();
	}

	private final Object actual;
	private final Type type;
	private String contentType;
	private Charset charset;
	private boolean base64;

	/**
	 * <p>
	 * Constructs a wrapped body.
	 * </p>
	 * <p>
	 * This constructor calls {@code actual.getClass()} to obtain the body type.
	 * Since {@code actual.getClass()} loses generic information due to type
	 * erasure, do not call it if the type is generic. Call
	 * {@code RestBody(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param actual the actual body
	 */
	public RestBody(Object actual) {
		this(actual, actual.getClass());
	}

	/**
	 * <p>
	 * Constructs a wrapped body with hinted type.
	 * </p>
	 * <p>
	 * Call this constructor if the body type is generic.
	 * </p>
	 * 
	 * @param <T>    the type of the actual body
	 * @param actual the actual body
	 * @param hint   the type hint
	 * @throws NullPointerException if the type hint is null
	 */
	public <T> RestBody(T actual, Hint<T> hint) {
		this(actual, getType(hint));
	}

	RestBody(Object actual, Type type) {
		this.actual = actual;
		this.type = type;
		this.contentType = null;
		this.charset = Coding.CHARSET;
		this.base64 = false;
	}

	Object getActual() {
		return actual;
	}

	Type getType() {
		return type;
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

	/**
	 * Alias for {@link #asContentType(String)}.
	 * 
	 * @param contentType the content type
	 * @return this body, for chaining
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 * @hidden
	 */
	public final RestBody as(String contentType) {
		return asContentType(contentType);
	}

	/**
	 * <p>
	 * Sets the content type for this body. Parameters are ignored.
	 * </p>
	 * <p>
	 * The alias {@link #as(String)} is available for short chaining.
	 * </p>
	 * 
	 * @param contentType the content type
	 * @return this body, for chaining
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final RestBody asContentType(String contentType) {
		if (contentType == null) {
			throw new NullPointerException("Content type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Content type cannot be blank");
		}
		this.contentType = contentType;
		return this;
	}

	/**
	 * Alias for {@link #inCharset(Charset)}.
	 * 
	 * @param charset the charset
	 * @return this body, for chaining
	 * @throws NullPointerException if the charset is null
	 * @hidden
	 */
	public final RestBody in(Charset charset) {
		return inCharset(charset);
	}

	/**
	 * <p>
	 * Sets the charset for this body. It is ignored if the type is considered
	 * binary.
	 * </p>
	 * <p>
	 * The alias {@link #in(Charset)} is available for short chaining.
	 * </p>
	 * 
	 * @param charset the charset
	 * @return this body, for chaining
	 * @throws NullPointerException if the charset is null
	 */
	public final RestBody inCharset(Charset charset) {
		if (charset == null) {
			throw new NullPointerException("Charset cannot be null");
		}
		this.charset = charset;
		return this;
	}

	/**
	 * Encodes this body in Base64.
	 * 
	 * @return this body, for chaining
	 */
	public final RestBody inBase64() {
		this.base64 = true;
		return this;
	}
}
