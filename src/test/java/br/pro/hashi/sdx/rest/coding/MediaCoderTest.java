package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.pro.hashi.sdx.rest.coding.exception.CharsetException;
import br.pro.hashi.sdx.rest.constant.Defaults;

class MediaCoderTest {
	private static final String REGULAR_CONTENT = "regular";
	private static final String SPECIAL_CONTENT = "spéçìal";

	private MediaCoder c;

	@BeforeEach
	void setUp() {
		c = new MediaCoder();
	}

	@Test
	void getsInstance() {
		assertInstanceOf(MediaCoder.class, MediaCoder.getInstance());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			" \t\n",
			";;parameter;;name=value;;",
			" \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void stripsToNull(String contentType) {
		assertNull(c.strip(contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"type/subtype",
			" \t\ntype/subtype \t\n",
			"type/subtype;;parameter;;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void stripsToType(String contentType) {
		assertEquals("type/subtype", c.strip(contentType));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {
			"",
			" \t\n",
			";;parameter;;name=value;;",
			" \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegular(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, Defaults.CHARSET);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=us-ascii",
			" \t\n; \t\ncharset \t\n= \t\nUS-ASCII \t\n",
			"type/subtype;;parameter;;charset=us-ascii;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nUS-ASCII \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegularFromUSASCIIToUSASCII(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, StandardCharsets.US_ASCII);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=iso-8859-1",
			" \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n",
			"type/subtype;;parameter;;charset=iso-8859-1;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegularFromUSASCIIToISO88591(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, StandardCharsets.US_ASCII);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=utf-8",
			" \t\n; \t\ncharset \t\n= \t\nUTF-8 \t\n",
			"type/subtype;;parameter;;charset=utf-8;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nUTF-8 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegularFromUSASCIIToUTF8(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, StandardCharsets.US_ASCII);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=us-ascii",
			" \t\n; \t\ncharset \t\n= \t\nUS-ASCII \t\n",
			"type/subtype;;parameter;;charset=us-ascii;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nUS-ASCII \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegularFromISO88591ToUSASCII(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, StandardCharsets.ISO_8859_1);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=iso-8859-1",
			" \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n",
			"type/subtype;;parameter;;charset=iso-8859-1;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegularFromISO88591ToISO88591(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, StandardCharsets.ISO_8859_1);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=utf-8",
			" \t\n; \t\ncharset \t\n= \t\nUTF-8 \t\n",
			"type/subtype;;parameter;;charset=utf-8;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nUTF-8 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegularFromISO88591ToUTF8(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, StandardCharsets.ISO_8859_1);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=us-ascii",
			" \t\n; \t\ncharset \t\n= \t\nUS-ASCII \t\n",
			"type/subtype;;parameter;;charset=us-ascii;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nUS-ASCII \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegularFromUTF8ToUSASCII(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, StandardCharsets.UTF_8);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=iso-8859-1",
			" \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n",
			"type/subtype;;parameter;;charset=iso-8859-1;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegularFromUTF8ToISO88591(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, StandardCharsets.UTF_8);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=utf-8",
			" \t\n; \t\ncharset \t\n= \t\nUTF-8 \t\n",
			"type/subtype;;parameter;;charset=utf-8;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nUTF-8 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsRegularFromUTF8ToUTF8(String contentType) {
		InputStream stream = newInputStream(REGULAR_CONTENT, StandardCharsets.UTF_8);
		assertEquals(REGULAR_CONTENT, read(stream, contentType));
	}

	@Test
	void doesNotReadSpecialFromUSASCIIToUSASCII() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.US_ASCII);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=us-ascii"));
	}

	@Test
	void doesNotReadSpecialFromUSASCIIToISO88591() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.US_ASCII);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=iso-8859-1"));
	}

	@Test
	void doesNotReadSpecialFromUSASCIIToUTF8() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.US_ASCII);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=utf-8"));
	}

	@Test
	void doesNotReadSpecialFromISO88591ToUSASCII() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=us-ascii"));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=iso-8859-1",
			" \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n",
			"type/subtype;;parameter;;charset=iso-8859-1;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nISO-8859-1 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsSpecialFromISO88591ToISO88591(String contentType) {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		assertEquals(SPECIAL_CONTENT, read(stream, contentType));
	}

	@Test
	void doesNotReadSpecialFromISO88591ToUTF8() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=utf-8"));
	}

	@Test
	void doesNotReadSpecialFromUTF8ToUSASCII() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.UTF_8);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=us-ascii"));
	}

	@Test
	void doesNotReadSpecialFromUTF8ToISO88591() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.UTF_8);
		assertNotEquals(SPECIAL_CONTENT, read(stream, ";charset=iso-8859-1"));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";charset=utf-8",
			" \t\n; \t\ncharset \t\n= \t\nUTF-8 \t\n",
			"type/subtype;;parameter;;charset=utf-8;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\nUTF-8 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void readsSpecialFromUTF8ToUTF8(String contentType) {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.UTF_8);
		assertEquals(SPECIAL_CONTENT, read(stream, contentType));
	}

	private String read(InputStream stream, String contentType) {
		return c.read(c.reader(stream, contentType));
	}

	@Test
	void doesNotReadIfCharsetIsIllegal() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		Exception exception = assertThrows(CharsetException.class, () -> {
			c.reader(stream, ";charset=iso=8859=1");
		});
		assertEquals("Charset name iso=8859=1 is not legal", exception.getMessage());
	}

	@Test
	void doesNotReadIfCharsetIsUnsupported() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		Exception exception = assertThrows(CharsetException.class, () -> {
			c.reader(stream, ";charset=latin-1");
		});
		assertEquals("Charset latin-1 is not supported", exception.getMessage());
	}

	@Test
	void doesNotReadIfReaderThrows() {
		Reader reader = Reader.nullReader();
		assertDoesNotThrow(() -> {
			reader.close();
		});
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			c.read(reader);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@Test
	void readsBytes() {
		InputStream stream = newInputStream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		assertEquals(SPECIAL_CONTENT, new String(c.read(stream), StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotReadIfStreamThrows() {
		InputStream stream = InputStream.nullInputStream();
		assertDoesNotThrow(() -> {
			stream.close();
		});
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			c.read(stream);
		});
		assertInstanceOf(IOException.class, exception.getCause());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			";base64",
			" \t\n; \t\nbase64 \t\n",
			"type/subtype;;parameter;;BASE64;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nBASE64 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void decodes(String contentType) {
		assertReads("", "", contentType);
		assertReads("a", "YQ==", contentType);
		assertReads("ab", "YWI=", contentType);
		assertReads("abc", "YWJj", contentType);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {
			"",
			" \t\n",
			"type/subtype;;parameter;;name=value;",
			" \t\ntype/subtype \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n" })
	void doesNotDecode(String contentType) {
		assertReads("", "", contentType);
		assertReads("YQ==", "YQ==", contentType);
		assertReads("YWI=", "YWI=", contentType);
		assertReads("YWJj", "YWJj", contentType);
	}

	private void assertReads(String expected, String content, String contentType) {
		InputStream stream = c.decode(newInputStream(content, StandardCharsets.US_ASCII), contentType);
		assertEquals(expected, new String(c.read(stream), StandardCharsets.US_ASCII));
	}

	private InputStream newInputStream(String content, Charset charset) {
		return new ByteArrayInputStream(content.getBytes(charset));
	}

	@Test
	void encodes() {
		assertWrites("", "");
		assertWrites("YQ==", "a");
		assertWrites("YWI=", "ab");
		assertWrites("YWJj", "abc");
	}

	private void assertWrites(String expected, String content) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(c.encode(stream), StandardCharsets.US_ASCII);
		assertDoesNotThrow(() -> {
			writer.write(content);
			writer.close();
		});
		assertEquals(expected, new String(stream.toByteArray(), StandardCharsets.US_ASCII));
	}
}
