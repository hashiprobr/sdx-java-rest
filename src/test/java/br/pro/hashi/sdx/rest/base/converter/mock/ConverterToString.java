package br.pro.hashi.sdx.rest.base.converter.mock;

import java.io.ByteArrayInputStream;

import br.pro.hashi.sdx.rest.base.converter.Converter;

public class ConverterToString extends Converter.ToString<ByteArrayInputStream> {
	@Override
	public String serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		String s;
		if (bytes[0] == 0) {
			s = "90";
		} else {
			s = "91";
		}
		return s;
	}

	@Override
	public ByteArrayInputStream deserialize(String value) {
		byte b;
		if (value.isEmpty()) {
			b = 90;
		} else {
			b = 91;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
