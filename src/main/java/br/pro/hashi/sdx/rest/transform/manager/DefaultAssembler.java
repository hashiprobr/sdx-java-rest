package br.pro.hashi.sdx.rest.transform.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import br.pro.hashi.sdx.rest.constant.Types;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultAssembler implements Assembler {
	private static final DefaultAssembler INSTANCE = new DefaultAssembler();

	public static DefaultAssembler getInstance() {
		return INSTANCE;
	}

	DefaultAssembler() {
	}

	@Override
	public <T> void write(T body, Type type, OutputStream stream) {
		try {
			if (body instanceof byte[]) {
				stream.write((byte[]) body);
				return;
			}
			if (body instanceof InputStream) {
				InputStream input = (InputStream) body;
				input.transferTo(stream);
				input.close();
				return;
			}
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
		if (Types.instanceOfStreamConsumer(body, type)) {
			@SuppressWarnings("unchecked")
			Consumer<OutputStream> consumer = (Consumer<OutputStream>) body;
			consumer.accept(stream);
			return;
		}
		throw new TypeException("Body must be an instance of byte[], InputStream, or Consumer<OutputStream>");
	}
}
