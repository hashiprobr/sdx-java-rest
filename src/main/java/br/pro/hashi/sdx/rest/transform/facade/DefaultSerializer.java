package br.pro.hashi.sdx.rest.transform.facade;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.UnsupportedException;

class DefaultSerializer implements Serializer {
	private final Type consumerType;

	public DefaultSerializer() {
		this.consumerType = new Hint<Consumer<Writer>>() {}.getType();
	}

	@Override
	public void write(Object body, Type type, Writer writer) {
		try {
			if (body != null && Facade.PRIMITIVE_TYPES.contains(type)) {
				writer.write(body.toString());
				return;
			}
			if (body instanceof String) {
				writer.write((String) body);
				return;
			}
			if (body instanceof Reader) {
				((Reader) body).transferTo(writer);
				return;
			}
			if (type.equals(consumerType)) {
				@SuppressWarnings("unchecked")
				Consumer<Writer> consumer = (Consumer<Writer>) body;
				consumer.accept(writer);
				return;
			}
			throw new UnsupportedException("Body must be a primitive or an instance of String, Reader or Consumer<Writer>");
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}
}
