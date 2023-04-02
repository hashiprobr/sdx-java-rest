package br.pro.hashi.sdx.rest.transform.facade;

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
import br.pro.hashi.sdx.rest.transform.facade.exception.SupportException;

class OctetAssemblerTest {
	private Assembler a;

	@BeforeEach
	void setUp() {
		a = new OctetAssembler();
	}

	@Test
	void writesIfBodyIsByteArray() {
		byte[] body = newByteArray();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, byte[].class, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesIfBodyIsByteArrayWithHint() {
		byte[] body = newByteArray();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<byte[]>() {}.getType(), stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesIfBodyIsInputStream() {
		InputStream body = newInputStream();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, InputStream.class, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesIfBodyIsInputStreamWithHint() {
		InputStream body = newInputStream();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<InputStream>() {}.getType(), stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesIfBodyIsByteArrayInputStream() {
		InputStream body = newInputStream();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, ByteArrayInputStream.class, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesIfBodyIsByteArrayInputStreamWithHint() {
		InputStream body = newInputStream();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<ByteArrayInputStream>() {}.getType(), stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesIfBodyIsConsumer() {
		Consumer<OutputStream> body = (stream) -> {
			try {
				stream.write(newByteArray());
			} catch (IOException exception) {
				throw new AssertionError(exception);
			}
		};
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<Consumer<OutputStream>>() {}.getType(), stream);
		assertEqualsBody(stream);
	}

	private void assertEqualsBody(ByteArrayOutputStream stream) {
		try {
			stream.close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		assertEquals("body", new String(stream.toByteArray(), StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotWriteIfBodyIsNeither() {
		Object body = new Object();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		assertThrows(SupportException.class, () -> {
			a.write(body, Object.class, stream);
		});
	}

	@Test
	void doesNotWriteIfBodyIsNull() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		assertThrows(SupportException.class, () -> {
			a.write(null, stream);
		});
	}

	@Test
	void doesNotWriteIfBodyIsByteArrayButThrows() throws IOException {
		byte[] body = newByteArray();
		OutputStream stream = OutputStream.nullOutputStream();
		stream.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			a.write(body, byte[].class, stream);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void doesNotWriteIfBodyIsInputStreamButThrows() throws IOException {
		InputStream body = newInputStream();
		OutputStream stream = OutputStream.nullOutputStream();
		stream.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			a.write(body, InputStream.class, stream);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	private InputStream newInputStream() {
		return new ByteArrayInputStream(newByteArray());
	}

	private byte[] newByteArray() {
		return "body".getBytes(StandardCharsets.US_ASCII);
	}
}
