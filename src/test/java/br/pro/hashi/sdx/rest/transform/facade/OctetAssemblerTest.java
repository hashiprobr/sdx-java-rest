package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
		assertArrayEquals(body, stream.toByteArray());
	}

	@Test
	void writesEqualsIfBodyIsInputStream() {
		InputStream body = newInputStream();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, InputStream.class, stream);
		assertArrayEquals(newByteArray(), stream.toByteArray());
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
		byte[] bytes = new byte[4];
		stream.readNBytes(bytes, 0, 4);
		assertEquals(-1, stream.read());
		assertArrayEquals(body, bytes);
		stream.close();
	}

	@Test
	void returnsSameIfBodyIsInputStream() {
		InputStream body = newInputStream();
		assertSame(body, a.toStream(body, InputStream.class));
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
