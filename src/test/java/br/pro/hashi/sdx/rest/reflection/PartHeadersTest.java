package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.FieldsTest;
import jakarta.servlet.http.Part;

class PartHeadersTest extends FieldsTest {
	private @Mock Part part;
	private PartHeaders h;

	@Override
	protected Fields newInstance(ParserFactory factory) {
		when(part.getHeaders("x")).thenReturn(List.of("false", "true"));
		when(part.getHeaders("y")).thenReturn(List.of("0", "1"));
		when(part.getHeaders("z")).thenReturn(List.of("2.2", "3.3"));
		when(part.getHeaders("xs")).thenReturn(List.of("false,true"));
		when(part.getHeaders("ys")).thenReturn(List.of("0,1"));
		when(part.getHeaders("zs")).thenReturn(List.of("2.2,3.3"));
		when(part.getHeader("x")).thenReturn("false");
		when(part.getHeader("y")).thenReturn("0");
		when(part.getHeader("z")).thenReturn("2.2");
		when(part.getHeader("xs")).thenReturn("false,true");
		when(part.getHeader("ys")).thenReturn("0,1");
		when(part.getHeader("zs")).thenReturn("2.2,3.3");
		when(part.getHeaderNames()).thenReturn(List.of("x", "y", "z", "xs", "ys", "zs"));
		h = new PartHeaders(factory, part);
		return h;
	}

	@Test
	void getsPart() {
		assertSame(part, h.getPart());
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
