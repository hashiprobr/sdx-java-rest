package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Objects;

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
				return Objects.toString(body);
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
		try {
			writer.close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		assertEqualsBody(writer.toString());
	}

	@Test
	void writesWithNull() {
		StringWriter writer = new StringWriter();
		s.write(null, writer);
		assertEqualsNull(writer);
	}

	@Test
	void writesWithNullAndHint() {
		StringWriter writer = new StringWriter();
		s.write(null, new Hint<Object>() {}.getType(), writer);
		assertEqualsNull(writer);
	}

	private void assertEqualsNull(StringWriter writer) {
		try {
			writer.close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		assertEqualsNull(writer.toString());
	}

	@Test
	void doesNotWrite() throws IOException {
		Writer writer = Writer.nullWriter();
		writer.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
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
	void returnsStringWithNull() {
		String content = s.toString(null);
		assertEqualsNull(content);
	}

	@Test
	void returnsStringWithNullAndHint() {
		String content = s.toString(null, new Hint<Object>() {}.getType());
		assertEqualsNull(content);
	}

	private void assertEqualsNull(String content) {
		assertEquals("null", content);
	}
}
