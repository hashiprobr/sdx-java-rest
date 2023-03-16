package br.pro.hashi.sdx.rest.server.tree.mock.cyclic;

import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Nested;

@Nested(in = SelfLoop.class)
public class SelfLoop extends RestResource {
}
