package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeCacheTest {
	private TypeCache c;

	@BeforeEach
	void setUp() {
		c = new TypeCache();
	}

	@Test
	void initializesWithoutListTypes() {
		assertTrue(c.listTypes.isEmpty());
	}

	@Test
	void initializesWithoutMapTypes() {
		assertTrue(c.mapTypes.isEmpty());
	}

	@Test
	void putsListTypeOnce() {
		Class<? extends List<Object>> listType = c.getListTypeOf(Object.class);
		assertNotNull(listType);
		assertEquals(1, c.listTypes.size());
		assertSame(listType, c.getListTypeOf(Object.class));
		assertEquals(1, c.listTypes.size());
	}

	@Test
	void putsMapTypeOnce() {
		Class<? extends Map<String, Object>> mapType = c.getMapTypeOf(Object.class);
		assertNotNull(mapType);
		assertEquals(1, c.mapTypes.size());
		assertSame(mapType, c.getMapTypeOf(Object.class));
		assertEquals(1, c.mapTypes.size());
	}
}
