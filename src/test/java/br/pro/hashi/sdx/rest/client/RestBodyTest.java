package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.constant.Defaults;
import br.pro.hashi.sdx.rest.transform.Hint;

class RestBodyTest {
	private MediaCoder coder;
	private Object actual;
	private RestBody b;

	@BeforeEach
	void setUp() {
		coder = mock(MediaCoder.class);
		actual = new Object();
	}

	@Test
	void initializesActual() {
		b = newRestBody();
		assertSame(actual, b.getActual());
	}

	@Test
	void initializesActualWithNull() {
		b = new RestBody(null);
		assertNull(b.getActual());
	}

	@Test
	void initializesType() {
		b = newRestBody();
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
			new RestBody(actual, (Hint<Object>) null);
		});
	}

	@Test
	void initializesTypeWithNull() {
		b = new RestBody(null);
		assertEquals(Object.class, b.getType());
	}

	@Test
	void initializesTypeWithNullAndHint() {
		b = new RestBody(null, new Hint<Object>() {});
		assertEquals(Object.class, b.getType());
	}

	@Test
	void doesNotInitializeTypeWithNullAndHint() {
		assertThrows(NullPointerException.class, () -> {
			new RestBody(null, (Hint<Object>) null);
		});
	}

	@Test
	void initializesWithoutContentType() {
		b = newRestBody();
		assertNull(b.getContentType());
	}

	@Test
	void initializesWithDefaultCharset() {
		b = newRestBody();
		assertEquals(Defaults.CHARSET, b.getCharset());
	}

	@Test
	void initializesWithoutBase64() {
		b = newRestBody();
		assertFalse(b.isBase64());
	}

	@Test
	void setsContentType() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			b = newRestBody();
			String contentType = "type/subtype";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertSame(b, b.as(contentType));
			assertEquals(contentType, b.getContentType());
		}
	}

	@Test
	void doesNotSetContentTypeIfItIsNull() {
		b = newRestBody();
		assertThrows(NullPointerException.class, () -> {
			b.as(null);
		});
		assertNull(b.getContentType());
	}

	@Test
	void doesNotSetContentTypeIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			b = newRestBody();
			String contentType = "type/subtype";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				b.as(contentType);
			});
			assertNull(b.getContentType());
		}
	}

	@Test
	void setsCharset() {
		b = newRestBody();
		if (Defaults.CHARSET.equals(StandardCharsets.UTF_8)) {
			assertSame(b, b.in(StandardCharsets.ISO_8859_1));
			assertEquals(StandardCharsets.ISO_8859_1, b.getCharset());
		} else {
			assertSame(b, b.in(StandardCharsets.UTF_8));
			assertEquals(StandardCharsets.UTF_8, b.getCharset());
		}
	}

	@Test
	void doesNotSetCharset() {
		b = newRestBody();
		assertThrows(NullPointerException.class, () -> {
			b.in(null);
		});
		assertEquals(Defaults.CHARSET, b.getCharset());
	}

	@Test
	void setsBase64() {
		b = newRestBody();
		assertSame(b, b.inBase64());
		assertTrue(b.isBase64());
	}

	private RestBody newRestBody() {
		return new RestBody(actual);
	}
}
