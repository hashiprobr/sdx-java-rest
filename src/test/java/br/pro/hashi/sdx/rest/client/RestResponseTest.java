package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestResponseTest {
	private Facade facade;
	private Headers headers;
	private RestResponse r;

	@BeforeEach
	void setUp() {
		facade = mock(Facade.class);
		headers = mock(Headers.class);
		r = new RestResponse(facade, 600, headers, null, InputStream.nullInputStream());
	}

	@Test
	void getsBody() {
		try (MockedStatic<Media> media = mockMedia()) {
			Object body = mockBody();
			assertSame(body, r.getBody(Object.class));
			assertThrows(ClientException.class, () -> {
				r.getBody(Object.class);
			});
		}
	}

	@Test
	void getsBodyWithHint() {
		try (MockedStatic<Media> media = mockMedia()) {
			Object body = mockBody();
			assertSame(body, r.getBody(new Hint<Object>() {}));
			assertThrows(ClientException.class, () -> {
				r.getBody(new Hint<Object>() {});
			});
		}
	}

	private Object mockBody() {
		Object body = new Object();
		Deserializer deserializer = mock(Deserializer.class);
		when(deserializer.read(any(), eq(Object.class))).thenReturn(body);
		when(facade.isBinary(Object.class)).thenReturn(false);
		when(facade.cleanForDeserializing(null, Object.class)).thenReturn("type/subtype");
		when(facade.getDeserializer("type/subtype")).thenReturn(deserializer);
		return body;
	}

	@Test
	void doesNotGetBody() {
		assertThrows(NullPointerException.class, () -> {
			r.getBody((Class<?>) null);
		});
	}

	@Test
	void doesNotGetBodyWithHint() {
		assertThrows(NullPointerException.class, () -> {
			r.getBody((Hint<?>) null);
		});
	}

	@Test
	void getsBinaryBody() {
		try (MockedStatic<Media> media = mockMedia()) {
			Object body = mockBinaryBody();
			assertSame(body, r.getBody(Object.class));
			assertThrows(ClientException.class, () -> {
				r.getBody(Object.class);
			});
		}
	}

	@Test
	void getsBinaryBodyWithHint() {
		try (MockedStatic<Media> media = mockMedia()) {
			Object body = mockBinaryBody();
			assertSame(body, r.getBody(new Hint<Object>() {}));
			assertThrows(ClientException.class, () -> {
				r.getBody(new Hint<Object>() {});
			});
		}
	}

	private Object mockBinaryBody() {
		Object body = new Object();
		Disassembler disassembler = mock(Disassembler.class);
		when(disassembler.read(any(), eq(Object.class))).thenReturn(body);
		when(facade.isBinary(Object.class)).thenReturn(true);
		when(facade.cleanForDisassembling(null, Object.class)).thenReturn("type/subtype");
		when(facade.getDisassembler("type/subtype")).thenReturn(disassembler);
		return body;
	}

	private MockedStatic<Media> mockMedia() {
		MockedStatic<Media> media = mockStatic(Media.class);
		media.when(() -> Media.strip("type/subtype")).thenReturn("type/subtype");
		return media;
	}
}
