package br.pro.hashi.sdx.rest.transform.manager;

import java.io.InputStream;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultDisassembler implements Disassembler {
	private static final DefaultDisassembler INSTANCE = newInstance();

	private static DefaultDisassembler newInstance() {
		MediaCoder coder = MediaCoder.getInstance();
		return new DefaultDisassembler(coder);
	}

	public static DefaultDisassembler getInstance() {
		return INSTANCE;
	}

	private final MediaCoder coder;

	DefaultDisassembler(MediaCoder coder) {
		this.coder = coder;
	}

	@Override
	public <T> T read(InputStream stream, Type type) {
		if (type.equals(byte[].class)) {
			@SuppressWarnings("unchecked")
			T body = (T) coder.read(stream);
			return body;
		}
		if (type.equals(InputStream.class)) {
			@SuppressWarnings("unchecked")
			T body = (T) stream;
			return body;
		}
		throw new TypeException("Type must be equal to byte[] or InputStream");
	}
}
