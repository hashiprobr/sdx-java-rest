package br.pro.hashi.sdx.rest;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import br.pro.hashi.sdx.rest.client.RestClientBuilder;
import br.pro.hashi.sdx.rest.coding.PathCoder;
import br.pro.hashi.sdx.rest.constant.Defaults;
import br.pro.hashi.sdx.rest.server.RestServerBuilder;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

/**
 * Base class for REST client and server builders.
 * 
 * @param <T> the subclass
 */
public sealed abstract class Builder<T extends Builder<T>> permits RestClientBuilder, RestServerBuilder {
	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected final PathCoder pathCoder;

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected final TransformManager managerBase;

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected Locale locale;

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected Charset urlCharset;

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected boolean redirection;

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected boolean compression;

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected Builder() {
		this.pathCoder = PathCoder.getInstance();
		this.managerBase = TransformManager.newInstance();
		this.locale = Defaults.LOCALE;
		this.urlCharset = StandardCharsets.UTF_8;
		this.redirection = true;
		this.compression = true;
	}

	/**
	 * Associates the specified content type to the same assembler associated to
	 * {@code application/octet-stream}. Parameters are ignored.
	 * 
	 * @param contentType the content type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withDefaultAssembler(String contentType) {
		managerBase.putDefaultAssembler(contentType);
		return self();
	}

	/**
	 * <p>
	 * Associates the specified content type to the specified assembler. Parameters
	 * are ignored.
	 * </p>
	 * <p>
	 * The only content type associated to an assembler by default is
	 * {@code application/octet-stream}.
	 * </p>
	 * 
	 * @param contentType the content type
	 * @param assembler   the assembler
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null or the assembler
	 *                                  is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withAssembler(String contentType, Assembler assembler) {
		managerBase.putAssembler(contentType, assembler);
		return self();
	}

	/**
	 * Associates the specified content type to the same disassembler associated to
	 * {@code application/octet-stream}. Parameters are ignored.
	 * 
	 * @param contentType the content type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withDefaultDisassembler(String contentType) {
		managerBase.putDefaultDisassembler(contentType);
		return self();
	}

	/**
	 * <p>
	 * Associates the specified content type to the specified disassembler.
	 * Parameters are ignored.
	 * </p>
	 * <p>
	 * The only content type associated to a disassembler by default is
	 * {@code application/octet-stream}.
	 * </p>
	 * 
	 * @param contentType  the content type
	 * @param disassembler the disassembler
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null or the
	 *                                  disassembler is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withDisassembler(String contentType, Disassembler disassembler) {
		managerBase.putDisassembler(contentType, disassembler);
		return self();
	}

	/**
	 * Associates the specified content type to the same serializer associated to
	 * {@code text/plain}. Parameters are ignored.
	 * 
	 * @param contentType the content type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withDefaultSerializer(String contentType) {
		managerBase.putDefaultSerializer(contentType);
		return self();
	}

	/**
	 * <p>
	 * Associates the specified content type to the specified serializer. Parameters
	 * are ignored.
	 * </p>
	 * <p>
	 * The only content type associated to a serializer by default is
	 * {@code text/plain}.
	 * </p>
	 * 
	 * @param contentType the content type
	 * @param serializer  the serializer
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null or the
	 *                                  serializer is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withSerializer(String contentType, Serializer serializer) {
		managerBase.putSerializer(contentType, serializer);
		return self();
	}

	/**
	 * Associates the specified content type to the same deserializer associated to
	 * {@code text/plain}. Parameters are ignored.
	 * 
	 * @param contentType the content type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withDefaultDeserializer(String contentType) {
		managerBase.putDefaultDeserializer(contentType);
		return self();
	}

	/**
	 * <p>
	 * Associates the specified content type to the specified deserializer.
	 * Parameters are ignored.
	 * </p>
	 * <p>
	 * The only content type associated to a deserializer by default is
	 * {@code text/plain}.
	 * </p>
	 * 
	 * @param contentType  the content type
	 * @param deserializer the deserializer
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null or the
	 *                                  deserializer is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withDeserializer(String contentType, Deserializer deserializer) {
		managerBase.putDeserializer(contentType, deserializer);
		return self();
	}

	/**
	 * <p>
	 * Establishes that the specified type should be considered binary.
	 * </p>
	 * <p>
	 * Since {@link Class} objects do not have generic information due to type
	 * erasure, do not call this method if the type is generic. Call
	 * {@link #withBinary(Hint)} instead.
	 * </p>
	 * <p>
	 * Objects of types considered binary are transformed by an {@link Assembler} or
	 * a {@link Disassembler}, while other objects are transformed by a
	 * {@link Serializer}s or a {@link Deserializer}.
	 * </p>
	 * <p>
	 * The only non-generic types considered binary by default are {@code byte[]}
	 * and {@link InputStream}.
	 * </p>
	 * 
	 * @param type the type
	 * @return this builder, for chaining
	 * @throws NullPointerException if the type is null
	 */
	public final T withBinary(Class<?> type) {
		if (type == null) {
			throw new NullPointerException("Type cannot be null");
		}
		managerBase.addBinary(type);
		return self();
	}

	/**
	 * <p>
	 * Establishes that the specified hinted type should be considered binary.
	 * </p>
	 * <p>
	 * Call this method if the type is generic.
	 * </p>
	 * <p>
	 * Objects of types considered binary are transformed by an {@link Assembler} or
	 * a {@link Disassembler}, while other objects are transformed by a
	 * {@link Serializer}s or a {@link Deserializer}.
	 * </p>
	 * <p>
	 * The only generic type considered binary by default is
	 * {@code Consumer<OutputStream>}.
	 * </p>
	 * 
	 * @param hint the hint
	 * @return this builder, for chaining
	 * @throws NullPointerException if the hint is null
	 */
	public final T withBinary(Hint<?> hint) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		managerBase.addBinary(hint.getType());
		return self();
	}

	/**
	 * <p>
	 * Establishes that the specified content type should be used as a fallback for
	 * types that are considered binary. Parameters are ignored.
	 * </p>
	 * 
	 * @param contentType the content type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withBinaryFallbackType(String contentType) {
		managerBase.setBinaryFallbackType(contentType);
		return self();
	}

	/**
	 * <p>
	 * Establishes that the specified content type should be used as a fallback for
	 * types that are not considered binary. Parameters are ignored.
	 * </p>
	 * 
	 * @param contentType the content type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withFallbackType(String contentType) {
		managerBase.setFallbackType(contentType);
		return self();
	}

	/**
	 * Sets the locale that should be used when processing a request.
	 * 
	 * @param locale the locale
	 * @return this builder, for chaining
	 * @throws NullPointerException if the locale is null
	 */
	public final T withLocale(Locale locale) {
		if (locale == null) {
			throw new NullPointerException("Locale cannot be null");
		}
		this.locale = locale;
		return self();
	}

	/**
	 * Sets the charset that should be used when percent-encoding or
	 * percent-decoding an URL. Default is {@link StandardCharsets#UTF_8}.
	 * 
	 * @param urlCharset the URL charset
	 * @return this builder, for chaining
	 * @throws NullPointerException if the charset is null
	 */
	public final T withUrlCharset(Charset urlCharset) {
		if (urlCharset == null) {
			throw new NullPointerException("URL charset cannot be null");
		}
		this.urlCharset = urlCharset;
		return self();
	}

	/**
	 * Disables HTTPS redirection.
	 * 
	 * @return this builder, for chaining
	 */
	public final T withoutRedirection() {
		this.redirection = false;
		return self();
	}

	/**
	 * Disables GZIP compression.
	 * 
	 * @return this builder, for chaining
	 */
	public final T withoutCompression() {
		this.compression = false;
		return self();
	}

	/**
	 * Internal member.
	 * 
	 * @return this builder, for chaining
	 * @hidden
	 */
	protected abstract T self();
}
