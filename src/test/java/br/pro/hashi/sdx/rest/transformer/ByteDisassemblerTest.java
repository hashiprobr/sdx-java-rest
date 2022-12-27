package br.pro.hashi.sdx.rest.transformer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.transformer.base.Disassembler;

class ByteDisassemblerTest {
	private Disassembler d;
	private InputStream stream;

	@BeforeEach
	void setUp() {
		d = new ByteDisassembler();
		stream = new ByteArrayInputStream("stream".getBytes(Coding.CHARSET));
	}

	@Test
	void returnsSameIfTypeEqualsInputStream() {
		InputStream body = assertDoesNotThrow(() -> {
			return d.disassemble(stream, InputStream.class);
		});
		assertSame(body, stream);
	}

	@Test
	void throwsIfTypeNotEqualsInputStream() {
		assertThrows(IllegalArgumentException.class, () -> {
			d.disassemble(stream, Object.class);
		});
	}
}
