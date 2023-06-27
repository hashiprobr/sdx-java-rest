package br.pro.hashi.sdx.rest.server.tree;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Reader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.server.stream.LimitInputStream;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

class DataTest {
	private static final String CONTENT_TYPE = "type/subtype";

	private MediaCoder coder;
	private TransformManager manager;
	private InputStream stream;
	private MockedStatic<MediaCoder> media;
	private Data d;

	@BeforeEach
	void setUp() {
		coder = mock(MediaCoder.class);
		manager = mock(TransformManager.class);
		stream = InputStream.nullInputStream();
		media = mockStatic(MediaCoder.class);
	}

	@AfterEach
	void tearDown() {
		media.close();
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { CONTENT_TYPE })
	void getsBody(String contentType) {
		Reader reader = mock(Reader.class);
		when(coder.decode(any(), eq(contentType))).thenReturn(stream);
		when(coder.reader(stream, contentType)).thenReturn(reader);
		when(coder.strip(contentType)).thenReturn(null);
		media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
		d = newData(contentType);
		Object body = new Object();
		Deserializer deserializer = mock(Deserializer.class);
		when(deserializer.read(reader, Object.class)).thenReturn(body);
		when(manager.isBinary(Object.class)).thenReturn(false);
		when(manager.getDeserializerType(null, Object.class)).thenReturn(CONTENT_TYPE);
		when(manager.getDeserializer(CONTENT_TYPE)).thenReturn(deserializer);
		assertSame(body, d.getBody(Object.class, 200000));
		verify(coder).decode(any(LimitInputStream.class), eq(contentType));
		verify(coder, times(0)).decode(stream, contentType);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { CONTENT_TYPE })
	void getsBinaryBody(String contentType) {
		when(coder.decode(any(), eq(contentType))).thenReturn(stream);
		when(coder.strip(contentType)).thenReturn(null);
		media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
		d = newData(contentType);
		Object body = new Object();
		Disassembler disassembler = mock(Disassembler.class);
		when(disassembler.read(stream, Object.class)).thenReturn(body);
		when(manager.isBinary(Object.class)).thenReturn(true);
		when(manager.getDisassemblerType(null, Object.class)).thenReturn(CONTENT_TYPE);
		when(manager.getDisassembler(CONTENT_TYPE)).thenReturn(disassembler);
		assertSame(body, d.getBody(Object.class, 0));
		verify(coder, times(0)).decode(any(LimitInputStream.class), eq(contentType));
		verify(coder).decode(stream, contentType);
	}

	private Data newData(String contentType) {
		return new Data(manager, contentType, stream);
	}
}
