package br.pro.hashi.sdx.rest.transform.facade;

import java.io.Reader;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;

class PlainDeserializer implements Deserializer {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromReader(Reader reader, Type type) {
		if (type.equals(String.class)) {
			return (T) Media.read(reader);
		}
		if (type.equals(Reader.class)) {
			return (T) reader;
		}
		throw new DeserializingException("Type must be String or Reader");
	}
}
