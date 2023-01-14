package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleAssemblerTest {
	private SimpleAssembler a;
	private Object body;

	@BeforeEach
	void setUp() {
		a = new SimpleAssembler() {
			@Override
			public <T> byte[] toBytes(T body, Class<T> type) {
				return body.toString().getBytes(StandardCharsets.US_ASCII);
			}
		};
		body = new Object() {
			@Override
			public String toString() {
				return "body";
			}
		};
	}

	@Test
	void writeCallsToBytes() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, stream);
		byte[] bytes = stream.toByteArray();
		assertEquals("body", new String(bytes, StandardCharsets.US_ASCII));
	}

	@Test
	void writeThrowsUncheckedIOExceptionIfStreamThrowsIOException() throws IOException {
		OutputStream stream = OutputStream.nullOutputStream();
		stream.close();
		assertThrows(UncheckedIOException.class, () -> {
			a.write(body, stream);
		});
	}

	@Test
	void toStreamCallsToBytes() throws IOException {
		InputStream stream = a.toStream(body);
		byte[] bytes = new byte[4];
		stream.readNBytes(bytes, 0, 4);
		assertEquals(-1, stream.read());
		assertEquals("body", new String(bytes, StandardCharsets.US_ASCII));
		stream.close();
	}

	@Test
	void toBytesCallsToBytes() {
		byte[] bytes = a.toBytes(body);
		assertEquals("body", new String(bytes, StandardCharsets.US_ASCII));
	}
}
