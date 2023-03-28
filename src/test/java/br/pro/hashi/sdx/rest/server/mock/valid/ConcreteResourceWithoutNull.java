package br.pro.hashi.sdx.rest.server.mock.valid;

import java.util.HashSet;
import java.util.Set;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ConcreteResourceWithoutNull extends RestResource {
	private static final Set<String> NOT_ACCEPTABLE_EXTENSIONS;

	static {
		NOT_ACCEPTABLE_EXTENSIONS = new HashSet<>();
		NOT_ACCEPTABLE_EXTENSIONS.add(null);
	}

	@Override
	public Set<String> notAcceptableExtensions() {
		return NOT_ACCEPTABLE_EXTENSIONS;
	}
}
