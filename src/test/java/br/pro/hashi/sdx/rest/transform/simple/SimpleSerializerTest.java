package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Serializer;

class SimpleSerializerTest {
	private Serializer s;

	@BeforeEach
	void setUp() {
		s = new SimpleSerializer() {
			@Override
			public String toString(Object body) {
				return body.toString();
			}
		};
	}

	@Test
	void toReaderCallsToString() throws IOException {
		Object body = new Object() {
			@Override
			public String toString() {
				return "body";
			}
		};
		Reader reader = s.toReader(body);
		char[] chars = new char[4];
		reader.read(chars, 0, 4);
		assertEquals(-1, reader.read());
		assertEquals("body", new String(chars));
	}
}
