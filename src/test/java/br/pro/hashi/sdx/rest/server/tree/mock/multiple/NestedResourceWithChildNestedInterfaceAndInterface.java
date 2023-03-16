package br.pro.hashi.sdx.rest.server.tree.mock.multiple;

import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Nested;

@Nested(in = EnclosingResource.class)
public class NestedResourceWithChildNestedInterfaceAndInterface extends RestResource implements ChildNestedInterface, Interface {
}
