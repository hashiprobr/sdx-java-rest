package br.pro.hashi.sdx.rest.base.converter.mock;

import java.io.ByteArrayInputStream;

import br.pro.hashi.sdx.rest.base.converter.Converter;

public class ConverterToDouble extends Converter.ToDouble<ByteArrayInputStream> {
	@Override
	public Double serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		double d;
		if (bytes[0] == 0) {
			d = 60;
		} else {
			d = 61;
		}
		return d;
	}

	@Override
	public ByteArrayInputStream deserialize(Double value) {
		byte b;
		if (value == 0) {
			b = 60;
		} else {
			b = 61;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
