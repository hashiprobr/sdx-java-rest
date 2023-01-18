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
	private final Object actual;
	private final String name;
	private Type type;
	private String contentType;
	private Charset charset;
	private boolean base64;

	/**
	 * Constructs a wrapped body.
	 * 
	 * @param actual the actual body
	 */
	public Body(Object actual) {
		this("", actual);
	}

	/**
	 * Constructs a named wrapped body for a multipart request.
	 * 
	 * @param actual the actual body
	 * @param name   the part name
	 * @throws NullPointerException     if the part name is null
	 * @throws IllegalArgumentException if the part name is blank
	 */
	public Body(String name, Object actual) {
		if (name == null) {
			throw new NullPointerException("Part name cannot be null");
		}
		name = name.strip();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Part name cannot be blank");
		}
		this.actual = actual;
		this.name = name;
		this.type = actual.getClass();
		this.contentType = null;
		this.charset = Coding.CHARSET;
		this.base64 = false;
	}

	Object getActual() {
		return actual;
	}

	String getName() {
		return name;
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
	 * Hint the type of this body for assembling or serializing.
	 * 
	 * @param hint the hint
	 * @return this body, for chaining
	 * @throws NullPointerException if the hint is null
	 */
	public final Body is(Hint<?> hint) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		this.type = hint.getType();
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
