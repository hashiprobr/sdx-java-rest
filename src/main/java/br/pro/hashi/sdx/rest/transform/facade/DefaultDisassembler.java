package br.pro.hashi.sdx.rest.transform.facade;

import java.io.InputStream;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.exception.UnsupportedException;

class DefaultDisassembler implements Disassembler {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(InputStream stream, Type type) {
		if (type.equals(byte[].class)) {
			return (T) Media.read(stream);
		}
		if (type.equals(InputStream.class)) {
			return (T) stream;
		}
		throw new UnsupportedException("Type must be equal to byte[] or InputStream");
	}
}
