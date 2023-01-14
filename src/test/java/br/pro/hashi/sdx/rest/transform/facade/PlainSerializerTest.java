package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

class PlainSerializerTest {
	private Serializer s;

	@BeforeEach
	void setUp() {
		s = new PlainSerializer();
	}

	@Test
	void writesEqualsIfBodyIsString() {
		String body = newString();
		Writer writer = new StringWriter();
		s.write(body, String.class, writer);
		assertEquals(body, writer.toString());
	}

	@Test
	void writesEqualsIfBodyIsReader() {
		Reader body = newReader();
		Writer writer = new StringWriter();
		s.write(body, Reader.class, writer);
		assertEquals(newString(), writer.toString());
	}

	@Test
	void writeThrowsUncheckedIOExceptionIfBodyIsStringButStreamThrowsIOException() throws IOException {
		String body = newString();
		Writer writer = Writer.nullWriter();
		writer.close();
		assertThrows(UncheckedIOException.class, () -> {
			s.write(body, String.class, writer);
		});
	}

	@Test
	void writeThrowsUncheckedIOExceptionIfBodyIsReaderButStreamThrowsIOException() throws IOException {
		Reader body = newReader();
		Writer writer = Writer.nullWriter();
		writer.close();
		assertThrows(UncheckedIOException.class, () -> {
			s.write(body, Reader.class, writer);
		});
	}

	@Test
	void writeThrowsSerializingExceptionIfBodyIsNeither() {
		Object body = new Object();
		Writer writer = new StringWriter();
		assertThrows(SerializingException.class, () -> {
			s.write(body, Object.class, writer);
		});
	}

	@Test
	void returnsEqualsIfBodyIsString() throws IOException {
		String body = newString();
		Reader reader = s.toReader(body, String.class);
		char[] chars = new char[4];
		reader.read(chars, 0, 4);
		assertEquals(-1, reader.read());
		assertEquals(body, new String(chars));
		reader.close();
	}

	@Test
	void returnsSameIfBodyIsReader() {
		Reader body = newReader();
		assertSame(body, s.toReader(body, Reader.class));
	}

	@Test
	void throwsSerializingExceptionIfBodyIsNeither() {
		Object body = new Object();
		assertThrows(SerializingException.class, () -> {
			s.toReader(body, Object.class);
		});
	}

	private Reader newReader() {
		return new StringReader(newString());
	}

	private String newString() {
		return "body";
	}
}
