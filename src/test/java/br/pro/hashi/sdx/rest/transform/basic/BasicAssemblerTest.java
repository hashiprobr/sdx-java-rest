package br.pro.hashi.sdx.rest.transform.basic;

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

class BasicAssemblerTest {
	private Assembler a;

	@BeforeEach
	void setUp() {
		a = new BasicAssembler();
	}

	@Test
	void returnsEqualsIfBodyIsByteArray() throws IOException {
		byte[] body = newByteArray();
		InputStream stream = a.toStream(body);
		byte[] bytes = new byte[4];
		stream.read(bytes, 0, 4);
		assertEquals(-1, stream.read());
		assertArrayEquals(body, bytes);
	}

	@Test
	void returnsSameIfBodyIsInputStream() {
		InputStream body = new ByteArrayInputStream(newByteArray());
		assertSame(body, a.toStream(body));
	}

	@Test
	void throwsIfBodyIsNeither() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			a.toStream(body);
		});
	}

	private byte[] newByteArray() {
		return "body".getBytes(StandardCharsets.US_ASCII);
	}
}
