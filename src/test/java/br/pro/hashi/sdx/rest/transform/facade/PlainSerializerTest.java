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
import br.pro.hashi.sdx.rest.transform.facade.exception.SupportException;

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
		assertThrows(SupportException.class, () -> {
			s.write(body, Object.class, writer);
		});
	}

	@Test
	void doesNotWriteIfBodyIsNull() {
		Writer writer = new StringWriter();
		assertThrows(SupportException.class, () -> {
			s.write(null, writer);
		});
	}

	@Test
	void doesNotWriteIfBodyIsStringButWriteThrows() throws IOException {
		String body = newString();
		Writer writer = Writer.nullWriter();
		writer.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, String.class, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void doesNotWriteIfBodyIsStringButCloseThrows() throws IOException {
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
	void doesNotWriteIfBodyIsReaderButWriteThrows() throws IOException {
		Reader body = newReader();
		Writer writer = Writer.nullWriter();
		writer.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			s.write(body, Reader.class, writer);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void doesNotWriteIfBodyIsReaderButCloseThrows() throws IOException {
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
