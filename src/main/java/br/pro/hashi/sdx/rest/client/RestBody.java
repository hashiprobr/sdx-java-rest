package br.pro.hashi.sdx.rest.client;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.constant.Defaults;

/**
 * Wraps a request body.
 */
public class RestBody {
	/**
	 * <p>
	 * Gets a wrapped body.
	 * </p>
	 * <p>
	 * This method calls {@code actual.getClass()} to obtain the body type, so do
	 * not call it if the body is null. Also, since {@code actual.getClass()} loses
	 * generic information due to type erasure, do not call it if the type is
	 * generic. In both cases, call {@code of(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param actual the actual body
	 * @return the wrapped body
	 * @throws NullPointerException     if the body is null
	 * @throws IllegalArgumentException if the body type is generic
	 */
	public static RestBody of(Object actual) {
		if (actual == null) {
			throw new NullPointerException("Body cannot be null");
		}
		Class<?> type = actual.getClass();
		if (type.getTypeParameters().length > 0) {
			throw new IllegalArgumentException("Body type cannot be generic");
		}
		return of(actual, type);
	}

	/**
	 * <p>
	 * Gets a wrapped body with hinted type.
	 * </p>
	 * <p>
	 * Call this method if the body is null or the type is generic.
	 * </p>
	 * 
	 * @param <T>    the body type
	 * @param actual the actual body
	 * @param hint   a {@link Hint} representing {@code T}
	 * @return the wrapped body
	 * @throws NullPointerException if the hint is null
	 */
	public static <T> RestBody of(T actual, Hint<T> hint) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		return of(actual, hint.getType());
	}

	private static RestBody of(Object actual, Type type) {
		MediaCoder coder = MediaCoder.getInstance();
		return new RestBody(coder, actual, type);
	}

	private final MediaCoder coder;
	private final Object actual;
	private final Type type;
	private String contentType;
	private Charset charset;
	private boolean base64;

	RestBody(MediaCoder coder, Object actual, Type type) {
		this.coder = coder;
		this.actual = actual;
		this.type = type;
		this.contentType = null;
		this.charset = Defaults.CHARSET;
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
		contentType = coder.strip(contentType);
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
