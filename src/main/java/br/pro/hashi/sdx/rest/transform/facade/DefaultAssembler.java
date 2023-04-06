package br.pro.hashi.sdx.rest.transform.facade;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.facade.exception.SupportException;

class DefaultAssembler implements Assembler {
	private final Type consumerType;

	public DefaultAssembler() {
		this.consumerType = new Hint<Consumer<OutputStream>>() {}.getType();
	}

	@Override
	public void write(Object body, Type type, OutputStream stream) {
		try {
			if (body instanceof byte[]) {
				stream.write((byte[]) body);
				return;
			}
			if (body instanceof InputStream) {
				((InputStream) body).transferTo(stream);
				return;
			}
			if (type.equals(consumerType)) {
				@SuppressWarnings("unchecked")
				Consumer<OutputStream> consumer = (Consumer<OutputStream>) body;
				consumer.accept(stream);
				return;
			}
			throw new SupportException("Body must be instance of byte[], InputStream or Consumer<OutputStream>");
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}
}
