package br.pro.hashi.sdx.rest.transformer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transformer.base.Serializer;

class TextSerializerTest {
	private Serializer s;

	@BeforeEach
	void setUp() {
		s = new TextSerializer();
	}

	@Test
	void sameIfBodyIsString() {
		String body = "";
		assertSame(body, s.serialize(body));
	}

	@Test
	void throwsIfBodyIsNotString() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			s.serialize(body);
		});
	}
}
