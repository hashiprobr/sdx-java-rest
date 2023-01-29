package br.pro.hashi.sdx.rest;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import br.pro.hashi.sdx.rest.reflection.Cache;

public abstract class Fields {
	private final Cache cache;

	protected Fields(Cache cache) {
		this.cache = cache;
	}

	public List<String> split(String name, String regex) {
		return split(name, regex, String.class);
	}

	public <T> List<T> split(String name, String regex, Class<T> type) {
		return map(Stream.of(require(name).split(regex)), type);
	}

	public String require(String name) {
		return require(name, String.class);
	}

	public <T> T require(String name, Class<T> type) {
		String valueString = doGet(name);
		if (valueString == null) {
			throw new IllegalArgumentException("Name '%s' does not exist".formatted(name));
		}
		return cache.get(type).apply(valueString);
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
		return get(name, String.class);
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

	public abstract Set<String> names();
}
