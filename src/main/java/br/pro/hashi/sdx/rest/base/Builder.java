package br.pro.hashi.sdx.rest.base;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.sdx.rest.converter.Converter;
import br.pro.hashi.sdx.rest.converter.base.BaseConverter;
import br.pro.hashi.sdx.rest.transformer.Transformer;
import br.pro.hashi.sdx.rest.transformer.base.Assembler;
import br.pro.hashi.sdx.rest.transformer.base.Deserializer;
import br.pro.hashi.sdx.rest.transformer.base.Disassembler;
import br.pro.hashi.sdx.rest.transformer.base.Serializer;

/**
 * @hidden
 */
public abstract class Builder<T extends Builder<T>> {
	private final Logger logger;

	/**
	 * @hidden
	 */
	protected final Transformer transformer;

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

	protected Builder(Class<?> type) {
		Gson gson = newGsonBuilder().create();
		this.logger = LoggerFactory.getLogger(type);
		this.transformer = new Transformer(gson);
		this.urlCharset = StandardCharsets.UTF_8;
		this.none = null;
		this.redirection = false;
		this.compression = true;
	}

	private GsonBuilder newGsonBuilder() {
		return new GsonBuilder()
				.disableJdkUnsafe()
				.disableHtmlEscaping()
				.serializeNulls()
				.setPrettyPrinting();
	}

	private Gson newGson(String packageName) {
		if (packageName == null) {
			throw new IllegalArgumentException("Package name cannot be null");
		}
		packageName = packageName.strip();
		if (packageName.isEmpty()) {
			throw new IllegalArgumentException("Package name cannot be blank");
		}
		GsonBuilder builder = newGsonBuilder();
		for (BaseConverter<?, ?> converter : Reflection.getSubInstances(packageName, BaseConverter.class)) {
			String name = converter.getClass().getName();
			converter.register(builder);
			logger.info("Registered %s".formatted(name));
		}
		return builder.create();
	}

	protected abstract T self();

	/**
	 * @hidden
	 */
	public Transformer getTransformer() {
		return transformer;
	}

	/**
	 * @hidden
	 */
	public Charset getURLCharset() {
		return urlCharset;
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
	public T withBinary(Class<?>... types) {
		for (Class<?> type : types) {
			transformer.addBinary(type);
		}
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
	public T withAssembler(String contentType, Assembler assembler) {
		transformer.putAssembler(contentType, assembler);
		return self();
	}

	/**
	 * <p>
	 * Disassociates a content type of its assembler.
	 * </p>
	 * <p>
	 * The only content type associated to an assembler by default is
	 * {@code application/octet-stream}.
	 * </p>
	 * 
	 * @param contentType the content type.
	 * @return this builder, for chaining.
	 */
	public T withoutAssembler(String contentType) {
		transformer.removeAssembler(contentType);
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
	public T withDisassembler(String contentType, Disassembler disassembler) {
		transformer.putDisassembler(contentType, disassembler);
		return self();
	}

	/**
	 * <p>
	 * Disassociates a content type of its disassembler.
	 * </p>
	 * <p>
	 * The only content type associated to an disassembler by default is
	 * {@code application/octet-stream}.
	 * </p>
	 * 
	 * @param contentType the content type.
	 * @return this builder, for chaining.
	 */
	public T withoutDisassembler(String contentType) {
		transformer.removeDisassembler(contentType);
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
	public T withSerializer(String contentType, Serializer serializer) {
		transformer.putSerializer(contentType, serializer);
		return self();
	}

	/**
	 * <p>
	 * Convenience method that associates the content type {@code application/json}
	 * to a serializer based on {@code Gson}.
	 * </p>
	 * 
	 * @param gson the {@code Gson}.
	 * @return this builder, for chaining.
	 */
	public T withSerializer(Gson gson) {
		transformer.putSerializer(gson);
		return self();
	}

	/**
	 * <p>
	 * Convenience method that creates a {@code Gson}, registers {@link Converter}s
	 * in this {@code Gson}, and associates the content type
	 * {@code application/json} to a serializer based on it.
	 * </p>
	 * 
	 * @param packageName the name of the package containing the {@link Converter}
	 *                    classes.
	 * @return this builder, for chaining.
	 */
	public T withSerializer(String packageName) {
		Gson gson = newGson(packageName);
		transformer.putUncheckedSerializer(gson);
		return self();
	}

	/**
	 * <p>
	 * Disassociates a content type of its serializer.
	 * </p>
	 * <p>
	 * The content types associated to serializers by default are {@code text/plain}
	 * and {@code application/json}.
	 * </p>
	 * 
	 * @param contentType the content type.
	 * @return this builder, for chaining.
	 */
	public T withoutSerializer(String contentType) {
		transformer.removeSerializer(contentType);
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
	public T withDeserializer(String contentType, Deserializer deserializer) {
		transformer.putDeserializer(contentType, deserializer);
		return self();
	}

	/**
	 * <p>
	 * Convenience method that associates the content type {@code application/json}
	 * to a deserializer based on {@code Gson}.
	 * </p>
	 * 
	 * @param gson the {@code Gson}.
	 * @return this builder, for chaining.
	 */
	public T withDeserializer(Gson gson) {
		transformer.putDeserializer(gson);
		return self();
	}

	/**
	 * <p>
	 * Convenience method that creates a {@code Gson}, registers {@link Converter}s
	 * in this {@code Gson}, and associates the content type
	 * {@code application/json} to a deserializer based on it.
	 * </p>
	 * 
	 * @param packageName the name of the package containing the {@link Converter}
	 *                    classes.
	 * @return this builder, for chaining.
	 */
	public T withDeserializer(String packageName) {
		Gson gson = newGson(packageName);
		transformer.putSafeDeserializer(gson);
		return self();
	}

	/**
	 * <p>
	 * Disassociates a content type of its deserializer.
	 * </p>
	 * <p>
	 * The content types associated to deserializers by default are
	 * {@code text/plain} and {@code application/json}.
	 * </p>
	 * 
	 * @param contentType the content type.
	 * @return this builder, for chaining.
	 */
	public T withoutDeserializer(String contentType) {
		transformer.removeDeserializer(contentType);
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
	public T withURLCharset(Charset urlCharset) {
		if (urlCharset == null) {
			throw new IllegalArgumentException("URL charset cannot be null");
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
	public T withNullBody() {
		this.none = "";
		return self();
	}

	/**
	 * Enables redirection.
	 * 
	 * @return this builder, for chaining.
	 */
	public T withRedirection() {
		this.redirection = true;
		return self();
	}

	/**
	 * Disables compression.
	 * 
	 * @return this builder, for chaining.
	 */
	public T withoutCompression() {
		this.compression = false;
		return self();
	}
}
