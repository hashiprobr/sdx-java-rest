package br.pro.hashi.sdx.rest.transformer;

import java.io.InputStream;

import br.pro.hashi.sdx.rest.transformer.base.Disassembler;
import br.pro.hashi.sdx.rest.transformer.exception.DisassemblingException;

class ByteDisassembler implements Disassembler {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T disassemble(InputStream stream, Class<T> type) throws DisassemblingException {
		if (!type.equals(InputStream.class)) {
			throw new IllegalArgumentException("Type must be InputStream");
		}
		return (T) stream;
	}
}
