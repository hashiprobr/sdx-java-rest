package br.pro.hashi.sdx.rest;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import br.pro.hashi.sdx.rest.client.RestClientBuilder;
import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.server.RestServerBuilder;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

/**
 * Base class for configuring and building REST clients and servers.
 * 
 * @param <T> the subclass
 */
public sealed abstract class Builder<T extends Builder<T>> permits RestClientBuilder, RestServerBuilder {
	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected final Cache cache;

	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected final Facade facade;

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
	protected Locale locale;

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
		this.cache = new Cache();
		this.facade = new Facade(this.cache);
		this.urlCharset = StandardCharsets.UTF_8;
		this.locale = Coding.LOCALE;
		this.redirection = false;
		this.compression = true;
	}

	/**
	 * <p>
	 * Adds a type that should be considered binary.
	 * </p>
	 * <p>
	 * Since {@link Class<?>} objects do not have generic information due to type
	 * erasure, do not call this method if the type is generic. Call
	 * {@link #withBinary(Hint<?>)} instead.
	 * </p>
	 * <p>
	 * Objects of types considered binary are transformed by an {@link Assembler} or
	 * a {@link Disassembler}, while other objects are transformed by a
	 * {@link Serializer}s or a {@link Deserializer}.
	 * </p>
	 * <p>
	 * The only types considered binary by default are {@code byte[]} and
	 * {@link InputStream}.
	 * </p>
	 * 
	 * @param type the type
	 * @return this builder, for chaining
	 * @throws NullPointerException if the type is null
	 */
	public final T withBinary(Class<?> type) {
		facade.addBinary(type);
		return self();
	}

	/**
	 * <p>
	 * Adds a hinted type that should be considered binary.
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
	 * The only types considered binary by default are {@code byte[]} and
	 * {@link InputStream}.
	 * </p>
	 * 
	 * @param hint the type hint
	 * @return this builder, for chaining
	 * @throws NullPointerException if the type hint is null
	 */
	public final T withBinary(Hint<?> hint) {
		facade.addBinary(hint);
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
	 * @param contentType the content type
	 * @param assembler   the assembler
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the content type is null or the assembler
	 *                                  is null
	 * @throws IllegalArgumentException if the content type is blank
	 */
	public final T withAssembler(String contentType, Assembler assembler) {
		facade.putAssembler(contentType, assembler);
		return self();
	}

	/**
	 * <p>
	 * Associates a content type to a disassembler.
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
		facade.putDisassembler(contentType, disassembler);
		return self();
	}

	/**
	 * <p>
	 * Associates a content type to a serializer.
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
		facade.putSerializer(contentType, serializer);
		return self();
	}

	/**
	 * <p>
	 * Associates a content type to a deserializer.
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
		facade.putDeserializer(contentType, deserializer);
		return self();
	}

	/**
	 * <p>
	 * Sets the fallback content type for objects considered binary.
	 * </p>
	 * <p>
	 * If the content type is not specified, an assembler or disassembler checks if
	 * the object is a {@code byte[]} or an {@link InputStream}. In this case, it
	 * simply uses {@code application/octet-stream}. Otherwise, it uses the fallback
	 * type set by this method.
	 * </p>
	 * 
	 * @param fallbackByteType the fallback type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the fallback type is null
	 * @throws IllegalArgumentException if the fallback type is blank
	 */
	public final T withFallbackByteType(String fallbackByteType) {
		facade.setFallbackByteType(fallbackByteType);
		return self();
	}

	/**
	 * <p>
	 * Sets the fallback content type for objects not considered binary.
	 * </p>
	 * <p>
	 * If the content type is not specified, a serializer or deserializer checks if
	 * the object is a {@code String} or a {@link Reader}. In this case, it simply
	 * uses {@code text/plain}. Otherwise, it uses the fallback type set by this
	 * method.
	 * </p>
	 * 
	 * @param fallbackTextType the fallback type
	 * @return this builder, for chaining
	 * @throws NullPointerException     if the fallback type is null
	 * @throws IllegalArgumentException if the fallback type is blank
	 */
	public final T withFallbackTextType(String fallbackTextType) {
		facade.setFallbackTextType(fallbackTextType);
		return self();
	}

	/**
	 * Sets the charset that should be used when percent-encoding or
	 * percent-decoding an URL. Default is {@link StandardCharsets#UTF_8}.
	 * 
	 * @param urlCharset the charset
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
	 * Enables automatic redirection.
	 * 
	 * @return this builder, for chaining
	 */
	public final T withRedirection() {
		this.redirection = true;
		return self();
	}

	/**
	 * Disables gzip compression.
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
