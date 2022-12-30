package br.pro.hashi.sdx.rest.converter.mock;

import java.io.ByteArrayInputStream;

import br.pro.hashi.sdx.rest.converter.Converter;

public class ConverterToShort extends Converter.ToShort<ByteArrayInputStream> {
	@Override
	public Short serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		short s;
		if (bytes[0] == 0) {
			s = 20;
		} else {
			s = 21;
		}
		return s;
	}

	@Override
	public ByteArrayInputStream deserialize(Short value) {
		byte b;
		if (value == 0) {
			b = 20;
		} else {
			b = 21;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
