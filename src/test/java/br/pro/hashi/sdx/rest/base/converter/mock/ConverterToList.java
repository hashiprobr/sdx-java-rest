package br.pro.hashi.sdx.rest.base.converter.mock;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import br.pro.hashi.sdx.rest.base.converter.Converter;

public class ConverterToList extends Converter.ToListOf<Byte, ByteArrayInputStream> {
	@Override
	public List<Byte> serialize(ByteArrayInputStream value) {
		List<Byte> list = new ArrayList<>();
		for (byte b : value.readAllBytes()) {
			list.add(b);
		}
		return list;
	}

	@Override
	public ByteArrayInputStream deserialize(List<Byte> value) {
		byte[] bytes = new byte[value.size()];
		int i = 0;
		for (byte b : value) {
			bytes[i] = b;
			i++;
		}
		return new ByteArrayInputStream(bytes);
	}
}
