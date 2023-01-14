package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;

class PlainDeserializerTest {
	private Reader reader;
	private Deserializer d;

	@BeforeEach
	void setUp() {
		reader = new StringReader(newString());
		d = new PlainDeserializer();
	}

	@Test
	void returnsEqualsIfTypeEqualsString() {
		assertEquals(newString(), d.fromReader(reader, String.class));
	}

	@Test
	void returnsSameIfTypeEqualsReader() {
		assertSame(reader, d.fromReader(reader, Reader.class));
	}

	@Test
	void throwsDeserializingExceptionIfTypeEqualsNeither() {
		assertThrows(DeserializingException.class, () -> {
			d.fromReader(reader, Object.class);
		});
	}

	private String newString() {
		return "content";
	}
}
