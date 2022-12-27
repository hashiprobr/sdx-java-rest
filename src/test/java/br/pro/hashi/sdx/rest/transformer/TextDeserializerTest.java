package br.pro.hashi.sdx.rest.transformer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transformer.base.Deserializer;

class TextDeserializerTest {
	private Deserializer d;
	private String content;

	@BeforeEach
	void setUp() {
		d = new TextDeserializer();
		content = "content";
	}

	@Test
	void returnsSameIfTypeEqualsString() {
		String body = assertDoesNotThrow(() -> {
			return d.deserialize(content, String.class);
		});
		assertSame(body, content);
	}

	@Test
	void throwsIfTypeNotEqualsString() {
		assertThrows(IllegalArgumentException.class, () -> {
			d.deserialize(content, Object.class);
		});
	}
}
