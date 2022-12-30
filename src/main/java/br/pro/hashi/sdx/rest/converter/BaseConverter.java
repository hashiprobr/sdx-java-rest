package br.pro.hashi.sdx.rest.converter;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import br.pro.hashi.sdx.rest.base.Reflection;

/**
 * Converts an object of an arbitrary type {@code T} to an object of a
 * JSON-supported type {@code J} and vice-versa.
 * 
 * @param <J> the JSON-supported type.
 * @param <T> the arbitrary type.
 * @hidden
 */
public abstract class BaseConverter<J, T> {
	/**
	 * Constructs a new base converter.
	 */
	BaseConverter() {
	}

	private JsonElement wrap(J value, JsonSerializationContext context) {
		if (value == null) {
			return JsonNull.INSTANCE;
		}
		return wrapNonNull(value, context);
	}

	abstract JsonElement wrapNonNull(J value, JsonSerializationContext context);

	abstract J unwrapNonNull(JsonElement value, JsonDeserializationContext context);

	/**
	 * Converts an object of type {@code T} to an object of type {@code J}.
	 * 
	 * @param value the original object.
	 * @return the converted object.
	 */
	public abstract J serialize(T value);

	/**
	 * Converts an object of type {@code J} to an object of type {@code T}.
	 * 
	 * @param value the original object.
	 * @return the converted object.
	 */
	public abstract T deserialize(J value);

	/**
	 * Convenience method that instantiates a {@code JsonSerializer} and a
	 * {@code JsonDeserializer} based on this converter and registers them in a
	 * {@code GsonBuilder}.
	 * 
	 * @param builder the {@code GsonBuilder}.
	 */
	public void register(GsonBuilder builder) {
		Type type = Reflection.getSpecificType(BaseConverter.class, this, 1);
		builder.registerTypeAdapter(type, new JsonSerializer<T>() {
			@Override
			public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
				return wrap(BaseConverter.this.serialize(src), context);
			}
		});
		builder.registerTypeAdapter(type, new JsonDeserializer<T>() {
			@Override
			public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
				return BaseConverter.this.deserialize(unwrapNonNull(json, context));
			}
		});
	}
}
