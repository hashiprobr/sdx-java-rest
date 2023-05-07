package br.pro.hashi.sdx.rest.reflection;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import br.pro.hashi.sdx.rest.Fields;
import jakarta.servlet.http.Part;

public final class PartHeaders extends Fields {
	private final ParserFactory cache;
	private final Part part;

	public PartHeaders(ParserFactory cache, Part part) {
		this.cache = cache;
		this.part = part;
	}

	public Part getPart() {
		return part;
	}

	@Override
	protected Stream<String> getStream(String name) {
		name = clean(name);
		return part.getHeaders(name).stream();
	}

	@Override
	protected String doGet(String name) {
		name = clean(name);
		return part.getHeader(name);
	}

	@Override
	protected <T> Function<String, T> function(Class<T> type) {
		return cache.get(type);
	}

	@Override
	public Set<String> names() {
		return Set.copyOf(part.getHeaderNames());
	}

	private String clean(String name) {
		name = name.strip();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be blank");
		}
		return name;
	}
}
