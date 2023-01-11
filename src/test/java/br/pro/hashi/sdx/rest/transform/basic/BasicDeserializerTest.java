package br.pro.hashi.sdx.rest.transform.basic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Deserializer;

class BasicDeserializerTest {
	private Reader reader;
	private Deserializer d;

	@BeforeEach
	void setUp() {
		reader = new StringReader(newString());
		d = new BasicDeserializer();
	}

	@Test
	void returnsEqualsIfTypeEqualsString() {
		String body = assertDoesNotThrow(() -> {
			return d.fromReader(reader, String.class);
		});
		assertEquals(newString(), body);
	}

	@Test
	void returnsSameIfTypeEqualsReader() {
		Reader body = assertDoesNotThrow(() -> {
			return d.fromReader(reader, Reader.class);
		});
		assertSame(reader, body);
	}

	@Test
	void throwsIfTypeEqualsNeither() {
		assertThrows(IllegalArgumentException.class, () -> {
			d.fromReader(reader, Object.class);
		});
	}

	private String newString() {
		return "content";
	}
}
