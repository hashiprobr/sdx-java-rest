package br.pro.hashi.sdx.rest.transform.facade;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;

import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

class PlainSerializer implements Serializer {
	@Override
	public <T> void write(T body, Class<T> type, Writer writer) {
		write(body, writer);
	}

	@Override
	public <T> void write(T body, Writer writer) {
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

	@Override
	public <T> Reader toReader(T body, Class<T> type) {
		return toReader(body);
	}

	@Override
	public <T> Reader toReader(T body) {
		if (body instanceof String) {
			return new StringReader((String) body);
		}
		if (body instanceof Reader) {
			return (Reader) body;
		}
		throw new SerializingException("Body must be instance of String or Reader to be read");
	}
}
