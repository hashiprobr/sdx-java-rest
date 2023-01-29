package br.pro.hashi.sdx.rest.fields;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import br.pro.hashi.sdx.rest.fields.exception.FieldsException;

public class Cache {
	private final Map<Class<?>, Function<String, ?>> functions;

	public Cache() {
		this.functions = new HashMap<>(Map.of(
				boolean.class, Boolean::parseBoolean,
				byte.class, Byte::parseByte,
				short.class, Short::parseShort,
				int.class, Integer::parseInt,
				long.class, Long::parseLong,
				float.class, Float::parseFloat,
				double.class, Double::parseDouble,
				BigInteger.class, BigInteger::new,
				BigDecimal.class, BigDecimal::new,
				String.class, (valueString) -> valueString));
	}

	int size() {
		return functions.size();
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Function<String, T> get(Class<T> type) {
		Function<String, T> function = (Function<String, T>) functions.get(type);
		if (function == null) {
			Method method;
			try {
				method = type.getDeclaredMethod("valueOf", String.class);
			} catch (NoSuchMethodException exception) {
				throw new FieldsException("Type must have a valueOf(String) method");
			}
			if (!method.getReturnType().equals(type)) {
				throw new FieldsException("Type valueOf method must return an object of the type");
			}
			int modifiers = method.getModifiers();
			if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))) {
				throw new FieldsException("Type valueOf method must be public and static");
			}
			function = (valueString) -> {
				return invoke(method, valueString);
			};
			functions.put(type, function);
		}
		return function;
	}

	@SuppressWarnings("unchecked")
	private <T> T invoke(Method method, String valueString) {
		T value;
		try {
			value = (T) method.invoke(null, valueString);
		} catch (InvocationTargetException | IllegalAccessException exception) {
			throw new FieldsException(exception);
		}
		return value;
	}
}
