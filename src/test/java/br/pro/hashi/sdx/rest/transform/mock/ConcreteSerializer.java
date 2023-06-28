package br.pro.hashi.sdx.rest.transform.mock;

import java.io.Writer;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.Serializer;

public class ConcreteSerializer implements Serializer {
	@Override
	public <T> void write(T body, Type type, Writer writer) {
	}
}
