package br.pro.hashi.sdx.rest.client;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Hint;

/**
 * Wrapper for customizing a body via a fluent interface.
 */
public class Body {
	private static Type getType(Hint<?> hint) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		return hint.getType();
	}

	private final Object actual;
	private final Type type;
	private String name;
	private String contentType;
	private Charset charset;
	private boolean base64;

	/**
	 * <p>
	 * Constructs a wrapped body.
	 * </p>
	 * <p>
	 * This constructor calls {@code body.getClass()} to obtain the body type. Since
	 * {@code body.getClass()} loses generic information due to type erasure, do not
	 * call it if the type is generic. Call {@code Body(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param actual the actual body
	 */
	public Body(Object actual) {
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
	public <T> Body(T actual, Hint<T> hint) {
		this(actual, getType(hint));
	}

	private Body(Object actual, Type type) {
		this.actual = actual;
		this.type = type;
		this.name = "";
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

	String getName() {
		return name;
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
	 * Set the name of this body for a multipart request.
	 * 
	 * @param name the name
	 * @return this body, for chaining
	 * @throws NullPointerException     if the hint is null
	 * @throws IllegalArgumentException if the name is blank
	 */
	public final Body withName(String name) {
		if (name == null) {
			throw new NullPointerException("Name cannot be null");
		}
		name = name.strip();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be blank");
		}
		this.name = name;
		return this;
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
	public final Body as(String contentType) {
		return asContentType(contentType);
	}

	/**
	 * <p>
	 * Set the content type for this body.
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
	public final Body asContentType(String contentType) {
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
	public final Body in(Charset charset) {
		return inCharset(charset);
	}

	/**
	 * <p>
	 * Set the charset for this body.
	 * </p>
	 * <p>
	 * The alias {@link #in(Charset)} is available for short chaining.
	 * </p>
	 * 
	 * @param charset the charset
	 * @return this body, for chaining
	 * @throws NullPointerException if the charset is null
	 */
	public final Body inCharset(Charset charset) {
		if (charset == null) {
			throw new NullPointerException("Charset cannot be null");
		}
		this.charset = charset;
		return this;
	}

	/**
	 * Encode this body in Base64.
	 * 
	 * @return this body, for chaining
	 */
	public final Body inBase64() {
		this.base64 = true;
		return this;
	}
}
