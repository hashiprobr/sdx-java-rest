package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;

class SimpleDisassemblerTest {
	private Disassembler d;

	@BeforeEach
	void setUp() {
		d = new SimpleDisassembler() {
			@Override
			public <T> T fromBytes(byte[] bytes, Class<T> type) {
				return mockWithBytes(type, bytes);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T fromBytes(byte[] bytes, Hint<T> hint) {
				return mockWithBytes((Class<T>) hint.getType(), bytes);
			}

			private <T> T mockWithBytes(Class<T> type, byte[] bytes) {
				T body = mock(type);
				when(body.toString()).thenReturn(new String(bytes, StandardCharsets.US_ASCII));
				return body;
			}
		};
	}

	@Test
	void fromStreamCallsFromBytes() {
		InputStream stream = newInputStream();
		Object body = d.fromStream(stream, Object.class);
		assertEqualsInputStream(body);
	}

	@Test
	void fromStreamCallsFromBytesWithHint() {
		InputStream stream = newInputStream();
		Object body = d.fromStream(stream, new Hint<Object>() {});
		assertEqualsInputStream(body);
	}

	private InputStream newInputStream() {
		return new ByteArrayInputStream("bytes".getBytes(StandardCharsets.US_ASCII));
	}

	private void assertEqualsInputStream(Object body) {
		assertEquals("bytes", body.toString());
	}
}
