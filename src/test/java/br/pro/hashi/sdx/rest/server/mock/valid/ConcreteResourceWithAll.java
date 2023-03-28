package br.pro.hashi.sdx.rest.server.mock.valid;

import java.util.Set;

import br.pro.hashi.sdx.rest.server.RestResource;

public class ConcreteResourceWithAll extends RestResource {
	private static final Set<String> NOT_ACCEPTABLE_EXTENSIONS = Set.of();

	@Override
	public Set<String> notAcceptableExtensions() {
		return NOT_ACCEPTABLE_EXTENSIONS;
	}
}
