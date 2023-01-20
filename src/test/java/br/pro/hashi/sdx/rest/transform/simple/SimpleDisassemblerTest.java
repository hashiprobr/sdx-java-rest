package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
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
			public <T> T read(byte[] bytes, Type type) {
				@SuppressWarnings("unchecked")
				T body = mock((Class<T>) type);
				when(body.toString()).thenReturn(new String(bytes, StandardCharsets.US_ASCII));
				return body;
			}
		};
	}

	@Test
	void reads() {
		InputStream stream = newInputStream();
		Object body = d.read(stream, Object.class);
		assertEqualsInputStream(body);
	}

	@Test
	void readsWithHint() {
		InputStream stream = newInputStream();
		Object body = d.read(stream, new Hint<Object>() {}.getType());
		assertEqualsInputStream(body);
	}

	private InputStream newInputStream() {
		return new ByteArrayInputStream("bytes".getBytes(StandardCharsets.US_ASCII));
	}

	private void assertEqualsInputStream(Object body) {
		assertEquals("bytes", body.toString());
	}
}
