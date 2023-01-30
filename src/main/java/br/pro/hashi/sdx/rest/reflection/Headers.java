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

	public HttpFields getHttpFields() {
		return fields;
	}

	@Override
	protected Stream<String> doStream(String name) {
		return fields.getValuesList(name).stream();
	}

	@Override
	protected String doGet(String name) {
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
}
