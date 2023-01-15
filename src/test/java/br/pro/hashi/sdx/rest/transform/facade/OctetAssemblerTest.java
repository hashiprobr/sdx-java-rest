package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.exception.AssemblingException;

class OctetAssemblerTest {
	private Assembler a;

	@BeforeEach
	void setUp() {
		a = new OctetAssembler();
	}

	@Test
	void writesEqualsIfBodyIsByteArray() {
		byte[] body = newByteArray();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, byte[].class, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesEqualsIfBodyIsByteArrayWithHint() {
		byte[] body = newByteArray();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<byte[]>() {}, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesEqualsIfBodyIsInputStream() {
		InputStream body = newInputStream();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, InputStream.class, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writesEqualsIfBodyIsInputStreamWithHint() {
		InputStream body = newInputStream();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<InputStream>() {}, stream);
		assertEqualsBody(stream);
	}

	private void assertEqualsBody(ByteArrayOutputStream stream) {
		assertEqualsBody(stream.toByteArray());
	}

	@Test
	void writeThrowsUncheckedIOExceptionIfBodyIsByteArrayButStreamThrowsIOException() throws IOException {
		byte[] body = newByteArray();
		OutputStream stream = OutputStream.nullOutputStream();
		stream.close();
		assertThrows(UncheckedIOException.class, () -> {
			a.write(body, byte[].class, stream);
		});
	}

	@Test
	void writeThrowsUncheckedIOExceptionIfBodyIsInputStreamButStreamThrowsIOException() throws IOException {
		InputStream body = newInputStream();
		OutputStream stream = OutputStream.nullOutputStream();
		stream.close();
		assertThrows(UncheckedIOException.class, () -> {
			a.write(body, InputStream.class, stream);
		});
	}

	@Test
	void writeThrowsAssemblingExceptionIfBodyIsNeither() {
		Object body = new Object();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		assertThrows(AssemblingException.class, () -> {
			a.write(body, Object.class, stream);
		});
	}

	@Test
	void returnsEqualsIfBodyIsByteArray() throws IOException {
		byte[] body = newByteArray();
		InputStream stream = a.toStream(body, byte[].class);
		assertEqualsBody(stream);
	}

	@Test
	void returnsEqualsIfBodyIsByteArrayWithHint() throws IOException {
		byte[] body = newByteArray();
		InputStream stream = a.toStream(body, new Hint<byte[]>() {});
		assertEqualsBody(stream);
	}

	private void assertEqualsBody(InputStream stream) throws IOException {
		byte[] bytes = new byte[4];
		stream.readNBytes(bytes, 0, 4);
		assertEquals(-1, stream.read());
		assertEqualsBody(bytes);
		stream.close();
	}

	private void assertEqualsBody(byte[] bytes) {
		assertEquals("body", new String(bytes, StandardCharsets.US_ASCII));
	}

	@Test
	void returnsSameIfBodyIsInputStream() {
		InputStream body = newInputStream();
		assertSame(body, a.toStream(body, InputStream.class));
	}

	@Test
	void returnsSameIfBodyIsInputStreamWithHint() {
		InputStream body = newInputStream();
		assertSame(body, a.toStream(body, new Hint<InputStream>() {}));
	}

	@Test
	void throwsAssemblingExceptionIfBodyIsNeither() {
		Object body = new Object();
		assertThrows(AssemblingException.class, () -> {
			a.toStream(body, Object.class);
		});
	}

	private InputStream newInputStream() {
		return new ByteArrayInputStream(newByteArray());
	}

	private byte[] newByteArray() {
		return "body".getBytes(StandardCharsets.US_ASCII);
	}
}
