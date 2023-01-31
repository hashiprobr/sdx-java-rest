package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Hint;

class RestBodyTest {
	private Object actual;
	private RestBody b;

	@BeforeEach
	void setUp() {
		actual = new Object();
	}

	@Test
	void initializesActual() {
		b = newBody();
		assertSame(actual, b.getActual());
	}

	@Test
	void initializesType() {
		b = newBody();
		assertEquals(Object.class, b.getType());
	}

	@Test
	void initializesTypeWithHint() {
		b = new RestBody(actual, new Hint<Object>() {});
		assertEquals(Object.class, b.getType());
	}

	@Test
	void doesNotInitializeTypeWithHint() {
		assertThrows(NullPointerException.class, () -> {
			new RestBody(actual, null);
		});
	}

	@Test
	void initializesWithoutName() {
		b = newBody();
		assertEquals("", b.getName());
	}

	@Test
	void initializesWithoutContentType() {
		b = newBody();
		assertNull(b.getContentType());
	}

	@Test
	void initializesWithDefaultCharset() {
		b = newBody();
		assertEquals(Coding.CHARSET, b.getCharset());
	}

	@Test
	void initializesWithoutBase64() {
		b = newBody();
		assertFalse(b.isBase64());
	}

	@Test
	void setsName() {
		b = newBody();
		b.setName("name");
		assertEquals("name", b.getName());
	}

	@Test
	void doesNotSetName() {
		b = newBody();
		assertThrows(NullPointerException.class, () -> {
			b.setName(null);
		});
		assertEquals("", b.getName());
	}

	@Test
	void setsContentType() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			b = newBody();
			String contentType = "type/subtype";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertSame(b, b.as(contentType));
			assertEquals(contentType, b.getContentType());
		}
	}

	@Test
	void doesNotSetContentTypeIfItIsNull() {
		b = newBody();
		assertThrows(NullPointerException.class, () -> {
			b.as(null);
		});
		assertNull(b.getContentType());
	}

	@Test
	void doesNotSetContentTypeIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			b = newBody();
			String contentType = "type/subtype";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			assertThrows(IllegalArgumentException.class, () -> {
				b.as(contentType);
			});
			assertNull(b.getContentType());
		}
	}

	@Test
	void setsCharset() {
		b = newBody();
		if (Coding.CHARSET.equals(StandardCharsets.UTF_8)) {
			assertSame(b, b.in(StandardCharsets.ISO_8859_1));
			assertEquals(StandardCharsets.ISO_8859_1, b.getCharset());
		} else {
			assertSame(b, b.in(StandardCharsets.UTF_8));
			assertEquals(StandardCharsets.UTF_8, b.getCharset());
		}
	}

	@Test
	void doesNotSetCharset() {
		b = newBody();
		assertThrows(NullPointerException.class, () -> {
			b.in(null);
		});
		assertEquals(Coding.CHARSET, b.getCharset());
	}

	@Test
	void setsBase64() {
		b = newBody();
		assertSame(b, b.inBase64());
		assertTrue(b.isBase64());
	}

	private RestBody newBody() {
		return new RestBody(actual);
	}
}
