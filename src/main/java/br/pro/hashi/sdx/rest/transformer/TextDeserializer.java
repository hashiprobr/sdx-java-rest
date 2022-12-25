package br.pro.hashi.sdx.rest.transformer;

import br.pro.hashi.sdx.rest.transformer.base.Deserializer;
import br.pro.hashi.sdx.rest.transformer.exception.DeserializingException;

class TextDeserializer implements Deserializer {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(String content, Class<T> type) throws DeserializingException {
		if (!type.equals(String.class)) {
			throw new IllegalArgumentException("Type must be String");
		}
		return (T) content;
	}
}
