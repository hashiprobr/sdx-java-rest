package br.pro.hashi.sdx.rest.converter.mock;

import java.io.ByteArrayInputStream;

import br.pro.hashi.sdx.rest.converter.Converter;

public class ConverterToFloat extends Converter.ToFloat<ByteArrayInputStream> {
	@Override
	public Float serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		float f;
		if (bytes[0] == 0) {
			f = 50;
		} else {
			f = 51;
		}
		return f;
	}

	@Override
	public ByteArrayInputStream deserialize(Float value) {
		byte b;
		if (value == 0) {
			b = 50;
		} else {
			b = 51;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
