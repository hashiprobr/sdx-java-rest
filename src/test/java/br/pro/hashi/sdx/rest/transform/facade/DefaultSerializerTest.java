package br.pro.hashi.sdx.rest.transform.facade;

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
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultSerializerTest {
	private Serializer s;

	@BeforeEach
	void setUp() {
		s = new DefaultSerializer();
	}

	@Test
	void writesIfBodyIsBoolean() {
		boolean body = true;
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEquals("true", writer.toString());
	}

	@Test
	void writesIfBodyIsByte() {
		byte body = 1;
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEquals("1", writer.toString());
	}

	@Test
	void writesIfBodyIsShort() {
		short body = 2;
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEquals("2", writer.toString());
	}

	@Test
	void writesIfBodyIsInt() {
		int body = 3;
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEquals("3", writer.toString());
	}

	@Test
	void writesIfBodyIsLong() {
		long body = 4;
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEquals("4", writer.toString());
	}

	@Test
	void writesIfBodyIsFloat() {
		float body = 5.5F;
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEquals("5.5", writer.toString());
	}

	@Test
	void writesIfBodyIsDouble() {
		double body = 6.6;
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEquals("6.6", writer.toString());
	}

	@Test
	void writesIfBodyIsString() {
		String body = newString();
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesIfBodyIsReader() {
		Reader body = new StringReader(newString());
		StringWriter writer = new StringWriter();
		s.write(body, writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesIfBodyIsWriterConsumer() {
		Consumer<Writer> body = (writer) -> {
			String content = newString();
			assertDoesNotThrow(() -> {
				writer.write(content);
			});
		};
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Consumer<Writer>>() {}.getType(), writer);
		assertEqualsBody(writer);
	}

	private void assertEqualsBody(StringWriter writer) {
		assertEquals("body", writer.toString());
	}

	@Test
	void doesNotWriteIfBodyIsNull() {
		Writer writer = new StringWriter();
		assertThrows(NullPointerException.class, () -> {
			s.write(null, writer);
		});
	}

	@Test
	void doesNotWriteIfBodyIsNeither() {
		Object body = new Object();
		Writer writer = new StringWriter();
		assertThrows(TypeException.class, () -> {
			s.write(body, writer);
		});
	}

	@Test
	void doesNotWriteIfWriterThrows() throws IOException {
		String body = newString();
		Writer writer = Writer.nullWriter();
		writer.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	private String newString() {
		return "body";
	}
}
