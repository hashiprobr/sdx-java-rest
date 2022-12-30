package br.pro.hashi.sdx.rest.base.converter.mock;

import java.io.ByteArrayInputStream;

import br.pro.hashi.sdx.rest.base.converter.Converter;

public class ConverterToBoolean extends Converter.ToBoolean<ByteArrayInputStream> {
	@Override
	public Boolean serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		return bytes[0] != 0;
	}

	@Override
	public ByteArrayInputStream deserialize(Boolean value) {
		byte b;
		if (value) {
			b = 1;
		} else {
			b = 0;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
