package br.pro.hashi.sdx.rest.transformer;

import br.pro.hashi.sdx.rest.transformer.base.Serializer;

class TextSerializer implements Serializer {
	@Override
	public String serialize(Object body) {
		if (!(body instanceof String)) {
			throw new IllegalArgumentException("Body must be instance of String");
		}
		return (String) body;
	}
}
