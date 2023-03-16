package br.pro.hashi.sdx.rest.server.tree.mock.node;

import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Nested;

@Nested(in = Two.class, at = 2)
public class TwoInTwo extends RestResource {
	public void get() {
	}
}
