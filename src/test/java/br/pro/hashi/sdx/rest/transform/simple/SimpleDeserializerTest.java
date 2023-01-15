package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;

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
			public <T> T fromString(String content, Class<T> type) {
				return mockWithContent(type, content);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T fromString(String content, Hint<T> hint) {
				return mockWithContent((Class<T>) hint.getType(), content);
			}

			private <T> T mockWithContent(Class<T> type, String content) {
				T body = mock(type);
				when(body.toString()).thenReturn(content);
				return body;
			}
		};
	}

	@Test
	void fromReaderCallsFromString() {
		Reader reader = newReader();
		Object body = d.fromReader(reader, Object.class);
		assertEqualsReader(body);
	}

	@Test
	void fromReaderCallsFromStringWithHint() {
		Reader reader = newReader();
		Object body = d.fromReader(reader, new Hint<Object>() {});
		assertEqualsReader(body);
	}

	private Reader newReader() {
		return new StringReader("content");
	}

	private void assertEqualsReader(Object body) {
		assertEquals("content", body.toString());
	}
}
