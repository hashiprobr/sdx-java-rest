package br.pro.hashi.sdx.rest.transform.facade;

import java.io.Reader;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.exception.UnsupportedException;

class DefaultDeserializer implements Deserializer {
	private final Cache cache;

	DefaultDeserializer(Cache cache) {
		this.cache = cache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(Reader reader, Type type) {
		if (Facade.PRIMITIVE_TYPES.contains(type)) {
			return (T) cache.get((Class<?>) type).apply(Media.read(reader));
		}
		if (type.equals(String.class)) {
			return (T) Media.read(reader);
		}
		if (type.equals(Reader.class)) {
			return (T) reader;
		}
		throw new UnsupportedException("Type must be primitive or equal to String or Reader");
	}
}
