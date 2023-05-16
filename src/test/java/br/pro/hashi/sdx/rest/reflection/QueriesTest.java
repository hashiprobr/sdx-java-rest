package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.FieldsTest;

class QueriesTest extends FieldsTest {
	private Map<String, String[]> map;
	private Queries q;

	@Override
	protected Fields newInstance(ParserFactory factory) {
		map = new HashMap<>();
		map.put("x", new String[] { "false", "true" });
		map.put("y", new String[] { "0", "1" });
		map.put("z", new String[] { "2.2", "3.3" });
		map.put("xs", new String[] { "false,true" });
		map.put("ys", new String[] { "0,1" });
		map.put("zs", new String[] { "2.2,3.3" });
		q = new Queries(factory, map);
		return q;
	}

	@Test
	void getsMap() {
		assertSame(map, q.getMap());
	}
}
