package br.pro.hashi.sdx.rest.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;

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

	Map<Class<?>, Function<String, ?>> getFunctions() {
		return functions;
	}

	public synchronized <T> Function<String, T> get(Class<T> type) {
		@SuppressWarnings("unchecked")
		Function<String, T> function = (Function<String, T>) functions.get(type);
		if (function == null) {
			Method method;
			try {
				method = type.getDeclaredMethod("valueOf", String.class);
			} catch (NoSuchMethodException exception) {
				throw new ReflectionException("Type must have a valueOf(String) method");
			}
			if (!method.getReturnType().equals(type)) {
				throw new ReflectionException("Type valueOf method must return an object of the type");
			}
			int modifiers = method.getModifiers();
			if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))) {
				throw new ReflectionException("Type valueOf method must be public and static");
			}
			for (Class<?> exceptionType : method.getExceptionTypes()) {
				if (!RuntimeException.class.isAssignableFrom(exceptionType)) {
					throw new ReflectionException("Type valueOf method can only throw unchecked exceptions");
				}
			}
			MethodHandle handle = unreflect(method);
			function = (valueString) -> {
				return invoke(handle, valueString);
			};
			functions.put(type, function);
		}
		return function;
	}

	MethodHandle unreflect(Method method) {
		MethodHandle handle;
		try {
			handle = Reflection.LOOKUP.unreflect(method);
		} catch (IllegalAccessException exception) {
			throw new AssertionError(exception);
		}
		return handle;
	}

	<T> T invoke(MethodHandle handle, String valueString) {
		T value;
		try {
			value = (T) handle.invoke(valueString);
		} catch (Throwable exception) {
			if (exception instanceof RuntimeException) {
				throw (RuntimeException) exception;
			}
			throw new AssertionError(exception);
		}
		return value;
	}
}
