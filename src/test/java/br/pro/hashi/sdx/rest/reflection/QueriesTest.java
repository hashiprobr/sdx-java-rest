package br.pro.hashi.sdx.rest.reflection;

import java.util.HashMap;
import java.util.Map;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.FieldsTest;

class QueriesTest extends FieldsTest {
	private Queries q;

	@Override
	protected Fields newInstance() {
		Cache cache = new Cache();
		Map<String, String[]> map = new HashMap<>();
		map.put("x", new String[] { "false", "true" });
		map.put("y", new String[] { "0", "1" });
		map.put("z", new String[] { "2.3", "4.5" });
		map.put("xs", new String[] { "false,true" });
		map.put("ys", new String[] { "0,1" });
		map.put("zs", new String[] { "2.3,4.5" });
		q = new Queries(cache, map);
		return q;
	}
}
