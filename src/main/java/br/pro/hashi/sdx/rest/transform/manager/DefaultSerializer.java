package br.pro.hashi.sdx.rest.transform.manager;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultSerializer implements Serializer {
	private static final DefaultSerializer INSTANCE = new DefaultSerializer();

	public static DefaultSerializer getInstance() {
		return INSTANCE;
	}

	private final Type consumerType;

	DefaultSerializer() {
		this.consumerType = new Hint<Consumer<Writer>>() {}.getType();
	}

	@Override
	public <T> void write(T body, Type type, Writer writer) {
		try {
			if (TransformManager.SIMPLE_TYPES.contains(type)) {
				writer.write(body.toString());
				return;
			}
			if (body instanceof Reader) {
				((Reader) body).transferTo(writer);
				return;
			}
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
		if (type.equals(consumerType)) {
			@SuppressWarnings("unchecked")
			Consumer<Writer> consumer = (Consumer<Writer>) body;
			consumer.accept(writer);
			return;
		}
		throw new TypeException("Body must be a primitive, a big number, or an instance of String, Reader, or Consumer<Writer>");
	}
}