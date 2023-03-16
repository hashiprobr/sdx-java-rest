package br.pro.hashi.sdx.rest.server.tree.mock.node;

import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Nested;

@Nested(in = One.class, at = 0)
public class ZeroInOne extends RestResource {
	public void get() {
	}
}
