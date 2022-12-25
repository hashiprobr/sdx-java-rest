package br.pro.hashi.sdx.rest.transformer;

import java.io.InputStream;

import br.pro.hashi.sdx.rest.transformer.base.Assembler;

class ByteAssembler implements Assembler {
	@Override
	public InputStream assemble(Object body) {
		if (!(body instanceof InputStream)) {
			throw new IllegalArgumentException("Body must be instance of InputStream");
		}
		return (InputStream) body;
	}
}
