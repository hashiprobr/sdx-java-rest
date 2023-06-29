package br.pro.hashi.sdx.rest.transform.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultSerializerTest {
	private Serializer s;

	@BeforeEach
	void setUp() {
		s = new DefaultSerializer();
	}

	@Test
	void getsInstance() {
		assertInstanceOf(DefaultSerializer.class, DefaultSerializer.getInstance());
	}

	@Test
	void writesString() {
		String body = newString();
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesReader() {
		Reader body = new StringReader(newString());
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesWriterConsumer() {
		Consumer<Writer> body = (writer) -> {
			String content = newString();
			assertDoesNotThrow(() -> {
				writer.write(content);
			});
		};
		Type type = new Hint<Consumer<Writer>>() {}.getType();
		StringWriter writer = new StringWriter();
		s.write(body, type, writer);
		assertEqualsBody(writer);
	}

	private void assertEqualsBody(StringWriter writer) {
		assertEquals("body", writer.toString());
	}

	@Test
	void doesNotWriteNull() {
		Writer writer = new StringWriter();
		assertThrows(NullPointerException.class, () -> {
			s.write(null, writer);
		});
	}

	@Test
	void doesNotWriteUnsupportedType() {
		Object body = new Object();
		Writer writer = new StringWriter();
		assertThrows(TypeException.class, () -> {
			s.write(body, writer);
		});
	}

	@Test
	void doesNotWrite() {
		String body = newString();
		Writer writer = Writer.nullWriter();
		assertDoesNotThrow(() -> {
			writer.close();
		});
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	private String newString() {
		return "body";
	}
}
