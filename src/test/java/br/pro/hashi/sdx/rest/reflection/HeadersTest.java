package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.FieldsTest;

class HeadersTest extends FieldsTest {
	private HttpFields.Mutable fields;
	private Headers h;

	@Override
	protected Fields newInstance(ParserFactory factory) {
		fields = HttpFields.build();
		fields.add("x", "false");
		fields.add("x", "true");
		fields.add("y", "0");
		fields.add("y", "1");
		fields.add("z", "2.2");
		fields.add("z", "3.3");
		fields.add("xs", "false,true");
		fields.add("ys", "0,1");
		fields.add("zs", "2.2,3.3");
		h = new Headers(factory, fields);
		return h;
	}

	@Test
	void getsInstance() {
		fields = HttpFields.build();
		assertInstanceOf(Headers.class, Headers.newInstance(fields));
	}

	@Test
	void getsFields() {
		assertSame(fields, h.getFields());
	}

	@Test
	void splitsListWithWhitespaces() {
		assertEquals(List.of("false"), h.split(" \t\nx \t\n", ","));
		assertEquals(List.of("0"), h.split(" \t\ny \t\n", ","));
		assertEquals(List.of("2.2"), h.split(" \t\nz \t\n", ","));
		assertEquals(List.of("false", "true"), h.split(" \t\nxs \t\n", ","));
		assertEquals(List.of("0", "1"), h.split(" \t\nys \t\n", ","));
		assertEquals(List.of("2.2", "3.3"), h.split(" \t\nzs \t\n", ","));
	}

	@Test
	void doesNotSplitListWithBlankName() {
		assertThrows(IllegalArgumentException.class, () -> {
			h.split(" \t\n", ",");
		});
	}

	@Test
	void requiresWithWhitespaces() {
		assertEquals("false", h.require(" \t\nx \t\n"));
		assertEquals("0", h.require(" \t\ny \t\n"));
		assertEquals("2.2", h.require(" \t\nz \t\n"));
		assertEquals("false,true", h.require(" \t\nxs \t\n"));
		assertEquals("0,1", h.require(" \t\nys \t\n"));
		assertEquals("2.2,3.3", h.require(" \t\nzs \t\n"));
	}

	@Test
	void doesNotRequireWithBlankName() {
		assertThrows(IllegalArgumentException.class, () -> {
			h.require(" \t\n");
		});
	}

	@Test
	void getsListWithWhitespaces() {
		assertEquals(List.of("false", "true"), h.getList(" \t\nx \t\n"));
		assertEquals(List.of("0", "1"), h.getList(" \t\ny \t\n"));
		assertEquals(List.of("2.2", "3.3"), h.getList(" \t\nz \t\n"));
		assertEquals(List.of("false,true"), h.getList(" \t\nxs \t\n"));
		assertEquals(List.of("0,1"), h.getList(" \t\nys \t\n"));
		assertEquals(List.of("2.2,3.3"), h.getList(" \t\nzs \t\n"));
	}

	@Test
	void doesNotGetListWithBlankName() {
		assertThrows(IllegalArgumentException.class, () -> {
			h.getList(" \t\n");
		});
	}

	@Test
	void getsWithWhitespaces() {
		assertEquals("false", h.get(" \t\nx \t\n"));
		assertEquals("0", h.get(" \t\ny \t\n"));
		assertEquals("2.2", h.get(" \t\nz \t\n"));
		assertEquals("false,true", h.get(" \t\nxs \t\n"));
		assertEquals("0,1", h.get(" \t\nys \t\n"));
		assertEquals("2.2,3.3", h.get(" \t\nzs \t\n"));
	}

	@Test
	void doesNotGetWithBlankName() {
		assertThrows(IllegalArgumentException.class, () -> {
			h.get(" \t\n");
		});
	}
}
