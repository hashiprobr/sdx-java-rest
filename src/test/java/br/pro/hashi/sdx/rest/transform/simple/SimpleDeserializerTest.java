package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Hint;

class SimpleDeserializerTest {
	private Deserializer d;

	@BeforeEach
	void setUp() {
		d = new SimpleDeserializer() {
			@Override
			public <T> T read(String content, Type type) {
				@SuppressWarnings("unchecked")
				T body = mock((Class<T>) type);
				when(body.toString()).thenReturn(content);
				return body;
			}
		};
	}

	@Test
	void fromReaderCallsFromString() {
		Reader reader = newReader();
		Object body = d.read(reader, Object.class);
		assertEqualsReader(body);
	}

	@Test
	void fromReaderCallsFromStringWithHint() {
		Reader reader = newReader();
		Object body = d.read(reader, new Hint<Object>() {}.getType());
		assertEqualsReader(body);
	}

	private Reader newReader() {
		return new StringReader("content");
	}

	private void assertEqualsReader(Object body) {
		assertEquals("content", body.toString());
	}
}
