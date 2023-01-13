package br.pro.hashi.sdx.rest.transform.facade;

import java.io.InputStream;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Disassembler;

class OctetDisassembler implements Disassembler {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromStream(InputStream stream, Class<T> type) {
		if (type.equals(byte[].class)) {
			return (T) Media.read(stream);
		}
		if (type.equals(InputStream.class)) {
			return (T) stream;
		}
		throw new IllegalArgumentException("Type must be byte[] or InputStream");
	}
}
