package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Disassembler;

class SimpleDisassemblerTest {
	private Disassembler d;

	@BeforeEach
	void setUp() {
		d = new SimpleDisassembler() {
			@Override
			public <T> T fromBytes(byte[] bytes, Class<T> type) {
				T body = mock(type);
				when(body.toString()).thenReturn(new String(bytes, StandardCharsets.US_ASCII));
				return body;
			}
		};
	}

	@Test
	void fromStreamCallsFromBytes() {
		InputStream stream = new ByteArrayInputStream("bytes".getBytes(StandardCharsets.US_ASCII));
		Object body = assertDoesNotThrow(() -> {
			return d.fromStream(stream, Object.class);
		});
		assertEquals("bytes", body.toString());
	}
}
