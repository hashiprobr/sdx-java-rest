package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.reflection.Headers;

class HeadersMapTest {
	private Fields fields;
	private Map<String, List<Fields>> map;
	private HeadersMap m;

	@BeforeEach
	void setUp() {
		fields = mock(Headers.class);
		map = new HashMap<>();
		map.put("name", List.of(fields));
		m = new HeadersMap(map);
	}

	@Test
	void gets() {
		assertSame(fields, m.get("name"));
	}

	@Test
	void doesNotGetIfNameIsNull() {
		assertThrows(NullPointerException.class, () -> {
			m.get(null);
		});
	}

	@Test
	void doesNotGetIfValueIsNotAvailable() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.get("");
		});
	}

	@Test
	void getsList() {
		List<Fields> headersList = m.getList("name");
		assertEquals(1, headersList.size());
		assertSame(fields, headersList.get(0));
	}

	@Test
	void getsEmptyList() {
		assertEquals(List.of(), m.getList(""));
	}

	@Test
	void doesNotGetList() {
		assertThrows(NullPointerException.class, () -> {
			m.getList(null);
		});
	}

	@Test
	void obtainsNames() {
		assertEquals(Set.of("name"), m.names());
	}
}
