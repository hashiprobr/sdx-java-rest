package br.pro.hashi.sdx.rest.converter.mock;

import java.io.ByteArrayInputStream;

import br.pro.hashi.sdx.rest.converter.Converter;

public class ConverterToLong extends Converter.ToLong<ByteArrayInputStream> {
	@Override
	public Long serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		long l;
		if (bytes[0] == 0) {
			l = 40;
		} else {
			l = 41;
		}
		return l;
	}

	@Override
	public ByteArrayInputStream deserialize(Long value) {
		byte b;
		if (value == 0) {
			b = 40;
		} else {
			b = 41;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
