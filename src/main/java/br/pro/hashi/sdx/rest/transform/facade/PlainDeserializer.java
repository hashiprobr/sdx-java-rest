package br.pro.hashi.sdx.rest.transform.facade;

import java.io.Reader;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;

class PlainDeserializer implements Deserializer {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromReader(Reader reader, Class<T> type) {
		if (type.equals(String.class)) {
			return (T) Media.read(reader);
		}
		if (type.equals(Reader.class)) {
			return (T) reader;
		}
		throw new IllegalArgumentException("Type must be String or Reader");
	}
}
