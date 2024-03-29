package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockStatic;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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
	void getsInstance() {
		map = new HashMap<>();
		try (MockedStatic<ParserFactory> factoryStatic = mockStatic(ParserFactory.class)) {
			factoryStatic.when(() -> ParserFactory.getInstance()).thenReturn(factory);
			assertInstanceOf(Queries.class, Queries.newInstance(map));
		}
	}

	@Test
	void getsMap() {
		assertSame(map, q.getMap());
	}
}
