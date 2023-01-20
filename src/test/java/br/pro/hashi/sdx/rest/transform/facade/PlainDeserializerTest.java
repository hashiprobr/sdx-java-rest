package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Hint;
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
		assertEquals(newString(), d.read(reader, String.class));
	}

	@Test
	void returnsEqualsIfTypeEqualsStringWithHint() {
		assertEquals(newString(), d.read(reader, new Hint<String>() {}.getType()));
	}

	@Test
	void returnsSameIfTypeEqualsReader() {
		assertSame(reader, d.read(reader, Reader.class));
	}

	@Test
	void returnsSameIfTypeEqualsReaderWithHint() {
		assertSame(reader, d.read(reader, new Hint<Reader>() {}.getType()));
	}

	@Test
	void returnsSameIfTypeEqualsStringReader() {
		assertSame(reader, d.read(reader, StringReader.class));
	}

	@Test
	void returnsSameIfTypeEqualsStringReaderWithHint() {
		assertSame(reader, d.read(reader, new Hint<StringReader>() {}.getType()));
	}

	@Test
	void throwsDeserializingExceptionIfTypeEqualsNeither() {
		assertThrows(DeserializingException.class, () -> {
			d.read(reader, Object.class);
		});
	}

	@Test
	void throwsDeserializingExceptionIfTypeEqualsNeitherWithHint() {
		assertThrows(DeserializingException.class, () -> {
			d.read(reader, new Hint<Object>() {}.getType());
		});
	}

	private String newString() {
		return "content";
	}
}
