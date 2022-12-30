package br.pro.hashi.sdx.rest.converter;

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

import br.pro.hashi.sdx.rest.base.Reflection;

/**
 * Provides convenience nested classes for extending type support in Gson.
 */
public final class Converter {
	private static abstract class ToNumber<J extends Number, T> extends BaseConverter<J, T> {
		@Override
		JsonElement wrapNonNull(J value, JsonSerializationContext context) {
			return new JsonPrimitive(value);
		}
	}

	/**
	 * Converts an object of type {@code T} to a {@link Boolean} and vice-versa.
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
		JsonElement wrapNonNull(Boolean value, JsonSerializationContext context) {
			return new JsonPrimitive(value);
		}

		@Override
		Boolean unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsBoolean();
		}

		/**
		 * Converts an object of type {@code T} to a {@link Boolean}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link Boolean}.
		 */
		@Override
		public abstract Boolean serialize(T value);

		/**
		 * Converts a {@link Boolean} to an object of type {@code T}.
		 * 
		 * @param value the {@link Boolean}.
		 * @return the {@link Boolean} converted to an object.
		 */
		@Override
		public abstract T deserialize(Boolean value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link Byte} and vice-versa.
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
		Byte unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsByte();
		}

		/**
		 * Converts an object of type {@code T} to a {@link Byte}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link Byte}.
		 */
		@Override
		public abstract Byte serialize(T value);

		/**
		 * Converts a {@link Byte} to an object of type {@code T}.
		 * 
		 * @param value the {@link Byte}.
		 * @return the {@link Byte} converted to an object.
		 */
		@Override
		public abstract T deserialize(Byte value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link Short} and vice-versa.
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
		Short unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsShort();
		}

		/**
		 * Converts an object of type {@code T} to a {@link Short}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link Short}.
		 */
		@Override
		public abstract Short serialize(T value);

		/**
		 * Converts a {@link Short} to an object of type {@code T}.
		 * 
		 * @param value the {@link Short}.
		 * @return the {@link Short} converted to an object.
		 */
		@Override
		public abstract T deserialize(Short value);
	}

	/**
	 * Converts an object of type {@code T} to an {@link Integer} and vice-versa.
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
		Integer unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsInt();
		}

		/**
		 * Converts an object of type {@code T} to a {@link Integer}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link Integer}.
		 */
		@Override
		public abstract Integer serialize(T value);

		/**
		 * Converts a {@link Integer} to an object of type {@code T}.
		 * 
		 * @param value the {@link Integer}.
		 * @return the {@link Integer} converted to an object.
		 */
		@Override
		public abstract T deserialize(Integer value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link Long} and vice-versa.
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
		Long unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsLong();
		}

		/**
		 * Converts an object of type {@code T} to a {@link Long}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link Long}.
		 */
		@Override
		public abstract Long serialize(T value);

		/**
		 * Converts a {@link Long} to an object of type {@code T}.
		 * 
		 * @param value the {@link Long}.
		 * @return the {@link Long} converted to an object.
		 */
		@Override
		public abstract T deserialize(Long value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link Float} and vice-versa.
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
		Float unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsFloat();
		}

		/**
		 * Converts an object of type {@code T} to a {@link Float}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link Float}.
		 */
		@Override
		public abstract Float serialize(T value);

		/**
		 * Converts a {@link Float} to an object of type {@code T}.
		 * 
		 * @param value the {@link Float}.
		 * @return the {@link Float} converted to an object.
		 */
		@Override
		public abstract T deserialize(Float value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link Double} and vice-versa.
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
		Double unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsDouble();
		}

		/**
		 * Converts an object of type {@code T} to a {@link Double}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link Double}.
		 */
		@Override
		public abstract Double serialize(T value);

		/**
		 * Converts a {@link Double} to an object of type {@code T}.
		 * 
		 * @param value the {@link Double}.
		 * @return the {@link Double} converted to an object.
		 */
		@Override
		public abstract T deserialize(Double value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link BigInteger} and vice-versa.
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
		BigInteger unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsBigInteger();
		}

		/**
		 * Converts an object of type {@code T} to a {@link BigInteger}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link BigInteger}.
		 */
		@Override
		public abstract BigInteger serialize(T value);

		/**
		 * Converts a {@link BigInteger} to an object of type {@code T}.
		 * 
		 * @param value the {@link BigInteger}.
		 * @return the {@link BigInteger} converted to an object.
		 */
		@Override
		public abstract T deserialize(BigInteger value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link BigDecimal} and vice-versa.
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
		BigDecimal unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsBigDecimal();
		}

		/**
		 * Converts an object of type {@code T} to a {@link BigDecimal}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link BigDecimal}.
		 */
		@Override
		public abstract BigDecimal serialize(T value);

		/**
		 * Converts a {@link BigDecimal} to an object of type {@code T}.
		 * 
		 * @param value the {@link BigDecimal}.
		 * @return the {@link BigDecimal} converted to an object.
		 */
		@Override
		public abstract T deserialize(BigDecimal value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link String} and vice-versa.
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
		JsonElement wrapNonNull(String value, JsonSerializationContext context) {
			return new JsonPrimitive(value);
		}

		@Override
		String unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsString();
		}

		/**
		 * Converts an object of type {@code T} to a {@link String}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link String}.
		 */
		@Override
		public abstract String serialize(T value);

		/**
		 * Converts a {@link String} to an object of type {@code T}.
		 * 
		 * @param value the {@link String}.
		 * @return the {@link String} converted to an object.
		 */
		@Override
		public abstract T deserialize(String value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link List<E>} and vice-versa.
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
		JsonElement wrapNonNull(List<E> value, JsonSerializationContext context) {
			JsonArray array = new JsonArray();
			for (E element : value) {
				array.add(context.serialize(element));
			}
			return array;
		}

		@Override
		List<E> unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			List<E> list = new ArrayList<>();
			for (JsonElement element : value.getAsJsonArray()) {
				list.add(context.deserialize(element, type));
			}
			return list;
		}

		/**
		 * Converts an object of type {@code T} to a {@link List<E>}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link List<E>}.
		 */
		@Override
		public abstract List<E> serialize(T value);

		/**
		 * Converts a {@link List<E>} to an object of type {@code T}.
		 * 
		 * @param value the {@link List<E>}.
		 * @return the {@link List<E>} converted to an object.
		 */
		@Override
		public abstract T deserialize(List<E> value);
	}

	/**
	 * Converts an object of type {@code T} to a {@link Map<String, V>} and
	 * vice-versa.
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
		JsonElement wrapNonNull(Map<String, V> value, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			for (String key : value.keySet()) {
				object.add(key, context.serialize(value.get(key)));
			}
			return object;
		}

		@Override
		Map<String, V> unwrapNonNull(JsonElement value, JsonDeserializationContext context) {
			Map<String, V> map = new LinkedHashMap<>();
			JsonObject object = value.getAsJsonObject();
			for (String key : object.keySet()) {
				map.put(key, context.deserialize(object.get(key), type));
			}
			return map;
		}

		/**
		 * Converts an object of type {@code T} to a {@link Map<String, V>}.
		 * 
		 * @param value the object.
		 * @return the object converted to a {@link Map<String, V>}.
		 */
		@Override
		public abstract Map<String, V> serialize(T value);

		/**
		 * Converts a {@link Map<String, V>} to an object of type T.
		 * 
		 * @param value the {@link Map<String, V>}.
		 * @return the {@link Map<String, V>} converted to an object.
		 */
		@Override
		public abstract T deserialize(Map<String, V> value);
	}

	private Converter() {
	}
}
