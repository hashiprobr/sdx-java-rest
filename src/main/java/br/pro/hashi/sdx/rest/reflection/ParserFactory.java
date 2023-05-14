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

public class ParserFactory {
	private static final ParserFactory INSTANCE = newInstance();

	private static ParserFactory newInstance() {
		Reflector reflector = Reflector.getInstance();
		return new ParserFactory(reflector);
	}

	public static ParserFactory getInstance() {
		return INSTANCE;
	}

	private final Reflector reflector;
	private final Map<Class<?>, Function<String, ?>> cache;

	ParserFactory(Reflector reflector) {
		this.reflector = reflector;
		this.cache = new HashMap<>(Map.of(
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

	public synchronized <T> Function<String, T> get(Class<T> type) {
		@SuppressWarnings("unchecked")
		Function<String, T> parser = (Function<String, T>) cache.get(type);
		if (parser == null) {
			String typeName = type.getName();
			Method method;
			try {
				method = type.getDeclaredMethod("valueOf", String.class);
			} catch (NoSuchMethodException exception) {
				throw new ReflectionException("Class %s must have a valueOf(String) method".formatted(typeName));
			}
			if (!method.getReturnType().equals(type)) {
				throw new ReflectionException("Method valueOf(String) of class %s must return an instance of this class".formatted(typeName));
			}
			int modifiers = method.getModifiers();
			if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))) {
				throw new ReflectionException("Method valueOf(String) of class %s must be public and static".formatted(typeName));
			}
			for (Class<?> exceptionType : method.getExceptionTypes()) {
				if (!RuntimeException.class.isAssignableFrom(exceptionType)) {
					throw new ReflectionException("Method valueOf(String) of class %s can only throw unchecked exceptions".formatted(typeName));
				}
			}
			MethodHandle handle = reflector.unreflect(method);
			parser = (valueString) -> {
				return invoke(handle, valueString);
			};
			cache.put(type, parser);
		}
		return parser;
	}

	<T> T invoke(MethodHandle handle, String valueString) {
		T value;
		try {
			value = (T) handle.invoke(valueString);
		} catch (Throwable throwable) {
			if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			}
			throw new AssertionError(throwable);
		}
		return value;
	}
}
