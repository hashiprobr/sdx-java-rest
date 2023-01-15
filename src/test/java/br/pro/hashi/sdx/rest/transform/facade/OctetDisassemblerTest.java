package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

class OctetDisassemblerTest {
	private InputStream stream;
	private Disassembler d;

	@BeforeEach
	void setUp() {
		stream = new ByteArrayInputStream(newByteArray());
		d = new OctetDisassembler();
	}

	@Test
	void returnsEqualsIfTypeEqualsByteArray() {
		assertArrayEquals(newByteArray(), d.fromStream(stream, byte[].class));
	}

	@Test
	void returnsEqualsIfTypeEqualsByteArrayWithHint() {
		assertArrayEquals(newByteArray(), d.fromStream(stream, new Hint<byte[]>() {}));
	}

	@Test
	void returnsSameIfTypeEqualsInputStream() {
		assertSame(stream, d.fromStream(stream, InputStream.class));
	}

	@Test
	void returnsSameIfTypeEqualsInputStreamWithHint() {
		assertSame(stream, d.fromStream(stream, new Hint<InputStream>() {}));
	}

	@Test
	void throwsDisassemblingExceptionIfTypeEqualsNeither() {
		assertThrows(DisassemblingException.class, () -> {
			d.fromStream(stream, Object.class);
		});
	}

	@Test
	void throwsDisassemblingExceptionIfTypeEqualsNeitherWithHint() {
		assertThrows(DisassemblingException.class, () -> {
			d.fromStream(stream, new Hint<Object>() {});
		});
	}

	private byte[] newByteArray() {
		return "bytes".getBytes(StandardCharsets.US_ASCII);
	}
}
