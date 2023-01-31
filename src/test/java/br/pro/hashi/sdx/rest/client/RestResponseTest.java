package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Reader;

import org.junit.jupiter.api.AfterEach;
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
	private InputStream stream;
	private Reader reader;
	private RestResponse r;
	private MockedStatic<Media> media;

	@BeforeEach
	void setUp() {
		facade = mock(Facade.class);
		headers = mock(Headers.class);
		stream = InputStream.nullInputStream();
		reader = mock(Reader.class);
		r = new RestResponse(facade, 600, headers, null, stream);
		media = mockStatic(Media.class);
		media.when(() -> Media.decode(stream, null)).thenReturn(stream);
		media.when(() -> Media.reader(stream, null)).thenReturn(reader);
		media.when(() -> Media.strip(null)).thenReturn("type/subtype");
	}

	@AfterEach
	void tearDown() {
		media.close();
	}

	@Test
	void getsBody() {
		Object body = mockBody();
		assertSame(body, r.getBody(Object.class));
		assertThrows(ClientException.class, () -> {
			r.getBody(Object.class);
		});
	}

	@Test
	void getsBodyWithHint() {
		Object body = mockBody();
		assertSame(body, r.getBody(new Hint<Object>() {}));
		assertThrows(ClientException.class, () -> {
			r.getBody(new Hint<Object>() {});
		});
	}

	private Object mockBody() {
		Object body = new Object();
		Deserializer deserializer = mock(Deserializer.class);
		when(deserializer.read(reader, Object.class)).thenReturn(body);
		when(facade.isBinary(Object.class)).thenReturn(false);
		when(facade.cleanForDeserializing("type/subtype", Object.class)).thenReturn("type/subtype");
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
		Object body = mockBinaryBody();
		assertSame(body, r.getBody(Object.class));
		assertThrows(ClientException.class, () -> {
			r.getBody(Object.class);
		});
	}

	@Test
	void getsBinaryBodyWithHint() {
		Object body = mockBinaryBody();
		assertSame(body, r.getBody(new Hint<Object>() {}));
		assertThrows(ClientException.class, () -> {
			r.getBody(new Hint<Object>() {});
		});
	}

	private Object mockBinaryBody() {
		Object body = new Object();
		Disassembler disassembler = mock(Disassembler.class);
		when(disassembler.read(stream, Object.class)).thenReturn(body);
		when(facade.isBinary(Object.class)).thenReturn(true);
		when(facade.cleanForDisassembling("type/subtype", Object.class)).thenReturn("type/subtype");
		when(facade.getDisassembler("type/subtype")).thenReturn(disassembler);
		return body;
	}
}
