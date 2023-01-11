package br.pro.hashi.sdx.rest.transform.basic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Disassembler;

class BasicDisassemblerTest {
	private InputStream stream;
	private Disassembler d;

	@BeforeEach
	void setUp() {
		stream = new ByteArrayInputStream(newByteArray());
		d = new BasicDisassembler();
	}

	@Test
	void returnsEqualsIfTypeEqualsByteArray() {
		byte[] body = assertDoesNotThrow(() -> {
			return d.fromStream(stream, byte[].class);
		});
		assertArrayEquals(newByteArray(), body);
	}

	@Test
	void returnsSameIfTypeEqualsInputStream() {
		InputStream body = assertDoesNotThrow(() -> {
			return d.fromStream(stream, InputStream.class);
		});
		assertSame(stream, body);
	}

	@Test
	void throwsIfTypeEqualsNeither() {
		assertThrows(IllegalArgumentException.class, () -> {
			d.fromStream(stream, Object.class);
		});
	}

	private byte[] newByteArray() {
		return "bytes".getBytes(StandardCharsets.US_ASCII);
	}
}
