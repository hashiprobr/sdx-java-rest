package br.pro.hashi.sdx.rest.converter.mock;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

import br.pro.hashi.sdx.rest.converter.Converter;

public class ConverterToBigDecimal extends Converter.ToBigDecimal<ByteArrayInputStream> {
	@Override
	public BigDecimal serialize(ByteArrayInputStream value) {
		byte[] bytes = value.readAllBytes();
		if (bytes.length == 0) {
			return null;
		}
		BigDecimal bd;
		if (bytes[0] == 0) {
			bd = BigDecimal.valueOf(80);
		} else {
			bd = BigDecimal.valueOf(81);
		}
		return bd;
	}

	@Override
	public ByteArrayInputStream deserialize(BigDecimal value) {
		byte b;
		if (value.equals(BigDecimal.valueOf(0))) {
			b = 80;
		} else {
			b = 81;
		}
		return new ByteArrayInputStream(new byte[] { b });
	}
}
