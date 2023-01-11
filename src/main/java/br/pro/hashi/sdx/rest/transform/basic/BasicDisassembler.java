package br.pro.hashi.sdx.rest.transform.basic;

import java.io.IOException;
import java.io.InputStream;

import br.pro.hashi.sdx.rest.transform.Disassembler;

public class BasicDisassembler implements Disassembler {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromStream(InputStream stream, Class<T> type) throws IOException {
		if (type.equals(byte[].class)) {
			return (T) stream.readAllBytes();
		}
		if (type.equals(InputStream.class)) {
			return (T) stream;
		}
		throw new IllegalArgumentException("Type must be byte[] or InputStream");
	}
}
