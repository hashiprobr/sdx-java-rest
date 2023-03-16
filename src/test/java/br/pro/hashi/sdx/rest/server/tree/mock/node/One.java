package br.pro.hashi.sdx.rest.server.tree.mock.node;

import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Nested;

@Nested(in = Enclosing.class)
public class One extends RestResource {
	public void get() {
	}
}
