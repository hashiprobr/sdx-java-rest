package br.pro.hashi.sdx.rest.base;

import java.io.InputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.sdx.rest.converter.BaseConverter;
import br.pro.hashi.sdx.rest.converter.Converter;
import br.pro.hashi.sdx.rest.transformer.Transformer;
import br.pro.hashi.sdx.rest.transformer.base.Assembler;
import br.pro.hashi.sdx.rest.transformer.base.Deserializer;
import br.pro.hashi.sdx.rest.transformer.base.Disassembler;
import br.pro.hashi.sdx.rest.transformer.base.Serializer;

/**
 * @hidden
 */
public abstract class Builder<T extends Builder<T>> {
	private Transformer transformer;

	protected Builder() {
		Gson gson = newGsonBuilder().create();
		this.transformer = new Transformer(gson);
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
			converter.register(builder);
		}
		return builder.create();
	}

	protected abstract T self();

	/**
	 * <p>
	 * Adds types that are considered binary.
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
}
