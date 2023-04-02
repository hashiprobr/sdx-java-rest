package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
				return Objects.toString(body).getBytes(StandardCharsets.US_ASCII);
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
		try {
			stream.close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		assertEqualsBody(stream.toByteArray());
	}

	@Test
	void writesWithNull() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(null, stream);
		assertEqualsNull(stream);
	}

	@Test
	void writesWithNullAndHint() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(null, new Hint<Object>() {}.getType(), stream);
		assertEqualsNull(stream);
	}

	private void assertEqualsNull(ByteArrayOutputStream stream) {
		try {
			stream.close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		assertEqualsNull(stream.toByteArray());
	}

	@Test
	void doesNotWrite() throws IOException {
		OutputStream stream = OutputStream.nullOutputStream();
		stream.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			a.write(body, stream);
		});
		assertInstanceOf(IOException.class, exception.getCause());
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
	void returnsBytesWithNull() {
		byte[] bytes = a.toBytes(null);
		assertEqualsNull(bytes);
	}

	@Test
	void returnsBytesWithNullAndHint() {
		byte[] bytes = a.toBytes(null, new Hint<Object>() {}.getType());
		assertEqualsNull(bytes);
	}

	private void assertEqualsNull(byte[] bytes) {
		assertEquals("null", new String(bytes, StandardCharsets.US_ASCII));
	}
}
