package br.pro.hashi.sdx.rest.fields;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
				String.class, (valueString) -> valueString));
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Function<String, T> get(Class<T> type) {
		Function<String, T> function = (Function<String, T>) functions.get(type);
		if (function == null) {
			Method method;
			try {
				method = type.getDeclaredMethod("valueOf", String.class);
			} catch (NoSuchMethodException exception) {
				return null;
			}
			int modifiers = method.getModifiers();
			if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))) {

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
		} catch (InvocationTargetException exception) {
			return null;
		} catch (IllegalAccessException exception) {
			return null;
		}
		return value;
	}
}
