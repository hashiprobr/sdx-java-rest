package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

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
	void writes() {
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesWithHint() {
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Object>() {}.getType(), writer);
		assertEqualsBody(writer);
	}

	private void assertEqualsBody(StringWriter writer) {
		assertEqualsBody(writer.toString());
	}

	@Test
	void returnsString() {
		String content = s.toString(body);
		assertEqualsBody(content);
	}

	@Test
	void returnsStringWithHint() {
		String content = s.toString(body, new Hint<Object>() {}.getType());
		assertEqualsBody(content);
	}

	private void assertEqualsBody(String content) {
		assertEquals("body", content);
	}

	@Test
	void throwsIfWriteThrows() throws IOException {
		Writer writer = Writer.nullWriter();
		writer.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void throwsIfCloseThrows() throws IOException {
		Writer writer = spy(Writer.nullWriter());
		Throwable cause = new IOException();
		doThrow(cause).when(writer).close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, writer);
		});
		assertSame(cause, exception.getCause());
	}
}
