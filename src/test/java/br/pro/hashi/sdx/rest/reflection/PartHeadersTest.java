package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.FieldsTest;
import jakarta.servlet.http.Part;

class PartHeadersTest extends FieldsTest {
	private PartHeaders h;

	@Override
	protected Fields newInstance() {
		ParserFactory cache = new ParserFactory();
		Part part = mock(Part.class);
		when(part.getHeaderNames()).thenReturn(List.of("x", "y", "z", "xs", "ys", "zs"));
		when(part.getHeaders("x")).thenReturn(List.of("false", "true"));
		when(part.getHeaders("y")).thenReturn(List.of("0", "1"));
		when(part.getHeaders("z")).thenReturn(List.of("2.3", "4.5"));
		when(part.getHeader("x")).thenReturn("false");
		when(part.getHeader("y")).thenReturn("0");
		when(part.getHeader("z")).thenReturn("2.3");
		when(part.getHeader("xs")).thenReturn("false,true");
		when(part.getHeader("ys")).thenReturn("0,1");
		when(part.getHeader("zs")).thenReturn("2.3,4.5");
		h = new PartHeaders(cache, part);
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
