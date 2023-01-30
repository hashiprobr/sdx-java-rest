package br.pro.hashi.sdx.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class FieldsTest {
	private static final double DELTA = 0.000001;

	private Fields f;

	@BeforeEach
	void setUp() {
		f = newInstance();
	}

	@Test
	void splitsStringList() {
		assertEquals(List.of("false", "true"), f.split("xs", ","));
		assertEquals(List.of("0", "1"), f.split("ys", ","));
		assertEquals(List.of("2.3", "4.5"), f.split("zs", ","));
	}

	@Test
	void splitsBooleanList() {
		assertEquals(List.of(false, true), f.split("xs", ",", boolean.class));
	}

	@Test
	void splitsIntList() {
		assertEquals(List.of(0, 1), f.split("ys", ",", int.class));
	}

	@Test
	void splitsDoubleList() {
		List<Double> values = f.split("zs", ",", double.class);
		assertEquals(2, values.size());
		assertEquals(2.3, values.get(0), DELTA);
		assertEquals(4.5, values.get(1), DELTA);
	}

	@Test
	void doesNotSplitIfNameIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.split(null, ",");
		});
	}

	@Test
	void doesNotSplitIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.split("x", ",", null);
		});
	}

	@Test
	void doesNotSplitIfValueIsNotAvailable() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.split("w", ",");
		});
	}

	@Test
	void doesNotSplitIfSeparatorIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.split("x", null);
		});
	}

	@Test
	void doesNotSplitIfSeparatorIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.split("x", "");
		});
	}

	@Test
	void requiresString() {
		assertEquals("false", f.require("x"));
		assertEquals("0", f.require("y"));
		assertEquals("2.3", f.require("z"));
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
		assertEquals(2.3, f.require("z", double.class), DELTA);
	}

	@Test
	void doesNotRequireIfNameIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.require(null);
		});
	}

	@Test
	void doesNotRequireIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.require("x", null);
		});
	}

	@Test
	void doesNotRequireIfValueIsNotAvailable() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.require("w");
		});
	}

	@Test
	void getsEmptyList() {
		assertEquals(List.of(), f.getList("w"));
	}

	@Test
	void getsStringList() {
		assertEquals(List.of("false", "true"), f.getList("x"));
		assertEquals(List.of("0", "1"), f.getList("y"));
		assertEquals(List.of("2.3", "4.5"), f.getList("z"));
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
		assertEquals(2.3, values.get(0), DELTA);
		assertEquals(4.5, values.get(1), DELTA);
	}

	@Test
	void doesNotGetListIfNameIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.getList(null);
		});
	}

	@Test
	void doesNotGetListIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.getList("x", null);
		});
	}

	@Test
	void getsNull() {
		assertNull(f.get("w"));
	}

	@Test
	void getsDefaultString() {
		assertEquals("6", f.get("w", "6"));
	}

	@Test
	void getsDefaultInt() {
		assertEquals(7, f.get("w", int.class, 7));
	}

	@Test
	void getsDefaultDouble() {
		assertEquals(8.9, f.get("w", double.class, 8.9), DELTA);
	}

	@Test
	void getsString() {
		assertEquals("false", f.get("x"));
		assertEquals("0", f.get("y"));
		assertEquals("2.3", f.get("z"));
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
		assertEquals(2.3, f.get("z", double.class), DELTA);
	}

	@Test
	void doesNotGetIfNameIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.get(null);
		});
	}

	@Test
	void doesNotGetIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.get("x", (Class<?>) null);
		});
	}

	@Test
	void obtainsNames() {
		assertEquals(Set.of("x", "y", "z", "xs", "ys", "zs"), f.names());
	}

	protected abstract Fields newInstance();
}
