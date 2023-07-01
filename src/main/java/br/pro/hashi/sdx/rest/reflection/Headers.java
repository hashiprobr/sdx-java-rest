package br.pro.hashi.sdx.rest.reflection;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jetty.http.HttpFields;

import br.pro.hashi.sdx.rest.Fields;

public non-sealed class Headers extends Fields {
	public static Headers newInstance(HttpFields fields) {
		ParserFactory factory = ParserFactory.getInstance();
		return new Headers(factory, fields);
	}

	private final HttpFields fields;

	Headers(ParserFactory factory, HttpFields fields) {
		super(factory);
		this.fields = fields;
	}

	public HttpFields getFields() {
		return fields;
	}

	@Override
	public Set<String> names() {
		return fields.getFieldNamesCollection();
	}

	@Override
	protected Stream<String> getStream(String name) {
		return fields.getValuesList(clean(name)).stream();
	}

	@Override
	protected String getString(String name) {
		return fields.get(clean(name));
	}

	private String clean(String name) {
		name = name.strip();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be blank");
		}
		return name;
	}
}
