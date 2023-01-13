package br.pro.hashi.sdx.rest.transform.facade;

import java.io.InputStream;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

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
		throw new DisassemblingException("Type must be byte[] or InputStream");
	}
}
