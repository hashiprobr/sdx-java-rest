package br.pro.hashi.sdx.rest.transform.facade;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.exception.AssemblingException;

class OctetAssembler implements Assembler {
	@Override
	public <T> InputStream toStream(T body, Class<T> type) {
		return toStream(body);
	}

	@Override
	public <T> InputStream toStream(T body) {
		if (body instanceof byte[]) {
			return new ByteArrayInputStream((byte[]) body);
		}
		if (body instanceof InputStream) {
			return (InputStream) body;
		}
		throw new AssemblingException("Body must be instance of byte[] or InputStream");
	}
}
