package br.pro.hashi.sdx.rest.transform.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultAssemblerTest {
	private Assembler a;

	@BeforeEach
	void setUp() {
		a = new DefaultAssembler();
	}

	@Test
	void writesByteArray() {
		byte[] body = newByteArray();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesInputStream() {
		InputStream body = new ByteArrayInputStream(newByteArray());
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesOutputStreamConsumer() {
		Consumer<OutputStream> body = (stream) -> {
			byte[] bytes = newByteArray();
			assertDoesNotThrow(() -> {
				stream.write(bytes);
			});
		};
		Type type = new Hint<Consumer<OutputStream>>() {}.getType();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, type, stream);
		assertEqualsBody(stream);
	}

	private void assertEqualsBody(ByteArrayOutputStream stream) {
		assertEquals("body", new String(stream.toByteArray(), StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotWriteNull() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		assertThrows(NullPointerException.class, () -> {
			a.write(null, stream);
		});
	}

	@Test
	void doesNotWriteUnsupportedType() {
		Object body = new Object();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		assertThrows(TypeException.class, () -> {
			a.write(body, stream);
		});
	}

	@Test
	void doesNotWrite() {
		byte[] body = newByteArray();
		OutputStream stream = OutputStream.nullOutputStream();
		assertDoesNotThrow(() -> {
			stream.close();
		});
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			a.write(body, stream);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	private byte[] newByteArray() {
		return "body".getBytes(StandardCharsets.US_ASCII);
	}
}
