package br.pro.hashi.sdx.rest.reflection;

import java.util.HashMap;
import java.util.Map;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.FieldsTest;

class QueriesTest extends FieldsTest {
	private Cache cache;
	private Map<String, String[]> map;
	private Queries q;

	@Override
	protected Fields newInstance() {
		cache = new Cache();
		map = new HashMap<>();
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
