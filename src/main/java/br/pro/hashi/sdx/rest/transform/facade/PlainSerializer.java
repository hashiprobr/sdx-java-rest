package br.pro.hashi.sdx.rest.transform.facade;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

class PlainSerializer implements Serializer {
	@Override
	public void write(Object body, Type type, Writer writer) {
		write(body, writer);
	}

	@Override
	public void write(Object body, Writer writer) {
		if (body instanceof String) {
			try {
				writer.write((String) body);
				writer.close();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
			return;
		}
		if (body instanceof Reader) {
			try {
				((Reader) body).transferTo(writer);
				writer.close();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
			return;
		}
		throw new SerializingException("Body must be instance of String or Reader to be written");
	}
}
