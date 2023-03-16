package br.pro.hashi.sdx.rest.server.tree.mock.node;

import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Nested;

@Nested(in = Zero.class, at = 2)
public class TwoInZero extends RestResource {
	public void get() {
	}
}
