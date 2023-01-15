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

import br.pro.hashi.sdx.rest.transform.Hint;

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

			@Override
			public <T> String toString(T body, Hint<T> hint) {
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
		assertEqualsBody(writer);
	}

	@Test
	void writeCallsToStringWithHint() {
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Object>() {}, writer);
		assertEqualsBody(writer);
	}

	private void assertEqualsBody(StringWriter writer) {
		assertEqualsBody(writer.toString());
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
		assertEqualsBody(reader);
	}

	@Test
	void toReaderCallsToStringWithHint() throws IOException {
		Reader reader = s.toReader(body, new Hint<Object>() {});
		assertEqualsBody(reader);
	}

	private void assertEqualsBody(Reader reader) throws IOException {
		char[] chars = new char[4];
		reader.read(chars, 0, 4);
		assertEquals(-1, reader.read());
		assertEqualsBody(new String(chars));
		reader.close();
	}

	@Test
	void toStringCallsToString() {
		String content = s.toString(body);
		assertEqualsBody(content);
	}

	@Test
	void toStringCallsToStringWithHint() {
		String content = s.toString(body, new Hint<Object>() {});
		assertEqualsBody(content);
	}

	private void assertEqualsBody(String content) {
		assertEquals("body", content);
	}
}
