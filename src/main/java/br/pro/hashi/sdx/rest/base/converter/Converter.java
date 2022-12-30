package br.pro.hashi.sdx.rest.base.converter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import br.pro.hashi.sdx.rest.base.BaseConverter;
import br.pro.hashi.sdx.rest.base.Reflection;

/**
 * Provides convenience nested classes for extending type support in Gson.
 */
public final class Converter {
	private static abstract class ToNumber<J extends Number, T> extends BaseConverter<J, T> {
		@Override
		protected final JsonElement wrapNonNull(J value, JsonSerializationContext context) {
			return new JsonPrimitive(value);
		}
	}

	/**
	 * Converts an object to a {@link Boolean}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToBoolean<T> extends BaseConverter<Boolean, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToBoolean() {
		}

		@Override
		protected final JsonElement wrapNonNull(Boolean value, JsonSerializationContext context) {
			return new JsonPrimitive(value);
		}

		@Override
		protected final Boolean unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsBoolean();
		}
	}

	/**
	 * Converts an object to a {@link Byte}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToByte<T> extends ToNumber<Byte, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToByte() {
		}

		@Override
		protected final Byte unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsByte();
		}
	}

	/**
	 * Converts an object to a {@link Short}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToShort<T> extends ToNumber<Short, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToShort() {
		}

		@Override
		protected final Short unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsShort();
		}
	}

	/**
	 * Converts an object to a {@link Integer}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToInteger<T> extends ToNumber<Integer, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToInteger() {
		}

		@Override
		protected final Integer unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsInt();
		}
	}

	/**
	 * Converts an object to a {@link Long}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToLong<T> extends ToNumber<Long, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToLong() {
		}

		@Override
		protected final Long unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsLong();
		}
	}

	/**
	 * Converts an object to a {@link Float}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToFloat<T> extends ToNumber<Float, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToFloat() {
		}

		@Override
		protected final Float unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsFloat();
		}
	}

	/**
	 * Converts an object to a {@link Double}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToDouble<T> extends ToNumber<Double, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToDouble() {
		}

		@Override
		protected final Double unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsDouble();
		}
	}

	/**
	 * Converts an object to a {@link BigInteger}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToBigInteger<T> extends ToNumber<BigInteger, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToBigInteger() {
		}

		@Override
		protected final BigInteger unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsBigInteger();
		}
	}

	/**
	 * Converts an object to a {@link BigDecimal}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToBigDecimal<T> extends ToNumber<BigDecimal, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToBigDecimal() {
		}

		@Override
		protected final BigDecimal unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsBigDecimal();
		}
	}

	/**
	 * Converts an object to a {@link String}.
	 * 
	 * @param <T> the type of the object.
	 */
	public static abstract class ToString<T> extends BaseConverter<String, T> {
		/**
		 * Constructs a new converter.
		 */
		protected ToString() {
		}

		@Override
		protected final JsonElement wrapNonNull(String value, JsonSerializationContext context) {
			return new JsonPrimitive(value);
		}

		@Override
		protected final String unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsString();
		}
	}

	/**
	 * Converts an object to a {@link List}.
	 * 
	 * @param <E> the type of the list elements.
	 * @param <T> the type of the object.
	 */
	public static abstract class ToListOf<E, T> extends BaseConverter<List<E>, T> {
		private Type type;

		/**
		 * Constructs a new converter.
		 */
		protected ToListOf() {
			this.type = Reflection.getSpecificType(ToListOf.class, this, 0);
		}

		@Override
		protected final JsonElement wrapNonNull(List<E> value, JsonSerializationContext context) {
			JsonArray array = new JsonArray();
			for (E element : value) {
				array.add(context.serialize(element));
			}
			return array;
		}

		@Override
		protected final List<E> unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			List<E> list = new ArrayList<>();
			for (JsonElement element : value.getAsJsonArray()) {
				list.add(context.deserialize(element, type));
			}
			return list;
		}
	}

	/**
	 * Converts an object to a {@link Map} with {@link String} keys.
	 * 
	 * @param <V> the type of the map values.
	 * @param <T> the type of the object.
	 */
	public static abstract class ToMapOf<V, T> extends BaseConverter<Map<String, V>, T> {
		private Type type;

		/**
		 * Constructs a new converter.
		 */
		protected ToMapOf() {
			this.type = Reflection.getSpecificType(ToMapOf.class, this, 0);
		}

		@Override
		protected final JsonElement wrapNonNull(Map<String, V> value, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			for (String key : value.keySet()) {
				object.add(key, context.serialize(value.get(key)));
			}
			return object;
		}

		@Override
		protected final Map<String, V> unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			Map<String, V> map = new LinkedHashMap<>();
			JsonObject object = value.getAsJsonObject();
			for (String key : object.keySet()) {
				map.put(key, context.deserialize(object.get(key), type));
			}
			return map;
		}
	}

	private Converter() {
	}
}
