package br.pro.hashi.sdx.rest.converter.base;

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
 * @hidden
 */
public abstract class BaseConverter<J, T> {
	private JsonElement wrap(J value, JsonSerializationContext context) {
		if (value == null) {
			return JsonNull.INSTANCE;
		}
		return wrapNonNull(value, context);
	}

	protected abstract JsonElement wrapNonNull(J value, JsonSerializationContext context);

	protected abstract J unwrapNonNull(JsonElement value, JsonDeserializationContext context);

	/**
	 * @hidden
	 */
	public abstract J serialize(T value);

	/**
	 * @hidden
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
