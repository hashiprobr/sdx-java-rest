package br.pro.hashi.sdx.rest.transform.basic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import br.pro.hashi.sdx.rest.transform.Assembler;

public class BasicAssembler implements Assembler {
	@Override
	public InputStream toStream(Object body) {
		if (body instanceof byte[]) {
			return new ByteArrayInputStream((byte[]) body);
		}
		if (body instanceof InputStream) {
			return (InputStream) body;
		}
		throw new IllegalArgumentException("Body must be instance of byte[] or InputStream");
	}
}
