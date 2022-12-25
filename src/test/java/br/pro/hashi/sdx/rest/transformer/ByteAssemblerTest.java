package br.pro.hashi.sdx.rest.transformer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transformer.base.Assembler;

class ByteAssemblerTest {
	private Assembler a;

	@BeforeEach
	void setUp() {
		a = new ByteAssembler();
	}

	@Test
	void sameIfBodyIsInputStream() {
		InputStream body = new ByteArrayInputStream("".getBytes());
		assertSame(body, a.assemble(body));
	}

	@Test
	void throwsIfBodyIsNotInputStream() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			a.assemble(body);
		});
	}
}
