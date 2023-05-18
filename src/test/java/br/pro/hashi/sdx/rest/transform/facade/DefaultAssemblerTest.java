package br.pro.hashi.sdx.rest.transform.facade;

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
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultAssemblerTest {
	private Assembler a;

	@BeforeEach
	void setUp() {
		a = new DefaultAssembler();
	}

	@Test
	void writesIfBodyIsByteArray() {
		byte[] body = newByteArray();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesIfBodyIsInputStream() {
		InputStream body = new ByteArrayInputStream(newByteArray());
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesIfBodyIsOutputStreamConsumer() {
		Consumer<OutputStream> body = (stream) -> {
			byte[] bytes = newByteArray();
			assertDoesNotThrow(() -> {
				stream.write(bytes);
			});
		};
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<Consumer<OutputStream>>() {}.getType(), stream);
		assertEqualsBody(stream);
	}

	private void assertEqualsBody(ByteArrayOutputStream stream) {
		assertEquals("body", new String(stream.toByteArray(), StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotWriteIfBodyIsNull() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		assertThrows(NullPointerException.class, () -> {
			a.write(null, stream);
		});
	}

	@Test
	void doesNotWriteIfBodyIsNeither() {
		Object body = new Object();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		assertThrows(TypeException.class, () -> {
			a.write(body, stream);
		});
	}

	@Test
	void doesNotWriteIfOutputStreamThrows() throws IOException {
		byte[] body = newByteArray();
		OutputStream stream = OutputStream.nullOutputStream();
		stream.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			a.write(body, stream);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	private byte[] newByteArray() {
		return "body".getBytes(StandardCharsets.US_ASCII);
	}
}
