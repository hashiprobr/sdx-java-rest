package br.pro.hashi.sdx.rest.transform.facade;

import java.io.InputStream;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

class OctetDisassembler implements Disassembler {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromStream(InputStream stream, Type type) {
		if (type instanceof Class) {
			if (byte[].class.isAssignableFrom((Class<?>) type)) {
				return (T) Media.read(stream);
			}
			if (InputStream.class.isAssignableFrom((Class<?>) type)) {
				return (T) stream;
			}
		}
		throw new DisassemblingException("Type must be assignable to byte[] or InputStream");
	}
}
