package br.pro.hashi.sdx.rest.reflection;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import br.pro.hashi.sdx.rest.Fields;

public final class Queries extends Fields {
	private final Cache cache;
	private final Map<String, String[]> map;

	protected Queries(Cache cache, Map<String, String[]> map) {
		this.cache = cache;
		this.map = map;
	}

	@Override
	protected Stream<String> doStream(String name) {
		String[] valueStrings = map.get(name);
		if (valueStrings == null) {
			return Stream.of();
		}
		return Stream.of(valueStrings);
	}

	@Override
	protected String doGet(String name) {
		String[] valueStrings = map.get(name);
		if (valueStrings == null) {
			return null;
		}
		return valueStrings[0];
	}

	@Override
	protected <T> Function<String, T> function(Class<T> type) {
		return cache.get(type);
	}

	@Override
	public Set<String> names() {
		return map.keySet();
	}
}
