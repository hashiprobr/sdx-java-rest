package br.pro.hashi.sdx.rest.converter.mock;

import java.io.ByteArrayInputStream;

import br.pro.hashi.sdx.rest.converter.Converter;

public class ConverterToInteger extends Converter.ToInteger<ByteArrayInputStream> {
	@Override
	public Integer serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		int i;
		if (bytes[0] == 0) {
			i = 30;
		} else {
			i = 31;
		}
		return i;
	}

	@Override
	public ByteArrayInputStream deserialize(Integer value) {
		byte b;
		if (value == 0) {
			b = 30;
		} else {
			b = 31;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
