package br.pro.hashi.sdx.rest.transform.facade;

import java.io.Reader;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.facade.exception.SupportException;

class PlainDeserializer implements Deserializer {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(Reader reader, Type type) {
		if (type.equals(String.class)) {
			return (T) Media.read(reader);
		}
		if (type.equals(Reader.class)) {
			return (T) reader;
		}
		throw new SupportException("Type must be equal to String or Reader");
	}
}
