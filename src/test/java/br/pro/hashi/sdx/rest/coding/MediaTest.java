package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.pro.hashi.sdx.rest.coding.exception.CharsetException;

class MediaTest {
	private static final String USASCII_CONTENT = "usascii";
	private static final String SPECIAL_CONTENT = "spéçíál";

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			" \t\n",
			";;parameter;;name=value;",
			" \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void stripsToNull(String contentType) {
		assertNull(Media.strip(contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"type/subtype",
			" \t\ntype/subtype \t\n",
			"type/subtype;;parameter;;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void stripsToExpected(String contentType) {
		assertEquals("type/subtype", Media.strip(contentType));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {
			"",
			" \t\n",
			";;parameter;;name=value;",
			" \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsCorrectly(String contentType) {
		InputStream stream = newInputStream(SPECIAL_CONTENT, Coding.CHARSET);
		assertEquals(SPECIAL_CONTENT, read(stream, contentType));
	}

	@Test
	void readsCorrectlyUSASCII() {
		InputStream stream = newInputStream(USASCII_CONTENT, StandardCharsets.US_ASCII);
		assertEquals(USASCII_CONTENT, read(stream, ";charset=us-ascii"));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=iso-8859-1",
			" \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n",
			"type/subtype;;parameter;;charset=iso-8859-1;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsCorrectlyISO88591(String contentType) {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		assertEquals(SPECIAL_CONTENT, read(stream, contentType));
	}

	@Test
	void readsCorrectlyUTF8() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.UTF_8);
		assertEquals(SPECIAL_CONTENT, read(stream, ";charset=utf-8"));
	}

	@Test
	void readsCorrectlyUSASCIIFromISO88591() {
		InputStream stream = newInputStream(USASCII_CONTENT, StandardCharsets.ISO_8859_1);
		assertEquals(USASCII_CONTENT, read(stream, ";charset=us-ascii"));
	}

	@Test
	void readsCorrectlyUSASCIIFromUTF8() {
		InputStream stream = newInputStream(USASCII_CONTENT, StandardCharsets.UTF_8);
		assertEquals(USASCII_CONTENT, read(stream, ";charset=us-ascii"));
	}

	@Test
	void readsCorrectlyISO88591FromUSASCII() {
		InputStream stream = newInputStream(USASCII_CONTENT, StandardCharsets.US_ASCII);
		assertEquals(USASCII_CONTENT, read(stream, ";charset=iso-8859-1"));
	}

	@Test
	void readsCorrectlyISO88591FromUTF8() {
		InputStream stream = newInputStream(USASCII_CONTENT, StandardCharsets.UTF_8);
		assertEquals(USASCII_CONTENT, read(stream, ";charset=iso-8859-1"));
	}

	@Test
	void readsCorrectlyUTF8FromUSASCII() {
		InputStream stream = newInputStream(USASCII_CONTENT, StandardCharsets.US_ASCII);
		assertEquals(USASCII_CONTENT, read(stream, ";charset=utf-8"));
	}

	@Test
	void readsCorrectlyUTF8FromISO88591() {
		InputStream stream = newInputStream(USASCII_CONTENT, StandardCharsets.ISO_8859_1);
		assertEquals(USASCII_CONTENT, read(stream, ";charset=utf-8"));
	}

	@Test
	void readsIncorrectlyUSASCIIFromISO88591() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=us-ascii"));
	}

	@Test
	void readsIncorrectlyUSASCIIFromUTF8() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.UTF_8);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=us-ascii"));
	}

	@Test
	void readsIncorrectlyISO88591FromUSASCII() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.US_ASCII);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=iso-8859-1"));
	}

	@Test
	void readsIncorrectlyISO88591FromUTF8() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.UTF_8);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=iso-8859-1"));
	}

	@Test
	void readsIncorrectlyUTF8FromUSASCII() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.US_ASCII);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=utf-8"));
	}

	@Test
	void readsIncorrectlyUTF8FromISO88591() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=utf-8"));
	}

	private String read(InputStream stream, String contentType) {
		Reader reader = assertDoesNotThrow(() -> {
			return Media.reader(stream, contentType);
		});
		String content = assertDoesNotThrow(() -> {
			return Media.read(reader);
		});
		return content;
	}

	@Test
	void doesNotReadIfCharsetIsIllegal() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		Exception exception = assertThrows(CharsetException.class, () -> {
			Media.reader(stream, ";charset=iso=8859=1");
		});
		assertEquals("Charset name iso=8859=1 is not legal", exception.getMessage());
	}

	@Test
	void doesNotReadIfCharsetIsUnsupported() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		Exception exception = assertThrows(CharsetException.class, () -> {
			Media.reader(stream, ";charset=latin-1");
		});
		assertEquals("Charset latin-1 is not supported", exception.getMessage());
	}

	@Test
	void doesNotReadIfReadThrows() throws IOException {
		InputStream stream = InputStream.nullInputStream();
		stream.close();
		Reader reader = assertDoesNotThrow(() -> {
			return Media.reader(stream, null);
		});
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			Media.read(reader);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void doesNotReadIfCloseThrows() throws IOException {
		InputStream stream = spy(InputStream.nullInputStream());
		Throwable cause = new IOException();
		doThrow(cause).when(stream).close();
		Reader reader = assertDoesNotThrow(() -> {
			return Media.reader(stream, null);
		});
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			Media.read(reader);
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void readsBytes() {
		InputStream stream = newInputStream(USASCII_CONTENT, StandardCharsets.US_ASCII);
		assertEquals(USASCII_CONTENT, new String(Media.read(stream), StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotReadBytesIfReadThrows() throws IOException {
		InputStream stream = InputStream.nullInputStream();
		stream.close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			Media.read(stream);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void doesNotReadBytesIfCloseThrows() throws IOException {
		InputStream stream = spy(InputStream.nullInputStream());
		Throwable cause = new IOException();
		doThrow(cause).when(stream).close();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			Media.read(stream);
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void decodesZeroChars() {
		assertReads("", "", ";base64");
	}

	@Test
	void decodesOneChar() {
		assertReads("0", "MA==", ";base64");
	}

	@Test
	void decodesTwoChars() {
		assertReads("01", "MDE=", ";base64");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";base64",
			" \t\n; \t\nbase64 \t\n",
			"type/subtype;;parameter;;BASE64;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nBASE64 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void decodesThreeChars(String contentType) {
		assertReads("012", "MDEy", contentType);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {
			"",
			" \t\n",
			"type/subtype;;parameter;;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void doesNotDecode(String contentType) {
		assertReads("MDEy", "MDEy", contentType);
	}

	private void assertReads(String expected, String content, String contentType) {
		InputStream stream = Media.decode(newInputStream(content, StandardCharsets.US_ASCII), contentType);
		assertEquals(expected, new String(Media.read(stream), StandardCharsets.US_ASCII));
	}

	private InputStream newInputStream(String content, Charset charset) {
		return new ByteArrayInputStream(content.getBytes(charset));
	}

	@Test
	void encodesZeroChars() throws IOException {
		assertWrites("", "");
	}

	@Test
	void encodesOneChar() throws IOException {
		assertWrites("MA==", "0");
	}

	@Test
	void encodesTwoChars() throws IOException {
		assertWrites("MDE=", "01");
	}

	@Test
	void encodesThreeChars() throws IOException {
		assertWrites("MDEy", "012");
	}

	private void assertWrites(String expected, String content) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(Media.encode(stream), StandardCharsets.US_ASCII);
		writer.write(content);
		writer.close();
		assertEquals(expected, new String(stream.toByteArray(), StandardCharsets.US_ASCII));
	}
}
