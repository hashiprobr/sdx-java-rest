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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

class RestResponseTest {
	private static final String CONTENT_TYPE = "type/subtype";

	private TransformManager manager;
	private Headers headers;
	private InputStream stream;
	private MediaCoder coder;
	private MockedStatic<MediaCoder> media;
	private RestResponse r;

	@BeforeEach
	void setUp() {
		manager = mock(TransformManager.class);
		headers = mock(Headers.class);
		stream = InputStream.nullInputStream();
		coder = mock(MediaCoder.class);
		media = mockStatic(MediaCoder.class);
		media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
	}

	@AfterEach
	void tearDown() {
		media.close();
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { CONTENT_TYPE })
	void getsBody(String contentType) {
		Object body = mockBody(contentType);
		assertSame(body, r.getBody(Object.class));
		assertThrows(ClientException.class, () -> {
			r.getBody(Object.class);
		});
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { CONTENT_TYPE })
	void getsBodyWithHint(String contentType) {
		Object body = mockBody(contentType);
		assertSame(body, r.getBody(new Hint<Object>() {}));
		assertThrows(ClientException.class, () -> {
			r.getBody(new Hint<Object>() {});
		});
	}

	private Object mockBody(String contentType) {
		Reader reader = mock(Reader.class);
		when(coder.decode(stream, contentType)).thenReturn(stream);
		when(coder.reader(stream, contentType)).thenReturn(reader);
		when(coder.strip(contentType)).thenReturn(null);
		r = newRestResponse(contentType);
		Object body = new Object();
		Deserializer deserializer = mock(Deserializer.class);
		when(deserializer.read(reader, Object.class)).thenReturn(body);
		when(manager.isBinary(Object.class)).thenReturn(false);
		when(manager.getDeserializerType(null, Object.class)).thenReturn(CONTENT_TYPE);
		when(manager.getDeserializer(CONTENT_TYPE)).thenReturn(deserializer);
		return body;
	}

	@Test
	void doesNotGetBody() {
		r = newRestResponse();
		assertThrows(NullPointerException.class, () -> {
			r.getBody((Class<?>) null);
		});
	}

	@Test
	void doesNotGetBodyWithHint() {
		r = newRestResponse();
		assertThrows(NullPointerException.class, () -> {
			r.getBody((Hint<?>) null);
		});
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { CONTENT_TYPE })
	void getsBinaryBody(String contentType) {
		Object body = mockBinaryBody(contentType);
		assertSame(body, r.getBody(Object.class));
		assertThrows(ClientException.class, () -> {
			r.getBody(Object.class);
		});
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { CONTENT_TYPE })
	void getsBinaryBodyWithHint(String contentType) {
		Object body = mockBinaryBody(contentType);
		assertSame(body, r.getBody(new Hint<Object>() {}));
		assertThrows(ClientException.class, () -> {
			r.getBody(new Hint<Object>() {});
		});
	}

	private Object mockBinaryBody(String contentType) {
		when(coder.decode(stream, contentType)).thenReturn(stream);
		when(coder.strip(contentType)).thenReturn(null);
		r = newRestResponse(contentType);
		Object body = new Object();
		Disassembler disassembler = mock(Disassembler.class);
		when(disassembler.read(stream, Object.class)).thenReturn(body);
		when(manager.isBinary(Object.class)).thenReturn(true);
		when(manager.getDisassemblerType(null, Object.class)).thenReturn(CONTENT_TYPE);
		when(manager.getDisassembler(CONTENT_TYPE)).thenReturn(disassembler);
		return body;
	}

	private RestResponse newRestResponse() {
		return newRestResponse(null);
	}

	private RestResponse newRestResponse(String contentType) {
		return new RestResponse(manager, 600, headers, contentType, stream);
	}
}
