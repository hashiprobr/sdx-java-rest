package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Serializer;

class PlainSerializerTest {
	private Serializer s;

	@BeforeEach
	void setUp() {
		s = new PlainSerializer();
	}

	@Test
	void returnsEqualsIfBodyIsString() throws IOException {
		String body = newString();
		Reader reader = s.toReader(body);
		char[] chars = new char[4];
		reader.read(chars, 0, 4);
		assertEquals(-1, reader.read());
		assertEquals(body, new String(chars));
		reader.close();
	}

	@Test
	void returnsSameIfBodyIsReader() {
		Reader body = new StringReader(newString());
		Reader reader = assertDoesNotThrow(() -> {
			return s.toReader(body);
		});
		assertSame(body, reader);
	}

	@Test
	void throwsIfBodyIsNeither() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			s.toReader(body);
		});
	}

	private String newString() {
		return "body";
	}
}
