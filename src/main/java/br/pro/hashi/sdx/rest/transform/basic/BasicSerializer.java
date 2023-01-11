package br.pro.hashi.sdx.rest.transform.basic;

import java.io.Reader;
import java.io.StringReader;

import br.pro.hashi.sdx.rest.transform.Serializer;

public class BasicSerializer implements Serializer {
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
