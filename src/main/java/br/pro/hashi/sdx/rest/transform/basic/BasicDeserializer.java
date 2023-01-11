package br.pro.hashi.sdx.rest.transform.basic;

import java.io.IOException;
import java.io.Reader;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;

public class BasicDeserializer implements Deserializer {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromReader(Reader reader, Class<T> type) throws IOException {
		if (type.equals(String.class)) {
			return (T) Media.read(reader);
		}
		if (type.equals(Reader.class)) {
			return (T) reader;
		}
		throw new IllegalArgumentException("Type must be String or Reader");
	}
}
