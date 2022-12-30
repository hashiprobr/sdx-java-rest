package br.pro.hashi.sdx.rest.base.converter.mock;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;

import br.pro.hashi.sdx.rest.base.converter.Converter;

public class ConverterToBigInteger extends Converter.ToBigInteger<ByteArrayInputStream> {
	@Override
	public BigInteger serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		BigInteger bi;
		if (bytes[0] == 0) {
			bi = BigInteger.valueOf(70);
		} else {
			bi = BigInteger.valueOf(71);
		}
		return bi;
	}

	@Override
	public ByteArrayInputStream deserialize(BigInteger value) {
		byte b;
		if (value.equals(BigInteger.valueOf(0))) {
			b = 70;
		} else {
			b = 71;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
