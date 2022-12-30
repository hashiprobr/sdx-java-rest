package br.pro.hashi.sdx.rest.base.converter.mock;

import java.io.ByteArrayInputStream;

import br.pro.hashi.sdx.rest.base.converter.Converter;

public class ConverterToByte extends Converter.ToByte<ByteArrayInputStream> {
	@Override
	public Byte serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		byte b;
		if (bytes[0] == 0) {
			b = 10;
		} else {
			b = 11;
		}
		return b;
	}

	@Override
	public ByteArrayInputStream deserialize(Byte value) {
		byte b;
		if (value == 0) {
			b = 10;
		} else {
			b = 11;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
