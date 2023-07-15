package br.pro.hashi.sdx.rest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.reflection.ParserFactory;

public abstract class FieldsTest {
	private static final double DELTA = 0.000001;

	private AutoCloseable mocks;
	private Fields f;

	protected @Mock ParserFactory factory;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		when(factory.get(boolean.class)).thenReturn(Boolean::parseBoolean);
		when(factory.get(int.class)).thenReturn(Integer::parseInt);
		when(factory.get(double.class)).thenReturn(Double::parseDouble);
		when(factory.get(String.class)).thenReturn((valueString) -> valueString);

		f = newInstance(factory);
	}

	@AfterEach
	void tearDown() {
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void splitsBooleanList() {
		assertEquals(List.of(false), f.split("x", ",", boolean.class));
		assertEquals(List.of(false, true), f.split("xs", ",", boolean.class));
	}

	@Test
	void splitsIntList() {
		assertEquals(List.of(0), f.split("y", ",", int.class));
		assertEquals(List.of(0, 1), f.split("ys", ",", int.class));
	}

	@Test
	void splitsDoubleList() {
		List<Double> values;
		values = f.split("z", ",", double.class);
		assertEquals(1, values.size());
		assertEquals(2.2, values.get(0), DELTA);
		values = f.split("zs", ",", double.class);
		assertEquals(2, values.size());
		assertEquals(2.2, values.get(0), DELTA);
		assertEquals(3.3, values.get(1), DELTA);
	}

	@Test
	void splitsStringList() {
		assertEquals(List.of("false"), f.split("x", ","));
		assertEquals(List.of("0"), f.split("y", ","));
		assertEquals(List.of("2.2"), f.split("z", ","));
		assertEquals(List.of("false", "true"), f.split("xs", ","));
		assertEquals(List.of("0", "1"), f.split("ys", ","));
		assertEquals(List.of("2.2", "3.3"), f.split("zs", ","));
	}

	@Test
	void doesNotSplitListWithNullName() {
		assertThrows(NullPointerException.class, () -> {
			f.split(null, ",");
		});
	}

	@Test
	void doesNotSplitListWithMissingName() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.split("w", ",");
		});
	}

	@Test
	void doesNotSplitListWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			f.split("x", ",", null);
		});
	}

	@Test
	void doesNotSplitListWithNullSeparator() {
		assertThrows(NullPointerException.class, () -> {
			f.split("x", null);
		});
	}

	@Test
	void doesNotSplitListWithEmptySeparator() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.split("x", "");
		});
	}

	@Test
	void requiresBoolean() {
		assertFalse(f.require("x", boolean.class));
	}

	@Test
	void requiresInt() {
		assertEquals(0, f.require("y", int.class));
	}

	@Test
	void requiresDouble() {
		assertEquals(2.2, f.require("z", double.class), DELTA);
	}

	@Test
	void requiresString() {
		assertEquals("false", f.require("x"));
		assertEquals("0", f.require("y"));
		assertEquals("2.2", f.require("z"));
		assertEquals("false,true", f.require("xs"));
		assertEquals("0,1", f.require("ys"));
		assertEquals("2.2,3.3", f.require("zs"));
	}

	@Test
	void doesNotRequireWithNullName() {
		assertThrows(NullPointerException.class, () -> {
			f.require(null);
		});
	}

	@Test
	void doesNotRequireWithMissingName() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.require("w");
		});
	}

	@Test
	void doesNotRequireWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			f.require("x", null);
		});
	}

	@Test
	void getsBooleanList() {
		assertEquals(List.of(false, true), f.getList("x", boolean.class));
	}

	@Test
	void getsIntList() {
		assertEquals(List.of(0, 1), f.getList("y", int.class));
	}

	@Test
	void getsDoubleList() {
		List<Double> values = f.getList("z", double.class);
		assertEquals(2, values.size());
		assertEquals(2.2, values.get(0), DELTA);
		assertEquals(3.3, values.get(1), DELTA);
	}

	@Test
	void getsStringList() {
		assertEquals(List.of("false", "true"), f.getList("x"));
		assertEquals(List.of("0", "1"), f.getList("y"));
		assertEquals(List.of("2.2", "3.3"), f.getList("z"));
		assertEquals(List.of("false,true"), f.getList("xs"));
		assertEquals(List.of("0,1"), f.getList("ys"));
		assertEquals(List.of("2.2,3.3"), f.getList("zs"));
	}

	@Test
	void getsEmptyList() {
		assertEquals(List.of(), f.getList("w"));
	}

	@Test
	void doesNotGetListWithNullName() {
		assertThrows(NullPointerException.class, () -> {
			f.getList(null);
		});
	}

	@Test
	void doesNotGetListWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			f.getList("x", null);
		});
	}

	@Test
	void getsBoolean() {
		assertFalse(f.get("x", boolean.class));
	}

	@Test
	void getsInt() {
		assertEquals(0, f.get("y", int.class));
	}

	@Test
	void getsDouble() {
		assertEquals(2.2, f.get("z", double.class), DELTA);
	}

	@Test
	void getsString() {
		assertEquals("false", f.get("x"));
		assertEquals("0", f.get("y"));
		assertEquals("2.2", f.get("z"));
		assertEquals("false,true", f.get("xs"));
		assertEquals("0,1", f.get("ys"));
		assertEquals("2.2,3.3", f.get("zs"));
	}

	@Test
	void getsDefaultBoolean() {
		assertTrue(f.get("w", boolean.class, true));
	}

	@Test
	void getsDefaultInt() {
		assertEquals(2, f.get("w", int.class, 2));
	}

	@Test
	void getsDefaultDouble() {
		assertEquals(3.3, f.get("w", double.class, 3.3), DELTA);
	}

	@Test
	void getsDefaultString() {
		assertNull(f.get("w", (String) null));
	}

	@Test
	void doesNotGetWithNullName() {
		assertThrows(NullPointerException.class, () -> {
			f.get(null);
		});
	}

	@Test
	void doesNotGetWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			f.get("x", (Class<?>) null);
		});
	}

	@Test
	void getsNames() {
		assertEquals(Set.of("x", "y", "z", "xs", "ys", "zs"), f.names());
	}

	protected abstract Fields newInstance(ParserFactory factory);
}
