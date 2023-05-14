package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.FieldsTest;

class HeadersTest extends FieldsTest {
	private Headers h;

	@Override
	protected Fields newInstance() {
		ParserFactory cache = ParserFactory.getInstance();
		HttpFields.Mutable fields = HttpFields.build();
		fields.add("x", "false");
		fields.add("x", "true");
		fields.add("y", "0");
		fields.add("y", "1");
		fields.add("z", "2.3");
		fields.add("z", "4.5");
		fields.add("xs", "false,true");
		fields.add("ys", "0,1");
		fields.add("zs", "2.3,4.5");
		h = new Headers(cache, fields);
		return h;
	}

	@Test
	void splitsWithWhitespaces() {
		assertEquals(List.of("false", "true"), h.split(" \t\nxs \t\n", ","));
		assertEquals(List.of("0", "1"), h.split(" \t\nys \t\n", ","));
		assertEquals(List.of("2.3", "4.5"), h.split(" \t\nzs \t\n", ","));
	}

	@Test
	void doesNotSplitIfNameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			h.split(" \t\n", ",");
		});
	}

	@Test
	void requiresWithWhitespaces() {
		assertEquals("false", h.require(" \t\nx \t\n"));
		assertEquals("0", h.require(" \t\ny \t\n"));
		assertEquals("2.3", h.require(" \t\nz \t\n"));
	}

	@Test
	void doesNotRequireIfNameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			h.require(" \t\n");
		});
	}

	@Test
	void getsListWithWhitespaces() {
		assertEquals(List.of("false", "true"), h.getList(" \t\nx \t\n"));
		assertEquals(List.of("0", "1"), h.getList(" \t\ny \t\n"));
		assertEquals(List.of("2.3", "4.5"), h.getList(" \t\nz \t\n"));
	}

	@Test
	void doesNotGetListIfNameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			h.getList(" \t\n");
		});
	}

	@Test
	void getsWithWhitespaces() {
		assertEquals("false", h.get(" \t\nx \t\n"));
		assertEquals("0", h.get(" \t\ny \t\n"));
		assertEquals("2.3", h.get(" \t\nz \t\n"));
	}

	@Test
	void doesNotGetIfNameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			h.get(" \t\n");
		});
	}
}
