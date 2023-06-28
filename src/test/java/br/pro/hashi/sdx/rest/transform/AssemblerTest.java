package br.pro.hashi.sdx.rest.transform;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.mock.ConcreteAssembler;

class AssemblerTest {
	private Assembler a;

	@BeforeEach
	void setUp() {
		a = new ConcreteAssembler();
	}

	@Test
	void writes() {
		a = spy(a);
		byte[] body = "body".getBytes(StandardCharsets.US_ASCII);
		OutputStream stream = OutputStream.nullOutputStream();
		a.write(body, stream);
		verify(a).write(body, byte[].class, stream);
	}

	@Test
	void doesNotWrite() {
		OutputStream stream = OutputStream.nullOutputStream();
		assertThrows(NullPointerException.class, () -> {
			a.write(null, stream);
		});
	}
}
