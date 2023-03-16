package br.pro.hashi.sdx.rest.transform.facade;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.exception.SupportException;

class PlainSerializer implements Serializer {
	@Override
	public void write(Object body, Type type, Writer writer) {
		write(body, writer);
	}

	@Override
	public void write(Object body, Writer writer) {
		try {
			if (body instanceof String) {
				writer.write((String) body);
				return;
			}
			if (body instanceof Reader) {
				((Reader) body).transferTo(writer);
				return;
			}
			throw new SupportException("Body must be instance of String or Reader");
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		} finally {
			try {
				writer.close();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		}
	}
}
