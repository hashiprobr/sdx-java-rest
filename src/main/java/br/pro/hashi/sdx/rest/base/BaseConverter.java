package br.pro.hashi.sdx.rest.base;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public abstract class BaseConverter<J, T> {
	private JsonElement wrap(J value, JsonSerializationContext context) {
		if (value == null) {
			return JsonNull.INSTANCE;
		}
		return wrapNonNull(value, context);
	}

	/**
	 * Wraps an object with a {@code JsonElement}.
	 * 
	 * @param value   the non-null object that needs to be wrapped by a
	 *                {@code JsonElement}.
	 * @param context the context for serialization.
	 * @return a {@code JsonElement} wrapping the object.
	 */
	protected abstract JsonElement wrapNonNull(J value, JsonSerializationContext context);

	/**
	 * Unwraps the object in a {@code JsonElement}.
	 * 
	 * @param value   the non-null {@code JsonElement} being deserialized.
	 * @param context the context for deserialization.
	 * @return the object wrapped by the {@code JsonElement}.
	 */
	protected abstract J unwrapNonNull(JsonElement value, JsonDeserializationContext context);

	protected abstract J serialize(T value);

	protected abstract T deserialize(J value);

	public void beRegisteredBy(GsonBuilder builder) {
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
