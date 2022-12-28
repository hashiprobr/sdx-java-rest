package br.pro.hashi.sdx.rest.transformer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import br.pro.hashi.sdx.rest.base.Reflection;
import br.pro.hashi.sdx.rest.transformer.exception.DeserializingException;

class SafeGsonDeserializer extends GsonDeserializer {
	private final Set<Class<?>> allowedTypes;

	SafeGsonDeserializer(Gson gson) {
		super(gson);
		this.allowedTypes = new HashSet<>(Set.of(
				boolean.class,
				char.class,
				byte.class,
				short.class,
				int.class,
				long.class,
				float.class,
				double.class,
				Boolean.class,
				Character.class,
				Byte.class,
				Short.class,
				Integer.class,
				Long.class,
				Float.class,
				Double.class,
				BigInteger.class,
				BigDecimal.class));
	}

	@Override
	public <T> T deserialize(String content, Class<T> type) throws DeserializingException {
		if (!allowedTypes.contains(type)) {
			Reflection.getNoArgsConstructor(type);
			allowedTypes.add(type);
		}
		return super.deserialize(content, type);
	}
}
