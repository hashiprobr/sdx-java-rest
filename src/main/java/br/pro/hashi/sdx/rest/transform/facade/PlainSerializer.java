package br.pro.hashi.sdx.rest.transform.facade;

import java.io.Reader;
import java.io.StringReader;

import br.pro.hashi.sdx.rest.transform.Serializer;

class PlainSerializer implements Serializer {
	@Override
	public Reader toReader(Object body) {
		if (body instanceof String) {
			return new StringReader((String) body);
		}
		if (body instanceof Reader) {
			return (Reader) body;
		}
		throw new IllegalArgumentException("Body must be instance of String or Reader");
	}
}
