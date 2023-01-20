package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Hint;

class SimpleAssemblerTest {
	private SimpleAssembler a;
	private Object body;

	@BeforeEach
	void setUp() {
		a = new SimpleAssembler() {
			@Override
			public byte[] toBytes(Object body, Type type) {
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
		assertEqualsBody(stream);
	}

	@Test
	void writeCallsToBytesWithHint() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<Object>() {}.getType(), stream);
		assertEqualsBody(stream);
	}

	private void assertEqualsBody(ByteArrayOutputStream stream) {
		assertEqualsBody(stream.toByteArray());
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
	void toBytesCallsToBytes() {
		byte[] bytes = a.toBytes(body);
		assertEqualsBody(bytes);
	}

	@Test
	void toBytesCallsToBytesWithHint() {
		byte[] bytes = a.toBytes(body, new Hint<Object>() {}.getType());
		assertEqualsBody(bytes);
	}

	private void assertEqualsBody(byte[] bytes) {
		assertEquals("body", new String(bytes, StandardCharsets.US_ASCII));
	}
}
