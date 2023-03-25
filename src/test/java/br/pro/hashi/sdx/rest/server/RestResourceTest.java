package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.reflection.Queries;
import jakarta.servlet.http.HttpServletResponse;

class RestResourceTest {
	private HttpServletResponse response;
	private RestResource r;

	@Test
	void constructs() {
		r = newRestResource();
		assertNull(r.getBase());
		assertTrue(r.isNullBase());
	}

	@Test
	void constructsWithBase() {
		r = new RestResourceMock("/b");
		assertEquals("/b", r.getBase());
		assertFalse(r.isNullBase());
	}

	@Test
	void initializesWithoutStatus() {
		r = newRestResource();
		assertEquals(-1, r.getStatus());
	}

	@Test
	void initializesWithoutNullable() {
		r = newRestResource();
		assertFalse(r.isNullable());
	}

	@Test
	void initializesWithoutContentType() {
		r = newRestResource();
		assertNull(r.getContentType());
	}

	@Test
	void initializesWithDefaultCharset() {
		r = newRestResource();
		assertEquals(Coding.CHARSET, r.getCharset());
	}

	@Test
	void initializesWithoutBase64() {
		r = newRestResource();
		assertFalse(r.isBase64());
	}

	@Test
	void initializesWithoutHeaders() {
		r = newRestResource();
		verify(response, times(0)).addHeader(any(), any());
	}

	@Test
	void setsNullable() {
		r = newRestResource();
		Object body = new Object();
		assertSame(body, r.nullable(body));
		assertTrue(r.isNullable());
	}

	@Test
	void setsContentType() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			r = newRestResource();
			String contentType = "type/subtype";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertNull(r.as(contentType));
			assertEquals(contentType, r.getContentType());
		}
	}

	@Test
	void doesNotSetContentTypeIfItIsNull() {
		r = newRestResource();
		assertThrows(NullPointerException.class, () -> {
			r.as(null);
		});
		assertNull(r.getContentType());
	}

	@Test
	void doesNotSetContentTypeIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			r = newRestResource();
			String contentType = "type/subtype";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			assertThrows(IllegalArgumentException.class, () -> {
				r.as(contentType);
			});
			assertNull(r.getContentType());
		}
	}

	@Test
	void setsCharset() {
		r = newRestResource();
		if (Coding.CHARSET.equals(StandardCharsets.UTF_8)) {
			assertNull(r.in(StandardCharsets.ISO_8859_1));
			assertEquals(StandardCharsets.ISO_8859_1, r.getCharset());
		} else {
			assertNull(r.in(StandardCharsets.UTF_8));
			assertEquals(StandardCharsets.UTF_8, r.getCharset());
		}
	}

	@Test
	void doesNotSetCharset() {
		r = newRestResource();
		assertThrows(NullPointerException.class, () -> {
			r.in(null);
		});
		assertEquals(Coding.CHARSET, r.getCharset());
	}

	@Test
	void setsBase64() {
		r = newRestResource();
		assertNull(r.inBase64());
		assertTrue(r.isBase64());
	}

	@Test
	void addsHeader() {
		r = newRestResource();
		assertNull(r.h(" \t\nname \t\n", 0));
		verify(response).addHeader("name", "0");
	}

	@Test
	void doesNotAddHeaderIfNameIsNull() {
		r = newRestResource();
		Object value = new Object();
		assertThrows(NullPointerException.class, () -> {
			r.h(null, value);
		});
		verify(response, times(0)).addHeader(any(), any());
	}

	@Test
	void doesNotAddHeaderIfNameIsBlank() {
		r = newRestResource();
		Object value = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			r.h(" \t\n", value);
		});
		verify(response, times(0)).addHeader(any(), any());
	}

	@Test
	void doesNotAddHeaderIfNameNotInUSASCII() {
		r = newRestResource();
		Object value = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			r.h("spéçíál", value);
		});
		verify(response, times(0)).addHeader(any(), any());
	}

	@Test
	void doesNotAddHeaderIfValueIsNull() {
		r = newRestResource();
		assertThrows(NullPointerException.class, () -> {
			r.h("name", null);
		});
		verify(response, times(0)).addHeader(any(), any());
	}

	@Test
	void doesNotAddHeaderIfValueStringIsNull() {
		r = newRestResource();
		Object value = new Object() {
			@Override
			public String toString() {
				return null;
			}
		};
		assertThrows(NullPointerException.class, () -> {
			r.h("name", value);
		});
		verify(response, times(0)).addHeader(any(), any());
	}

	@Test
	void doesNotAddHeaderIfValueStringNotInUSASCII() {
		r = newRestResource();
		assertThrows(IllegalArgumentException.class, () -> {
			r.h("name", "spéçíál");
		});
		verify(response, times(0)).addHeader(any(), any());
	}

	@Test
	void returnsInformationResponse() {
		r = newRestResource();
		Object body = new Object();
		assertSame(body, r.response(150, body));
	}

	@Test
	void returnsSuccessResponse() {
		r = newRestResource();
		Object body = new Object();
		assertSame(body, r.response(250, body));
	}

	@Test
	void returnsRedirectionResponse() {
		r = newRestResource();
		Object body = new Object();
		assertSame(body, r.response(350, body));
	}

	@Test
	void doesNotReturnSmallResponse() {
		r = newRestResource();
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			r.response(50, body);
		});
	}

	@Test
	void doesNotReturnLargeResponse() {
		r = newRestResource();
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			r.response(450, body);
		});
	}

	@Test
	void throwsClientError() {
		r = newRestResource();
		Object body = new Object();
		RestException exception = r.error(450, body);
		assertEquals(450, exception.getStatus());
		assertSame(body, exception.getBody());
		assertEquals(Object.class, exception.getType());
	}

	@Test
	void throwsServerError() {
		r = newRestResource();
		Object body = new Object();
		RestException exception = r.error(550, body);
		assertEquals(550, exception.getStatus());
		assertSame(body, exception.getBody());
		assertEquals(Object.class, exception.getType());
	}

	@Test
	void doesNotThrowSmallError() {
		r = newRestResource();
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			r.error(350, body);
		});
	}

	@Test
	void doesNotThrowLargeError() {
		r = newRestResource();
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			r.error(650, body);
		});
	}

	private RestResource newRestResource() {
		RestResourceMock concreteResource = new RestResourceMock();
		CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();
		response = mock(HttpServletResponse.class);
		concreteResource.setFields(new HashMap<>(), mock(Headers.class), mock(Queries.class), encoder, response);
		return concreteResource;
	}
}
