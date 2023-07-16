package br.pro.hashi.sdx.rest.transform.manager;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import br.pro.hashi.sdx.rest.constant.Types;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultSerializer implements Serializer {
	private static final DefaultSerializer INSTANCE = new DefaultSerializer();

	public static DefaultSerializer getInstance() {
		return INSTANCE;
	}

	DefaultSerializer() {
	}

	@Override
	public <T> void write(T body, Type type, Writer writer) {
		try {
			if (Types.instanceOfSimple(body, type)) {
				writer.write(body.toString());
				return;
			}
			if (body instanceof Reader) {
				Reader reader = (Reader) body;
				reader.transferTo(writer);
				reader.close();
				return;
			}
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
		if (Types.instanceOfWriterConsumer(body, type)) {
			@SuppressWarnings("unchecked")
			Consumer<Writer> consumer = (Consumer<Writer>) body;
			consumer.accept(writer);
			return;
		}
		throw new TypeException("Body must be a primitive, a big number, or an instance of String, Reader, or Consumer<Writer>");
	}
}
