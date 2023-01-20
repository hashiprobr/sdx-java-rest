package br.pro.hashi.sdx.rest.transform.facade;

import java.io.Reader;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;

class PlainDeserializer implements Deserializer {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(Reader reader, Type type) {
		if (type instanceof Class) {
			if (String.class.isAssignableFrom((Class<?>) type)) {
				return (T) Media.read(reader);
			}
			if (Reader.class.isAssignableFrom((Class<?>) type)) {
				return (T) reader;
			}
		}
		throw new DeserializingException("Type must be assignable to String or Reader");
	}
}
