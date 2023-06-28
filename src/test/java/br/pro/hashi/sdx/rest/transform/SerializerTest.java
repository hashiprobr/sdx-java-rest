package br.pro.hashi.sdx.rest.transform;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.Writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.mock.ConcreteSerializer;

class SerializerTest {
	private Serializer s;

	@BeforeEach
	void setUp() {
		s = new ConcreteSerializer();
	}

	@Test
	void writes() {
		s = spy(s);
		String body = "body";
		Writer writer = Writer.nullWriter();
		s.write(body, writer);
		verify(s).write(body, String.class, writer);
	}

	@Test
	void doesNotWrite() {
		Writer writer = Writer.nullWriter();
		assertThrows(NullPointerException.class, () -> {
			s.write(null, writer);
		});
	}
}
