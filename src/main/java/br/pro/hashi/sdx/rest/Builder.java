package br.pro.hashi.sdx.rest;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Serializer;

/**
 * @hidden
 */
public abstract class Builder<T extends Builder<T>> {
	/**
	 * @hidden
	 */
	protected final Facade facade;

	/**
	 * @hidden
	 */
	protected Charset urlCharset;

	/**
	 * @hidden
	 */
	protected String none;

	/**
	 * @hidden
	 */
	protected boolean redirection;

	/**
	 * @hidden
	 */
	protected boolean compression;

	/**
	 * @hidden
	 */
	protected Builder() {
		this.facade = new Facade();
		this.urlCharset = StandardCharsets.UTF_8;
		this.none = null;
		this.redirection = false;
		this.compression = true;
	}

	/**
	 * <p>
	 * Adds types that should be considered binary.
	 * </p>
	 * <p>
	 * Objects of types considered binary are transformed by {@link Assembler}s and
	 * {@link Disassembler}s, while other objects are transformed by
	 * {@link Serializer}s and {@link Deserializer}s.
	 * </p>
	 * <p>
	 * The only type considered binary by default is {@link InputStream}.
	 * </p>
	 * 
	 * @param types the types.
	 * @return this builder, for chaining.
	 */
	public final T withBinary(Class<?> type) {
		facade.addBinary(type);
		return self();
	}

	/**
	 * <p>
	 * Associates a content type to an assembler.
	 * </p>
	 * <p>
	 * The only content type associated to an assembler by default is
	 * {@code application/octet-stream}.
	 * </p>
	 * 
	 * @param contentType the content type.
	 * @param assembler   the assembler.
	 * @return this builder, for chaining.
	 */
	public final T withAssembler(String contentType, Assembler assembler) {
		facade.putAssembler(contentType, assembler);
		return self();
	}

	/**
	 * <p>
	 * Associates a content type to an disassembler.
	 * </p>
	 * <p>
	 * The only content type associated to an disassembler by default is
	 * {@code application/octet-stream}.
	 * </p>
	 * 
	 * @param contentType  the content type.
	 * @param disassembler the disassembler.
	 * @return this builder, for chaining.
	 */
	public final T withDisassembler(String contentType, Disassembler disassembler) {
		facade.putDisassembler(contentType, disassembler);
		return self();
	}

	/**
	 * <p>
	 * Associates a content type to a serializer.
	 * </p>
	 * <p>
	 * The content types associated to serializers by default are {@code text/plain}
	 * and {@code application/json}.
	 * </p>
	 * 
	 * @param contentType the content type.
	 * @param serializer  the serializer.
	 * @return this builder, for chaining.
	 */
	public final T withSerializer(String contentType, Serializer serializer) {
		facade.putSerializer(contentType, serializer);
		return self();
	}

	/**
	 * <p>
	 * Associates a content type to a deserializer.
	 * </p>
	 * <p>
	 * The content types associated to deserializers by default are
	 * {@code text/plain} and {@code application/json}.
	 * </p>
	 * 
	 * @param contentType  the content type.
	 * @param deserializer the deserializer.
	 * @return this builder, for chaining.
	 */
	public final T withDeserializer(String contentType, Deserializer deserializer) {
		facade.putDeserializer(contentType, deserializer);
		return self();
	}

	/**
	 * <p>
	 * Sets the charset that should be used when percent-encoding or
	 * percent-decoding an URL.
	 * </p>
	 * <p>
	 * The default value is {@link StandardCharsets#UTF_8}.
	 * </p>
	 * 
	 * @param urlCharset the URL charset.
	 * @return this builder, for chaining.
	 */
	public final T withUrlCharset(Charset urlCharset) {
		if (urlCharset == null) {
			throw new NullPointerException("URL charset cannot be null");
		}
		this.urlCharset = urlCharset;
		return self();
	}

	/**
	 * <p>
	 * Establishes that {@code null} is a valid body that should be serialized and
	 * {@code ""} is an empty body.
	 * </p>
	 * <p>
	 * The default behavior is considering {@code ""} as a valid body that should be
	 * serialized and {@code null} as an empty body.
	 * </p>
	 * 
	 * @return this builder, for chaining.
	 */
	public final T withNullBody() {
		this.none = "";
		return self();
	}

	/**
	 * Enables redirection.
	 * 
	 * @return this builder, for chaining.
	 */
	public final T withRedirection() {
		this.redirection = true;
		return self();
	}

	/**
	 * Disables compression.
	 * 
	 * @return this builder, for chaining.
	 */
	public final T withoutCompression() {
		this.compression = false;
		return self();
	}

	/**
	 * @hidden
	 */
	protected abstract T self();
}
