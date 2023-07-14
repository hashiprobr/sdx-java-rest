package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Reader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

class RestResponseTest {
	private static final int STATUS = 600;
	private static final String CONTENT_TYPE = "type/subtype";

	private AutoCloseable mocks;
	private @Mock MediaCoder coder;
	private @Mock TransformManager manager;
	private @Mock Headers headers;
	private InputStream stream;
	private RestResponse r;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		stream = InputStream.nullInputStream();

		when(coder.strip(any(String.class))).thenAnswer((invocation) -> {
			String contentType = invocation.getArgument(0);
			if (contentType.isEmpty()) {
				return null;
			}
			int index = contentType.indexOf(';');
			return contentType.substring(0, index);
		});
		when(coder.decode(eq(stream), any())).thenAnswer((invocation) -> {
			String contentType = invocation.getArgument(1);
			assertTrue(contentType == null || contentType.indexOf(';') != -1);
			return stream;
		});
	}

	@AfterEach
	void tearDown() {
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsInstance() {
		r = RestResponse.newInstance(manager, STATUS, headers, CONTENT_TYPE, stream);
		assertSame(manager, r.getManager());
		assertSame(stream, r.getStream());
		assertEquals(STATUS, r.getStatus());
		assertSame(headers, r.getHeaders());
		assertEquals(CONTENT_TYPE, r.getContentType());
	}

	@Test
	void getsBody() {
		r = newRestResponse();
		Object body = mockBody(CONTENT_TYPE);
		assertSame(body, r.getBody(Object.class));
		assertThrows(IllegalStateException.class, () -> {
			r.getBody(Object.class);
		});
	}

	@Test
	void getsBodyWithHint() {
		r = newRestResponse();
		Object body = mockBody(CONTENT_TYPE);
		assertSame(body, r.getBody(new Hint<Object>() {}));
		assertThrows(IllegalStateException.class, () -> {
			r.getBody(new Hint<Object>() {});
		});
	}

	@Test
	void getsBodyWithoutContentType() {
		r = newRestResponse();
		Object body = mockBody(null);
		assertSame(body, r.getBody(Object.class, null));
		assertThrows(IllegalStateException.class, () -> {
			r.getBody(Object.class, null);
		});
	}

	@Test
	void getsBodyWithHintWithoutContentType() {
		r = newRestResponse();
		Object body = mockBody(null);
		assertSame(body, r.getBody(new Hint<Object>() {}, null));
		assertThrows(IllegalStateException.class, () -> {
			r.getBody(new Hint<Object>() {}, null);
		});
	}

	private Object mockBody(String strippedContentType) {
		Object body = new Object();
		Reader reader = Reader.nullReader();
		Deserializer deserializer = mock(Deserializer.class);
		when(coder.reader(eq(stream), any())).thenAnswer((invocation) -> {
			String contentType = invocation.getArgument(1);
			assertTrue(contentType == null || contentType.indexOf(';') != -1);
			return reader;
		});
		when(manager.isBinary(Object.class)).thenReturn(false);
		when(manager.getDeserializerType(strippedContentType, Object.class)).thenReturn(CONTENT_TYPE);
		when(manager.getDeserializer(CONTENT_TYPE)).thenReturn(deserializer);
		when(deserializer.read(reader, Object.class)).thenReturn(body);
		return body;
	}

	@Test
	void getsBinaryBody() {
		r = newRestResponse();
		Object body = mockBinaryBody(CONTENT_TYPE);
		assertSame(body, r.getBody(Object.class));
		assertThrows(IllegalStateException.class, () -> {
			r.getBody(Object.class);
		});
	}

	@Test
	void getsBinaryBodyWithHint() {
		r = newRestResponse();
		Object body = mockBinaryBody(CONTENT_TYPE);
		assertSame(body, r.getBody(new Hint<Object>() {}));
		assertThrows(IllegalStateException.class, () -> {
			r.getBody(new Hint<Object>() {});
		});
	}

	@Test
	void getsBinaryBodyWithoutContentType() {
		r = newRestResponse();
		Object body = mockBinaryBody(null);
		assertSame(body, r.getBody(Object.class, null));
		assertThrows(IllegalStateException.class, () -> {
			r.getBody(Object.class, null);
		});
	}

	@Test
	void getsBinaryBodyWithHintWithoutContentType() {
		r = newRestResponse();
		Object body = mockBinaryBody(null);
		assertSame(body, r.getBody(new Hint<Object>() {}, null));
		assertThrows(IllegalStateException.class, () -> {
			r.getBody(new Hint<Object>() {}, null);
		});
	}

	private Object mockBinaryBody(String strippedContentType) {
		Object body = new Object();
		Disassembler disassembler = mock(Disassembler.class);
		when(manager.isBinary(Object.class)).thenReturn(true);
		when(manager.getDisassemblerType(strippedContentType, Object.class)).thenReturn(CONTENT_TYPE);
		when(manager.getDisassembler(CONTENT_TYPE)).thenReturn(disassembler);
		when(disassembler.read(stream, Object.class)).thenReturn(body);
		return body;
	}

	@Test
	void doesNotGetBodyWithNullType() {
		r = newRestResponse();
		assertThrows(NullPointerException.class, () -> {
			r.getBody((Class<?>) null);
		});
	}

	@Test
	void doesNotGetBodyWithNullHint() {
		r = newRestResponse();
		assertThrows(NullPointerException.class, () -> {
			r.getBody((Hint<?>) null);
		});
	}

	private RestResponse newRestResponse() {
		return new RestResponse(coder, manager, STATUS, headers, "%s;parameter".formatted(CONTENT_TYPE), stream);
	}
}
