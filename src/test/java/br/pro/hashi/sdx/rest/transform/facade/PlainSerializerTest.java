package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

class PlainSerializerTest {
	private Serializer s;

	@BeforeEach
	void setUp() {
		s = new PlainSerializer();
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

	private void assertEqualsBody(StringWriter writer) {
		assertEquals("body", writer.toString());
	}

	@Test
	void doesNotWriteIfBodyIsNeither() {
		Object body = new Object();
		Writer writer = new StringWriter();
		assertThrows(SerializingException.class, () -> {
			s.write(body, Object.class, writer);
		});
	}

	@Test
	void throwsIfBodyIsStringButWriteThrows() throws IOException {
		String body = newString();
		Writer writer = Writer.nullWriter();
		writer.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, String.class, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void throwsIfBodyIsStringButCloseThrows() throws IOException {
		String body = newString();
		Writer writer = spy(Writer.nullWriter());
		Throwable cause = new IOException();
		doThrow(cause).when(writer).close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, String.class, writer);
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void throwsIfBodyIsReaderButWriteThrows() throws IOException {
		Reader body = newReader();
		Writer writer = Writer.nullWriter();
		writer.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, Reader.class, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void throwsIfBodyIsReaderButCloseThrows() throws IOException {
		Reader body = newReader();
		Writer writer = spy(Writer.nullWriter());
		Throwable cause = new IOException();
		doThrow(cause).when(writer).close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, Reader.class, writer);
		});
		assertSame(cause, exception.getCause());
	}

	private Reader newReader() {
		return new StringReader(newString());
	}

	private String newString() {
		return "body";
	}
}
