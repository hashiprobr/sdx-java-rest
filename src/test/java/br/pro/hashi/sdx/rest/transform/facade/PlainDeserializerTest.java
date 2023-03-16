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
import br.pro.hashi.sdx.rest.transform.facade.exception.SupportException;

class PlainDeserializerTest {
	private Reader reader;
	private Deserializer d;

	@BeforeEach
	void setUp() {
		reader = new StringReader(newString());
		d = new PlainDeserializer();
	}

	@Test
	void readsIfTypeEqualsString() {
		assertEquals(newString(), d.read(reader, String.class));
	}

	@Test
	void readsIfTypeEqualsStringWithHint() {
		assertEquals(newString(), d.read(reader, new Hint<String>() {}.getType()));
	}

	@Test
	void readsIfTypeEqualsReader() {
		assertSame(reader, d.read(reader, Reader.class));
	}

	@Test
	void readsIfTypeEqualsReaderWithHint() {
		assertSame(reader, d.read(reader, new Hint<Reader>() {}.getType()));
	}

	@Test
	void doesNotReadIfTypeEqualsStringReader() {
		assertThrows(SupportException.class, () -> {
			d.read(reader, StringReader.class);
		});
	}

	@Test
	void doesNotReadIfTypeEqualsStringReaderWithHint() {
		assertThrows(SupportException.class, () -> {
			d.read(reader, new Hint<StringReader>() {}.getType());
		});
	}

	private String newString() {
		return "content";
	}
}
