package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Deserializer;

class SimpleDeserializerTest {
	private Deserializer d;

	@BeforeEach
	void setUp() {
		d = new SimpleDeserializer() {
			@Override
			public <T> T fromString(String content, Class<T> type) {
				T body = mock(type);
				when(body.toString()).thenReturn(content);
				return body;
			}
		};
	}

	@Test
	void fromReaderCallsFromString() {
		Reader reader = new StringReader("content");
		Object body = d.fromReader(reader, Object.class);
		assertEquals("content", body.toString());
	}
}
