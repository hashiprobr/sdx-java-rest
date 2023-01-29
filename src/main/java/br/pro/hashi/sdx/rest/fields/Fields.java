package br.pro.hashi.sdx.rest.fields;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public abstract class Fields {
	private final Cache cache;

	protected Fields(Cache cache) {
		this.cache = cache;
	}

	public List<String> getList(String name, String regex) {
		return getList(name, regex, String.class);
	}

	public <T> List<T> getList(String name, String regex, Class<T> type) {
		return map(Stream.of(require(name).split(regex)), type);
	}

	public String require(String name) {
		return require(name, String.class);
	}

	public <T> T require(String name, Class<T> type) {
		T value = get(name, type);
		if (value == null) {
			throw new IllegalArgumentException("Name '%s' does not exist".formatted(name));
		}
		return value;
	}

	public List<String> getList(String name) {
		return getList(name, String.class);
	}

	public <T> List<T> getList(String name, Class<T> type) {
		return map(doStream(name), type);
	}

	private <T> List<T> map(Stream<String> stream, Class<T> type) {
		return stream.map(cache.get(type)).toList();
	}

	public String get(String name) {
		return get(name, String.class, null);
	}

	public String get(String name, String defaultValue) {
		return get(name, String.class, defaultValue);
	}

	public <T> T get(String name, Class<T> type) {
		return get(name, type, null);
	}

	public <T> T get(String name, Class<T> type, T defaultValue) {
		String valueString = doGet(name);
		T value;
		if (valueString == null) {
			value = defaultValue;
		} else {
			value = cache.get(type).apply(valueString);
		}
		return value;
	}

	protected abstract Stream<String> doStream(String name);

	protected abstract String doGet(String name);

	protected abstract Set<String> names();
}
