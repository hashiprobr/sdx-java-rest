package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;

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
			public String toString(Object body, Type type) {
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
		s.write(body, new Hint<Object>() {}.getType(), writer);
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
	void toStringCallsToString() {
		String content = s.toString(body);
		assertEqualsBody(content);
	}

	@Test
	void toStringCallsToStringWithHint() {
		String content = s.toString(body, new Hint<Object>() {}.getType());
		assertEqualsBody(content);
	}

	private void assertEqualsBody(String content) {
		assertEquals("body", content);
	}
}
