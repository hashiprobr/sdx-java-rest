package br.pro.hashi.sdx.rest.reflection;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jetty.http.HttpFields;

import br.pro.hashi.sdx.rest.Fields;

public final class Headers extends Fields {
	private final Cache cache;
	private final HttpFields fields;

	public Headers(Cache cache, HttpFields fields) {
		this.cache = cache;
		this.fields = fields;
	}

	public HttpFields getFields() {
		return fields;
	}

	@Override
	protected Stream<String> getStream(String name) {
		name = clean(name);
		return fields.getValuesList(name).stream();
	}

	@Override
	protected String doGet(String name) {
		name = clean(name);
		return fields.get(name);
	}

	@Override
	protected <T> Function<String, T> function(Class<T> type) {
		return cache.get(type);
	}

	@Override
	public Set<String> names() {
		return fields.getFieldNamesCollection();
	}

	private String clean(String name) {
		name = name.strip();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be blank");
		}
		return name;
	}
}
