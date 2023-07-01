package br.pro.hashi.sdx.rest.constant;

import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import java.util.function.Consumer;

import br.pro.hashi.sdx.rest.Hint;

public final class Types {
	private static final Type STREAM_CONSUMER = new Hint<Consumer<OutputStream>>() {}.getType();
	private static final Type WRITER_CONSUMER = new Hint<Consumer<Writer>>() {}.getType();
	private static final Set<Class<?>> SIMPLE = Set.of(
			boolean.class,
			Boolean.class,
			char.class,
			Character.class,
			byte.class,
			Byte.class,
			short.class,
			Short.class,
			int.class,
			Integer.class,
			long.class,
			Long.class,
			float.class,
			Float.class,
			double.class,
			Double.class,
			BigInteger.class,
			BigDecimal.class,
			String.class);

	public static boolean instanceOfStreamConsumer(Object body, Type type) {
		return body != null && type.equals(STREAM_CONSUMER);
	}

	public static boolean instanceOfWriterConsumer(Object body, Type type) {
		return body != null && type.equals(WRITER_CONSUMER);
	}

	public static boolean instanceOfSimple(Object body, Type type) {
		return body != null && SIMPLE.contains(type);
	}

	public static boolean equalsSimple(Type type) {
		return SIMPLE.contains(type);
	}

	private Types() {
	}
}
