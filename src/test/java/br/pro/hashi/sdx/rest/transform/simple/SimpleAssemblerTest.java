package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

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
	void writes() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesWithHint() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<Object>() {}.getType(), stream);
		assertEqualsBody(stream);
	}

	private void assertEqualsBody(ByteArrayOutputStream stream) {
		assertEqualsBody(stream.toByteArray());
	}

	@Test
	void returnsBytes() {
		byte[] bytes = a.toBytes(body);
		assertEqualsBody(bytes);
	}

	@Test
	void returnsBytesWithHint() {
		byte[] bytes = a.toBytes(body, new Hint<Object>() {}.getType());
		assertEqualsBody(bytes);
	}

	private void assertEqualsBody(byte[] bytes) {
		assertEquals("body", new String(bytes, StandardCharsets.US_ASCII));
	}

	@Test
	void throwsIfWriteThrows() throws IOException {
		OutputStream stream = OutputStream.nullOutputStream();
		stream.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			a.write(body, stream);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void throwsIfCloseThrows() throws IOException {
		OutputStream stream = spy(OutputStream.nullOutputStream());
		Throwable cause = new IOException();
		doThrow(cause).when(stream).close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			a.write(body, stream);
		});
		assertSame(cause, exception.getCause());
	}
}
