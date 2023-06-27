package br.pro.hashi.sdx.rest.transform.manager;

import java.io.Reader;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultDeserializer implements Deserializer {
	private final ParserFactory factory;

	DefaultDeserializer(ParserFactory factory) {
		this.factory = factory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(Reader reader, Type type) {
		if (TransformManager.PRIMITIVE_TYPES.contains(type)) {
			return (T) factory.get((Class<?>) type).apply(MediaCoder.getInstance().read(reader));
		}
		if (type.equals(String.class)) {
			return (T) MediaCoder.getInstance().read(reader);
		}
		if (type.equals(Reader.class)) {
			return (T) reader;
		}
		throw new TypeException("Type must be primitive or equal to String or Reader");
	}
}
