package br.pro.hashi.sdx.rest.server.tree;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Reader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

class DataTest {
	private static final String CONTENT_TYPE = "type/subtype";

	private Facade facade;
	private InputStream stream;
	private MockedStatic<Media> media;
	private Data d;

	@BeforeEach
	void setUp() {
		facade = mock(Facade.class);
		stream = InputStream.nullInputStream();
		media = mockStatic(Media.class);
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
		media.when(() -> Media.decode(any(), eq(contentType))).thenReturn(stream);
		media.when(() -> Media.reader(stream, contentType)).thenReturn(reader);
		media.when(() -> Media.strip(contentType)).thenReturn(null);
		d = newData(contentType);
		Object body = new Object();
		Deserializer deserializer = mock(Deserializer.class);
		when(deserializer.read(reader, Object.class)).thenReturn(body);
		when(facade.isBinary(Object.class)).thenReturn(false);
		when(facade.cleanForDeserializing(null, Object.class)).thenReturn(CONTENT_TYPE);
		when(facade.getDeserializer(CONTENT_TYPE)).thenReturn(deserializer);
		assertSame(body, d.getBody(Object.class, 200000));
		media.verify(() -> Media.decode(any(LimitInputStream.class), eq(contentType)));
		media.verify(() -> Media.decode(stream, contentType), times(0));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { CONTENT_TYPE })
	void getsBinaryBody(String contentType) {
		media.when(() -> Media.decode(any(), eq(contentType))).thenReturn(stream);
		media.when(() -> Media.strip(contentType)).thenReturn(null);
		d = newData(contentType);
		Object body = new Object();
		Disassembler disassembler = mock(Disassembler.class);
		when(disassembler.read(stream, Object.class)).thenReturn(body);
		when(facade.isBinary(Object.class)).thenReturn(true);
		when(facade.cleanForDisassembling(null, Object.class)).thenReturn(CONTENT_TYPE);
		when(facade.getDisassembler(CONTENT_TYPE)).thenReturn(disassembler);
		assertSame(body, d.getBody(Object.class, 0));
		media.verify(() -> Media.decode(any(LimitInputStream.class), eq(contentType)), times(0));
		media.verify(() -> Media.decode(stream, contentType));
	}

	private Data newData(String contentType) {
		return new Data(facade, contentType, stream);
	}
}
