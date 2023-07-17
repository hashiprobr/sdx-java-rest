package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.constant.Defaults;

class RestBodyTest {
	private AutoCloseable mocks;
	private RestBody b;

	protected @Mock MediaCoder coder;
	protected Object actual;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		when(coder.strip(any(String.class))).thenAnswer((invocation) -> {
			String contentType = invocation.getArgument(0);
			if (contentType.isEmpty()) {
				return null;
			}
			int index = contentType.indexOf(';');
			return contentType.substring(0, index);
		});

		actual = new Object();
	}

	@AfterEach
	void tearDown() {
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void gets() {
		try (MockedStatic<MediaCoder> coderStatic = mockStatic(MediaCoder.class)) {
			coderStatic.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			b = RestBody.of(actual);
		}
		assertSame(actual, b.getActual());
		assertEquals(Object.class, b.getType());
	}

	@Test
	void doesNotGetFromNull() {
		assertThrows(NullPointerException.class, () -> {
			RestBody.of(null);
		});
	}

	@Test
	void doesNotGetFromGeneric() {
		assertThrows(IllegalArgumentException.class, () -> {
			RestBody.of(List.of());
		});
	}

	@Test
	void getsFromHint() {
		try (MockedStatic<MediaCoder> coderStatic = mockStatic(MediaCoder.class)) {
			coderStatic.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			b = RestBody.of(actual, new Hint<Object>() {});
		}
		assertSame(actual, b.getActual());
		assertEquals(new Hint<Object>() {}.getType(), b.getType());
	}

	@Test
	void doesNotGetFromNullHint() {
		assertThrows(NullPointerException.class, () -> {
			RestBody.of(actual, null);
		});
	}

	@Test
	void initializesWithoutContentType() {
		b = newInstance();
		assertNull(b.getContentType());
	}

	@Test
	void initializesWithDefaultCharset() {
		b = newInstance();
		assertEquals(Defaults.CHARSET, b.getCharset());
	}

	@Test
	void initializesWithoutBase64() {
		b = newInstance();
		assertFalse(b.isBase64());
	}

	@Test
	void setsContentType() {
		b = newInstance();
		assertSame(b, b.as("type/subtype;parameter"));
		assertEquals("type/subtype", b.getContentType());
	}

	@Test
	void doesNotSetNullContentType() {
		b = newInstance();
		assertThrows(NullPointerException.class, () -> {
			b.as(null);
		});
	}

	@Test
	void doesNotSetBlankContentType() {
		b = newInstance();
		assertThrows(IllegalArgumentException.class, () -> {
			b.as("");
		});
	}

	@Test
	void setsCharset() {
		b = newInstance();
		assertSame(b, b.in(StandardCharsets.US_ASCII));
		assertEquals(StandardCharsets.US_ASCII, b.getCharset());
	}

	@Test
	void doesNotSetNullCharset() {
		b = newInstance();
		assertThrows(NullPointerException.class, () -> {
			b.in(null);
		});
	}

	@Test
	void setsBase64() {
		b = newInstance();
		assertSame(b, b.inBase64());
		assertTrue(b.isBase64());
	}

	protected RestBody newInstance() {
		return new RestBody(coder, actual, Object.class);
	}
}
