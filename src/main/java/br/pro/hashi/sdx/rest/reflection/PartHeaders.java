package br.pro.hashi.sdx.rest.reflection;

import java.util.Set;
import java.util.stream.Stream;

import br.pro.hashi.sdx.rest.Fields;
import jakarta.servlet.http.Part;

public final class PartHeaders extends Fields {
	private final Part part;

	public PartHeaders(ParserFactory factory, Part part) {
		super(factory);
		this.part = part;
	}

	public Part getPart() {
		return part;
	}

	@Override
	public Set<String> names() {
		return Set.copyOf(part.getHeaderNames());
	}

	@Override
	protected Stream<String> getStream(String name) {
		return part.getHeaders(clean(name)).stream();
	}

	@Override
	protected String getString(String name) {
		return part.getHeader(clean(name));
	}

	private String clean(String name) {
		name = name.strip();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be blank");
		}
		return name;
	}
}
