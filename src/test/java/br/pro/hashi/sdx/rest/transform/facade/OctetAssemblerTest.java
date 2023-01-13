package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		InputStream body = new ByteArrayInputStream(newByteArray());
		assertSame(body, a.toStream(body, InputStream.class));
	}

	@Test
	void throwsIfBodyIsNeither() {
		Object body = new Object();
		assertThrows(AssemblingException.class, () -> {
			a.toStream(body, Object.class);
		});
	}

	private byte[] newByteArray() {
		return "body".getBytes(StandardCharsets.US_ASCII);
	}
}
