package br.pro.hashi.sdx.rest.reflection;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import br.pro.hashi.sdx.rest.Fields;

public final class Queries extends Fields {
	private final Map<String, String[]> map;

	public Queries(ParserFactory factory, Map<String, String[]> map) {
		super(factory);
		this.map = map;
	}

	public Map<String, String[]> getMap() {
		return map;
	}

	@Override
	public Set<String> names() {
		return map.keySet();
	}

	@Override
	protected Stream<String> getStream(String name) {
		String[] valueStrings = map.get(name);
		if (valueStrings == null) {
			return Stream.of();
		}
		return Stream.of(valueStrings);
	}

	@Override
	protected String getString(String name) {
		String[] valueStrings = map.get(name);
		if (valueStrings == null) {
			return null;
		}
		return valueStrings[0];
	}
}
