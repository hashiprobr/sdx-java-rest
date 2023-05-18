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
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultDisassemblerTest {
	private InputStream stream;
	private Disassembler d;

	@BeforeEach
	void setUp() {
		stream = new ByteArrayInputStream(newByteArray());
		d = new DefaultDisassembler();
	}

	@Test
	void readsIfTypeEqualsByteArray() {
		assertArrayEquals(newByteArray(), d.read(stream, byte[].class));
	}

	@Test
	void readsIfTypeEqualsByteArrayWithHint() {
		assertArrayEquals(newByteArray(), d.read(stream, new Hint<byte[]>() {}.getType()));
	}

	@Test
	void readsIfTypeEqualsInputStream() {
		assertSame(stream, d.read(stream, InputStream.class));
	}

	@Test
	void readsIfTypeEqualsInputStreamWithHint() {
		assertSame(stream, d.read(stream, new Hint<InputStream>() {}.getType()));
	}

	@Test
	void doesNotReadIfTypeByteArrayInputStream() {
		assertThrows(TypeException.class, () -> {
			d.read(stream, ByteArrayInputStream.class);
		});
	}

	@Test
	void doesNotReadIfTypeByteArrayInputStreamWithHint() {
		assertThrows(TypeException.class, () -> {
			d.read(stream, new Hint<ByteArrayInputStream>() {}.getType());
		});
	}

	private byte[] newByteArray() {
		return "bytes".getBytes(StandardCharsets.US_ASCII);
	}
}
