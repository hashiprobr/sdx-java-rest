package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleSerializerTest {
	private SimpleSerializer s;
	private Object body;

	@BeforeEach
	void setUp() {
		s = new SimpleSerializer() {
			@Override
			public <T> String toString(T body, Class<T> type) {
				return body.toString();
			}
		};
		body = new Object() {
			@Override
			public String toString() {
				return "body";
			}
		};
	}

	@Test
	void writeCallsToString() {
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		String content = writer.toString();
		assertEquals("body", content);
	}

	@Test
	void writeThrowsUncheckedIOExceptionIfWriterThrowsIOException() throws IOException {
		Writer writer = Writer.nullWriter();
		writer.close();
		assertThrows(UncheckedIOException.class, () -> {
			s.write(body, writer);
		});
	}

	@Test
	void toReaderCallsToString() throws IOException {
		Reader reader = s.toReader(body);
		char[] chars = new char[4];
		reader.read(chars, 0, 4);
		assertEquals(-1, reader.read());
		assertEquals("body", new String(chars));
		reader.close();
	}

	@Test
	void toStringCallsToString() {
		String content = s.toString(body);
		assertEquals("body", content);
	}
}
