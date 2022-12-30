package br.pro.hashi.sdx.rest.converter.mock;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import br.pro.hashi.sdx.rest.converter.Converter;

public class ConverterToMap extends Converter.ToMapOf<Byte, ByteArrayInputStream> {
	@Override
	public Map<String, Byte> serialize(ByteArrayInputStream value) {
		Map<String, Byte> map = new LinkedHashMap<>();
		int i = 0;
		for (byte b : value.readAllBytes()) {
			map.put(Integer.toString(i), b);
			i++;
		}
		return map;
	}

	@Override
	public ByteArrayInputStream deserialize(Map<String, Byte> value) {
		byte[] bytes = new byte[value.size()];
		int i = 0;
		for (byte b : value.values()) {
			bytes[i] = b;
			i++;
		}
		return new ByteArrayInputStream(bytes);
	}
}
