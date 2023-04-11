package br.pro.hashi.sdx.rest.transform.facade;

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
import br.pro.hashi.sdx.rest.transform.exception.UnsupportedException;

class DefaultSerializerTest {
	private Serializer s;

	@BeforeEach
	void setUp() {
		s = new DefaultSerializer();
	}

	@Test
	void writesIfBodyIsBoolean() {
		boolean body = false;
		StringWriter writer = new StringWriter();
		s.write(body, boolean.class, writer);
		assertEquals("false", writer.toString());
	}

	@Test
	void writesIfBodyIsBooleanWithHint() {
		Boolean body = false;
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Boolean>() {}.getType(), writer);
		assertEquals("false", writer.toString());
	}

	@Test
	void writesIfBodyIsByte() {
		byte body = 0;
		StringWriter writer = new StringWriter();
		s.write(body, byte.class, writer);
		assertEquals("0", writer.toString());
	}

	@Test
	void writesIfBodyIsByteWithHint() {
		Byte body = 0;
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Byte>() {}.getType(), writer);
		assertEquals("0", writer.toString());
	}

	@Test
	void writesIfBodyIsShort() {
		short body = 1;
		StringWriter writer = new StringWriter();
		s.write(body, short.class, writer);
		assertEquals("1", writer.toString());
	}

	@Test
	void writesIfBodyIsShortWithHint() {
		Short body = 1;
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Short>() {}.getType(), writer);
		assertEquals("1", writer.toString());
	}

	@Test
	void writesIfBodyIsInteger() {
		int body = 2;
		StringWriter writer = new StringWriter();
		s.write(body, int.class, writer);
		assertEquals("2", writer.toString());
	}

	@Test
	void writesIfBodyIsIntegerWithHint() {
		Integer body = 2;
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Integer>() {}.getType(), writer);
		assertEquals("2", writer.toString());
	}

	@Test
	void writesIfBodyIsLong() {
		long body = 3L;
		StringWriter writer = new StringWriter();
		s.write(body, long.class, writer);
		assertEquals("3", writer.toString());
	}

	@Test
	void writesIfBodyIsLongWithHint() {
		Long body = 3L;
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Long>() {}.getType(), writer);
		assertEquals("3", writer.toString());
	}

	@Test
	void writesIfBodyIsFloat() {
		float body = 4.5F;
		StringWriter writer = new StringWriter();
		s.write(body, float.class, writer);
		assertEquals("4.5", writer.toString());
	}

	@Test
	void writesIfBodyIsFloatWithHint() {
		Float body = 4.5F;
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Float>() {}.getType(), writer);
		assertEquals("4.5", writer.toString());
	}

	@Test
	void writesIfBodyIsDouble() {
		double body = 6.7;
		StringWriter writer = new StringWriter();
		s.write(body, double.class, writer);
		assertEquals("6.7", writer.toString());
	}

	@Test
	void writesIfBodyIsDoubleWithHint() {
		Double body = 6.7;
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Double>() {}.getType(), writer);
		assertEquals("6.7", writer.toString());
	}

	@Test
	void writesIfBodyIsString() {
		String body = newString();
		StringWriter writer = new StringWriter();
		s.write(body, String.class, writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesIfBodyIsStringWithHint() {
		String body = newString();
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<String>() {}.getType(), writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesIfBodyIsReader() {
		Reader body = newReader();
		StringWriter writer = new StringWriter();
		s.write(body, Reader.class, writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesIfBodyIsReaderWithHint() {
		Reader body = newReader();
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Reader>() {}.getType(), writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesIfBodyIsStringReader() {
		Reader body = newReader();
		StringWriter writer = new StringWriter();
		s.write(body, StringReader.class, writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesIfBodyIsStringReaderWithHint() {
		Reader body = newReader();
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<StringReader>() {}.getType(), writer);
		assertEqualsBody(writer);
	}

	@Test
	void writesIfBodyIsConsumer() {
		Consumer<Writer> body = (writer) -> {
			try {
				writer.write(newString());
			} catch (IOException exception) {
				throw new AssertionError(exception);
			}
		};
		StringWriter writer = new StringWriter();
		s.write(body, new Hint<Consumer<Writer>>() {}.getType(), writer);
		assertEqualsBody(writer);
	}

	private void assertEqualsBody(StringWriter writer) {
		try {
			writer.close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		assertEquals("body", writer.toString());
	}

	@Test
	void doesNotWriteIfBodyIsNeither() {
		Object body = new Object();
		Writer writer = new StringWriter();
		assertThrows(UnsupportedException.class, () -> {
			s.write(body, Object.class, writer);
		});
	}

	@Test
	void doesNotWriteIfBodyIsNull() {
		Writer writer = new StringWriter();
		assertThrows(UnsupportedException.class, () -> {
			s.write(null, writer);
		});
	}

	@Test
	void doesNotWriteIfBodyIsStringButThrows() throws IOException {
		String body = newString();
		Writer writer = Writer.nullWriter();
		writer.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, String.class, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void doesNotWriteIfBodyIsReaderButThrows() throws IOException {
		Reader body = newReader();
		Writer writer = Writer.nullWriter();
		writer.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, Reader.class, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	private Reader newReader() {
		return new StringReader(newString());
	}

	private String newString() {
		return "body";
	}
}
